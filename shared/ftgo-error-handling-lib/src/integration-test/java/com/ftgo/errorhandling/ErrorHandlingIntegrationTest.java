package com.ftgo.errorhandling;

import com.ftgo.errorhandling.config.FtgoErrorHandlingAutoConfiguration;
import com.ftgo.errorhandling.exception.ErrorCodes;
import com.ftgo.errorhandling.exception.ResourceNotFoundException;
import com.ftgo.errorhandling.exception.ServiceCommunicationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import net.chrisrichardson.ftgo.common.UnsupportedStateTransitionException;
import net.chrisrichardson.ftgo.domain.OrderMinimumNotMetException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test verifying the full error handling pipeline end-to-end.
 *
 * <p>Spins up a minimal web-layer Spring context with the error handling
 * auto-configuration and a test controller that throws various exceptions.
 */
@SpringBootTest(classes = ErrorHandlingIntegrationTest.TestApplication.class)
@AutoConfigureMockMvc
class ErrorHandlingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // =========================================================================
    // Validation errors — 400
    // =========================================================================

    @Test
    void validationError_returns400_withFieldDetails() throws Exception {
        String invalidBody = """
                {"name": "", "quantity": -1}
                """;

        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(ErrorCodes.VALIDATION_ERROR))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details.length()").value(2))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/test/validate"));
    }

    // =========================================================================
    // Malformed request — 400
    // =========================================================================

    @Test
    void malformedJson_returns400() throws Exception {
        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(ErrorCodes.MALFORMED_REQUEST))
                .andExpect(jsonPath("$.message").value("Malformed request body"));
    }

    // =========================================================================
    // Resource not found — 404
    // =========================================================================

    @Test
    void resourceNotFound_returns404() throws Exception {
        mockMvc.perform(get("/test/not-found/42"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value(ErrorCodes.ORDER_NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Order not found with id: 42"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/test/not-found/42"));
    }

    // =========================================================================
    // State conflict — 409
    // =========================================================================

    @Test
    void stateConflict_returns409() throws Exception {
        mockMvc.perform(post("/test/state-conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value(ErrorCodes.STATE_CONFLICT))
                .andExpect(jsonPath("$.message").value("current state: DELIVERED"));
    }

    // =========================================================================
    // Order minimum not met — 422
    // =========================================================================

    @Test
    void orderMinimumNotMet_returns422() throws Exception {
        mockMvc.perform(post("/test/order-minimum"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errorCode").value(ErrorCodes.ORDER_MINIMUM_NOT_MET))
                .andExpect(jsonPath("$.message").exists());
    }

    // =========================================================================
    // Service communication failure — 502
    // =========================================================================

    @Test
    void serviceCommunicationFailure_returns502() throws Exception {
        mockMvc.perform(get("/test/service-error"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.errorCode").value(ErrorCodes.SERVICE_COMMUNICATION_ERROR))
                .andExpect(jsonPath("$.message").value("Failed to communicate with downstream service: consumer-service"));
    }

    // =========================================================================
    // Unhandled exception — 500 (no stack trace leak)
    // =========================================================================

    @Test
    void unhandledException_returns500_withoutStackTrace() throws Exception {
        mockMvc.perform(get("/test/internal-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value(ErrorCodes.INTERNAL_ERROR))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // =========================================================================
    // Method not allowed — 405
    // =========================================================================

    @Test
    void methodNotAllowed_returns405() throws Exception {
        mockMvc.perform(get("/test/validate"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.errorCode").value(ErrorCodes.METHOD_NOT_ALLOWED));
    }

    // =========================================================================
    // Test application context
    // =========================================================================

    @Configuration
    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class,
            SecurityAutoConfiguration.class
    })
    @Import(FtgoErrorHandlingAutoConfiguration.class)
    static class TestApplication {
    }

    @RestController
    @RequestMapping("/test")
    static class TestController {

        @PostMapping("/validate")
        public void validate(@Valid @RequestBody TestRequest request) {
            // no-op; validation is handled by @Valid
        }

        @GetMapping("/not-found/{id}")
        public void notFound(@PathVariable long id) {
            throw new ResourceNotFoundException("Order", id, ErrorCodes.ORDER_NOT_FOUND);
        }

        @PostMapping("/state-conflict")
        public void stateConflict() {
            throw new UnsupportedStateTransitionException(TestOrderState.DELIVERED);
        }

        @PostMapping("/order-minimum")
        public void orderMinimum() {
            throw new OrderMinimumNotMetException();
        }

        @GetMapping("/service-error")
        public void serviceError() {
            throw new ServiceCommunicationException("consumer-service", "Connection refused");
        }

        @GetMapping("/internal-error")
        public void internalError() {
            throw new RuntimeException("Sensitive DB connection string leaked");
        }
    }

    static class TestRequest {
        @NotBlank(message = "Name must not be blank")
        private String name;

        @NotNull
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    enum TestOrderState {
        APPROVED, DELIVERED
    }
}
