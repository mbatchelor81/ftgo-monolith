package com.ftgo.openapi.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ApiErrorResponseTest {

    @Test
    void constructorSetsFieldsAndTimestamp() {
        ApiErrorResponse response = new ApiErrorResponse(400, "Bad Request", "Validation failed", "/api/v1/orders");

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getError()).isEqualTo("Bad Request");
        assertThat(response.getMessage()).isEqualTo("Validation failed");
        assertThat(response.getPath()).isEqualTo("/api/v1/orders");
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    void fieldErrorsCanBeAttached() {
        ApiErrorResponse response = new ApiErrorResponse(422, "Unprocessable Entity", "Validation failed", "/api/v1/consumers");
        response.setFieldErrors(List.of(
                new ApiErrorResponse.FieldError("email", "must not be blank", ""),
                new ApiErrorResponse.FieldError("name", "must not be null", null)
        ));

        assertThat(response.getFieldErrors()).hasSize(2);
        assertThat(response.getFieldErrors().get(0).getField()).isEqualTo("email");
    }
}
