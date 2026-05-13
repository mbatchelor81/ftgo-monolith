package net.chrisrichardson.ftgo.errorhandling;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorResponseTest {

    @Test
    void of_withCodeAndMessage_populatesAllFields() {
        ErrorResponse response = ErrorResponse.of(ErrorCode.RESOURCE_NOT_FOUND,
                "Order not found with id 42", "trace-123");

        assertThat(response.getCode()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(response.getMessage()).isEqualTo("Order not found with id 42");
        assertThat(response.getTraceId()).isEqualTo("trace-123");
        assertThat(response.getTimestamp()).isNotNull();
        assertThat(response.getDetails()).isNull();
    }

    @Test
    void of_withCodeOnly_usesDefaultMessage() {
        ErrorResponse response = ErrorResponse.of(ErrorCode.INTERNAL_ERROR, "trace-456");

        assertThat(response.getCode()).isEqualTo("INTERNAL_ERROR");
        assertThat(response.getMessage()).isEqualTo("An unexpected internal error occurred");
    }

    @Test
    void withValidationErrors_includesFieldDetails() {
        List<ValidationErrorDetail> details = List.of(
                new ValidationErrorDetail("quantity", -1, "must be greater than 0"),
                new ValidationErrorDetail("name", null, "must not be blank")
        );

        ErrorResponse response = ErrorResponse.withValidationErrors(
                "Validation failed", details, "trace-789");

        assertThat(response.getCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getMessage()).isEqualTo("Validation failed");
        assertThat(response.getDetails()).hasSize(2);
        assertThat(response.getDetails().get(0).getField()).isEqualTo("quantity");
        assertThat(response.getDetails().get(0).getRejectedValue()).isEqualTo(-1);
        assertThat(response.getDetails().get(0).getMessage()).isEqualTo("must be greater than 0");
    }

    @Test
    void of_withNullTraceId_allowsNullTrace() {
        ErrorResponse response = ErrorResponse.of(ErrorCode.INTERNAL_ERROR, null);

        assertThat(response.getTraceId()).isNull();
    }

    @Test
    void of_withNullMessage_fallsBackToDefault() {
        ErrorResponse response = ErrorResponse.of(ErrorCode.STATE_CONFLICT, null, "trace-abc");

        assertThat(response.getMessage()).isEqualTo("The operation conflicts with the current resource state");
    }
}
