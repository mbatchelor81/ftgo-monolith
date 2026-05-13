package net.chrisrichardson.ftgo.errorhandling;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.ConnectException;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerIntegrationTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        Tracer tracer = mock(Tracer.class);
        Span span = mock(Span.class);
        TraceContext ctx = mock(TraceContext.class);
        when(ctx.traceId()).thenReturn("test-trace-id-001");
        when(span.context()).thenReturn(ctx);
        when(tracer.currentSpan()).thenReturn(span);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler(tracer))
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    @RestController
    static class TestController {

        @GetMapping("/test/not-found")
        String notFound() {
            throw new OrderNotFoundException(42L);
        }

        @GetMapping("/test/state-conflict")
        String stateConflict() {
            throw new UnsupportedStateTransitionException("APPROVED");
        }

        @GetMapping("/test/order-minimum")
        String orderMinimum() {
            throw new OrderMinimumNotMetException();
        }

        @GetMapping("/test/illegal-argument")
        String illegalArgument() {
            throw new IllegalArgumentException("Invalid parameter value");
        }

        @GetMapping("/test/illegal-state")
        String illegalState() {
            throw new IllegalStateException("Cannot modify in current state");
        }

        @GetMapping("/test/not-implemented")
        String notImplemented() {
            throw new UnsupportedOperationException("Feature not available");
        }

        @GetMapping("/test/connect-error")
        String connectError() throws ConnectException {
            throw new ConnectException("Connection refused");
        }

        @GetMapping("/test/internal-error")
        String internalError() {
            throw new RuntimeException("Something went wrong");
        }

        @GetMapping("/test/checked-error")
        String checkedError() throws Exception {
            throw new Exception("Checked exception");
        }

        @GetMapping("/test/optimistic-lock")
        String optimisticLock() {
            throw new OptimisticOfflineLockException();
        }

        @PostMapping("/test/validate")
        String validate(@Valid @RequestBody CreateOrderRequest request) {
            return "ok";
        }

        @GetMapping("/test/ok")
        String ok() {
            return "ok";
        }
    }

    // -- Stub exceptions matching FTGO naming conventions --

    static class OrderNotFoundException extends RuntimeException {
        OrderNotFoundException(Long id) {
            super("Order not found with id " + id);
        }
    }

    static class UnsupportedStateTransitionException extends RuntimeException {
        UnsupportedStateTransitionException(String state) {
            super("current state: " + state);
        }
    }

    static class OrderMinimumNotMetException extends RuntimeException {
    }

    static class OptimisticOfflineLockException extends RuntimeException {
    }

    static class CreateOrderRequest {
        @NotBlank
        private String restaurantName;

        @NotNull
        @Min(1)
        private Integer quantity;

        public String getRestaurantName() { return restaurantName; }
        public void setRestaurantName(String restaurantName) { this.restaurantName = restaurantName; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    // -- Tests --

    @Test
    void handleRuntimeException_notFoundException_returns404() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("RESOURCE_NOT_FOUND")))
                .andExpect(jsonPath("$.message", is("Order not found with id 42")))
                .andExpect(jsonPath("$.traceId", is("test-trace-id-001")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    void handleRuntimeException_stateConflict_returns409() throws Exception {
        mockMvc.perform(get("/test/state-conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("STATE_CONFLICT")))
                .andExpect(jsonPath("$.message", is("current state: APPROVED")));
    }

    @Test
    void handleRuntimeException_orderMinimumNotMet_returns422() throws Exception {
        mockMvc.perform(get("/test/order-minimum"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code", is("ORDER_MINIMUM_NOT_MET")));
    }

    @Test
    void handleIllegalArgument_invalidValue_returns400() throws Exception {
        mockMvc.perform(get("/test/illegal-argument"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("INVALID_REQUEST")))
                .andExpect(jsonPath("$.message", is("Invalid parameter value")));
    }

    @Test
    void handleIllegalState_invalidState_returns409() throws Exception {
        mockMvc.perform(get("/test/illegal-state"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("STATE_CONFLICT")));
    }

    @Test
    void handleUnsupportedOperation_notImplemented_returns501() throws Exception {
        mockMvc.perform(get("/test/not-implemented"))
                .andExpect(status().isNotImplemented())
                .andExpect(jsonPath("$.code", is("NOT_IMPLEMENTED")));
    }

    @Test
    void handleConnectException_connectionRefused_returns503() throws Exception {
        mockMvc.perform(get("/test/connect-error"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code", is("SERVICE_UNAVAILABLE")));
    }

    @Test
    void handleRuntimeException_unhandled_returns500WithGenericMessage() throws Exception {
        mockMvc.perform(get("/test/internal-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code", is("INTERNAL_ERROR")))
                .andExpect(jsonPath("$.message", is("An unexpected internal error occurred")));
    }

    @Test
    void handleGenericException_checkedException_returns500() throws Exception {
        mockMvc.perform(get("/test/checked-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code", is("INTERNAL_ERROR")))
                .andExpect(jsonPath("$.traceId", is("test-trace-id-001")));
    }

    @Test
    void handleRuntimeException_optimisticLock_returns409() throws Exception {
        mockMvc.perform(get("/test/optimistic-lock"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("OPTIMISTIC_LOCK_CONFLICT")));
    }

    @Test
    void handleMethodArgumentNotValid_invalidFields_returns400WithDetails() throws Exception {
        String invalidBody = "{ \"restaurantName\": \"\", \"quantity\": -1 }";

        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details", hasSize(2)))
                .andExpect(jsonPath("$.traceId", is("test-trace-id-001")));
    }

    @Test
    void handleHttpMessageNotReadable_malformedJson_returns400() throws Exception {
        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("INVALID_REQUEST")));
    }

    @Test
    void handleHttpRequestMethodNotSupported_deleteOnGetEndpoint_returns405() throws Exception {
        mockMvc.perform(delete("/test/ok"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code", is("METHOD_NOT_ALLOWED")));
    }

    @Test
    void handleRequest_validGetRequest_returns200() throws Exception {
        mockMvc.perform(get("/test/ok"))
                .andExpect(status().isOk());
    }
}
