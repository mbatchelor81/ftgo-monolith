package com.ftgo.errorhandling.handler;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ftgo.common.UnsupportedStateTransitionException;
import com.ftgo.domain.OrderMinimumNotMetException;
import com.ftgo.errorhandling.exception.BusinessRuleException;
import com.ftgo.errorhandling.exception.ResourceNotFoundException;
import com.ftgo.errorhandling.exception.ServiceCommunicationException;
import com.ftgo.errorhandling.model.ErrorCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Integration tests for {@link GlobalExceptionHandler}.
 *
 * <p>Uses a minimal Spring MVC context with a stub controller that throws the various exceptions
 * handled by the global handler.
 */
@WebMvcTest(
        controllers = GlobalExceptionHandlerTest.StubController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class)
@Import(GlobalExceptionHandlerTest.TestConfig.class)
@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    @Autowired private MockMvc mockMvc;

    @TestConfiguration
    static class TestConfig {

        @Bean
        public io.micrometer.tracing.Tracer tracer() {
            return io.micrometer.tracing.Tracer.NOOP;
        }

        @Bean
        public GlobalExceptionHandler globalExceptionHandler(io.micrometer.tracing.Tracer tracer) {
            return new GlobalExceptionHandler(tracer);
        }
    }

    /** Stub controller that throws specific exceptions for testing. */
    @RestController
    @RequestMapping("/test")
    static class StubController {

        @GetMapping("/not-found")
        public void notFound() {
            throw new ResourceNotFoundException(ErrorCode.ORDER_NOT_FOUND, "Order 42 not found");
        }

        @GetMapping("/state-conflict")
        public void stateConflict() {
            throw new UnsupportedStateTransitionException(TestState.DELIVERED);
        }

        @GetMapping("/order-minimum")
        public void orderMinimum() {
            throw new OrderMinimumNotMetException();
        }

        @GetMapping("/business-rule")
        public void businessRule() {
            throw new BusinessRuleException(
                    ErrorCode.BUSINESS_RULE_VIOLATION, "Cannot cancel a delivered order");
        }

        @GetMapping("/service-failure")
        public void serviceFailure() {
            throw new ServiceCommunicationException(
                    "restaurant-service",
                    "Connection refused to restaurant-service",
                    new RuntimeException("connect refused"));
        }

        @GetMapping("/internal-error")
        public void internalError() {
            throw new RuntimeException("unexpected NPE");
        }

        @GetMapping("/authentication-required")
        public void authenticationRequired() {
            throw new BadCredentialsException("Bad credentials");
        }

        @GetMapping("/access-denied")
        public void accessDenied() {
            throw new AccessDeniedException("Access is denied");
        }

        @PostMapping("/validate")
        public void validate(@Valid @RequestBody ValidationDto body) {
            // Bean Validation will trigger before this body executes
        }

        enum TestState {
            DELIVERED
        }

        /** Simple DTO for validation testing. */
        static class ValidationDto {
            @NotBlank(message = "name must not be blank")
            private String name;

            @Positive(message = "quantity must be positive")
            private int quantity;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public int getQuantity() {
                return quantity;
            }

            public void setQuantity(int quantity) {
                this.quantity = quantity;
            }
        }
    }

    // ---- Validation (400) ----

    @Nested
    @DisplayName("Validation errors (400)")
    class ValidationErrors {

        @Test
        @DisplayName("returns 400 with field-level details for invalid request body")
        void validationFailure_returns400WithFieldDetails() throws Exception {
            mockMvc.perform(
                            post("/test/validate")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"name\": \"\", \"quantity\": -1}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("FTGO-400-001"))
                    .andExpect(jsonPath("$.message").value("Request validation failed"))
                    .andExpect(jsonPath("$.timestamp").value(notNullValue()))
                    .andExpect(jsonPath("$.details").isArray())
                    .andExpect(jsonPath("$.details", hasSize(2)));
        }

        @Test
        @DisplayName("returns 400 for malformed JSON body")
        void malformedBody_returns400() throws Exception {
            mockMvc.perform(
                            post("/test/validate")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("not-json"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("FTGO-400-002"))
                    .andExpect(jsonPath("$.timestamp").value(notNullValue()));
        }
    }

    // ---- Not Found (404) ----

    @Nested
    @DisplayName("Not Found errors (404)")
    class NotFoundErrors {

        @Test
        @DisplayName("returns 404 with error code for ResourceNotFoundException")
        void resourceNotFound_returns404() throws Exception {
            mockMvc.perform(get("/test/not-found"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("FTGO-404-002"))
                    .andExpect(jsonPath("$.message").value("Order 42 not found"))
                    .andExpect(jsonPath("$.timestamp").value(notNullValue()));
        }
    }

    // ---- Conflict (409) ----

    @Nested
    @DisplayName("Conflict errors (409)")
    class ConflictErrors {

        @Test
        @DisplayName("returns 409 for UnsupportedStateTransitionException")
        void stateTransitionConflict_returns409() throws Exception {
            mockMvc.perform(get("/test/state-conflict"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("FTGO-409-001"))
                    .andExpect(jsonPath("$.message").value(startsWith("current state:")))
                    .andExpect(jsonPath("$.timestamp").value(notNullValue()));
        }
    }

    // ---- Unprocessable Entity (422) ----

    @Nested
    @DisplayName("Unprocessable Entity errors (422)")
    class UnprocessableEntityErrors {

        @Test
        @DisplayName("returns 422 for OrderMinimumNotMetException")
        void orderMinimumNotMet_returns422() throws Exception {
            mockMvc.perform(get("/test/order-minimum"))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.code").value("FTGO-422-001"))
                    .andExpect(jsonPath("$.timestamp").value(notNullValue()));
        }

        @Test
        @DisplayName("returns 422 for BusinessRuleException")
        void businessRule_returns422() throws Exception {
            mockMvc.perform(get("/test/business-rule"))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.code").value("FTGO-422-002"))
                    .andExpect(jsonPath("$.message").value("Cannot cancel a delivered order"))
                    .andExpect(jsonPath("$.timestamp").value(notNullValue()));
        }
    }

    // ---- Service Communication (502) ----

    @Nested
    @DisplayName("Service communication errors (502)")
    class ServiceCommunicationErrors {

        @Test
        @DisplayName("returns 502 for ServiceCommunicationException")
        void serviceCommunication_returns502() throws Exception {
            mockMvc.perform(get("/test/service-failure"))
                    .andExpect(status().isBadGateway())
                    .andExpect(jsonPath("$.code").value("FTGO-502-001"))
                    .andExpect(
                            jsonPath("$.message").value("Connection refused to restaurant-service"))
                    .andExpect(jsonPath("$.timestamp").value(notNullValue()));
        }
    }

    // ---- Internal Server Error (500) ----

    @Nested
    @DisplayName("Internal server errors (500)")
    class InternalServerErrors {

        @Test
        @DisplayName("returns 500 without stack trace for unhandled exceptions")
        void uncaughtException_returns500WithoutStackTrace() throws Exception {
            mockMvc.perform(get("/test/internal-error"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("FTGO-500-001"))
                    .andExpect(jsonPath("$.message").value("An unexpected internal error occurred"))
                    .andExpect(jsonPath("$.timestamp").value(notNullValue()));
        }
    }

    // ---- Authentication (401) ----

    @Nested
    @DisplayName("Authentication errors (401)")
    class AuthenticationErrors {

        @Test
        @DisplayName("returns 401 for AuthenticationException")
        void authenticationException_returns401() throws Exception {
            mockMvc.perform(get("/test/authentication-required"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("FTGO-401-001"))
                    .andExpect(jsonPath("$.message").value("Authentication is required"))
                    .andExpect(jsonPath("$.timestamp").value(notNullValue()));
        }
    }

    // ---- Forbidden (403) ----

    @Nested
    @DisplayName("Access denied errors (403)")
    class AccessDeniedErrors {

        @Test
        @DisplayName("returns 403 for AccessDeniedException")
        void accessDenied_returns403() throws Exception {
            mockMvc.perform(get("/test/access-denied"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FTGO-403-001"))
                    .andExpect(
                            jsonPath("$.message")
                                    .value("Access to the requested resource is denied"))
                    .andExpect(jsonPath("$.timestamp").value(notNullValue()));
        }
    }

    // ---- Error response structure ----

    @Nested
    @DisplayName("Error response structure")
    class ErrorResponseStructure {

        @Test
        @DisplayName("never leaks stack traces in error responses")
        void errorResponse_neverContainsStackTrace() throws Exception {
            String body =
                    mockMvc.perform(get("/test/internal-error"))
                            .andReturn()
                            .getResponse()
                            .getContentAsString();
            // Stack traces contain "at " prefixed lines — should never appear
            org.assertj.core.api.Assertions.assertThat(body).doesNotContain("at com.");
            org.assertj.core.api.Assertions.assertThat(body).doesNotContain("java.lang.");
        }

        @Test
        @DisplayName("all error responses include timestamp")
        void errorResponse_alwaysIncludesTimestamp() throws Exception {
            mockMvc.perform(get("/test/not-found"))
                    .andExpect(jsonPath("$.timestamp").value(notNullValue()));
        }
    }
}
