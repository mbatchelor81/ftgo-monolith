package com.ftgo.errorhandling.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ErrorResponse}. */
@DisplayName("ErrorResponse")
class ErrorResponseTest {

    @Test
    @DisplayName("default constructor sets timestamp automatically")
    void defaultConstructor_setsTimestamp() {
        ErrorResponse response = new ErrorResponse();
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("parameterized constructor populates all required fields")
    void parameterizedConstructor_populatesAllFields() {
        ErrorResponse response = new ErrorResponse("FTGO-404-001", "Not found", "abc123");

        assertThat(response.getCode()).isEqualTo("FTGO-404-001");
        assertThat(response.getMessage()).isEqualTo("Not found");
        assertThat(response.getTraceId()).isEqualTo("abc123");
        assertThat(response.getTimestamp()).isNotNull();
        assertThat(response.getDetails()).isNull();
    }

    @Test
    @DisplayName("field errors can be attached for validation failures")
    void fieldErrors_canBeAttached() {
        ErrorResponse response = new ErrorResponse("FTGO-400-001", "Validation failed", null);

        List<ErrorResponse.FieldError> fieldErrors =
                List.of(
                        new ErrorResponse.FieldError("name", "must not be blank", ""),
                        new ErrorResponse.FieldError("quantity", "must be positive", -1));
        response.setDetails(fieldErrors);

        assertThat(response.getDetails()).hasSize(2);
        assertThat(response.getDetails().get(0).getField()).isEqualTo("name");
        assertThat(response.getDetails().get(0).getMessage()).isEqualTo("must not be blank");
        assertThat(response.getDetails().get(0).getRejectedValue()).isEqualTo("");
        assertThat(response.getDetails().get(1).getField()).isEqualTo("quantity");
        assertThat(response.getDetails().get(1).getRejectedValue()).isEqualTo(-1);
    }
}
