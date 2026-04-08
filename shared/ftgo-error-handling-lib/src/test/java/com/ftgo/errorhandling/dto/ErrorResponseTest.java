package com.ftgo.errorhandling.dto;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorResponseTest {

    @Test
    void builder_createsCompleteErrorResponse() {
        // Arrange
        Instant now = Instant.now();
        List<ErrorResponse.FieldError> details = List.of(
                new ErrorResponse.FieldError("name", "must not be blank", null));

        // Act
        ErrorResponse response = ErrorResponse.builder()
                .errorCode("FTGO_VALIDATION_ERROR")
                .message("Validation failed")
                .details(details)
                .timestamp(now)
                .traceId("abc123")
                .path("/api/v1/orders")
                .build();

        // Assert
        assertThat(response.getErrorCode()).isEqualTo("FTGO_VALIDATION_ERROR");
        assertThat(response.getMessage()).isEqualTo("Validation failed");
        assertThat(response.getDetails()).hasSize(1);
        assertThat(response.getDetails().get(0).getField()).isEqualTo("name");
        assertThat(response.getDetails().get(0).getMessage()).isEqualTo("must not be blank");
        assertThat(response.getDetails().get(0).getRejectedValue()).isNull();
        assertThat(response.getTimestamp()).isEqualTo(now);
        assertThat(response.getTraceId()).isEqualTo("abc123");
        assertThat(response.getPath()).isEqualTo("/api/v1/orders");
    }

    @Test
    void builder_defaultsTimestampToNow() {
        // Act
        Instant before = Instant.now();
        ErrorResponse response = ErrorResponse.builder()
                .errorCode("FTGO_INTERNAL_ERROR")
                .message("test")
                .build();
        Instant after = Instant.now();

        // Assert
        assertThat(response.getTimestamp()).isBetween(before, after);
    }

    @Test
    void builder_nullDetailsAreOmitted() {
        // Act
        ErrorResponse response = ErrorResponse.builder()
                .errorCode("FTGO_INTERNAL_ERROR")
                .message("An unexpected error occurred")
                .build();

        // Assert
        assertThat(response.getDetails()).isNull();
    }
}
