package com.ftgo.openapi.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

/**
 * Standard error response format for all FTGO REST APIs.
 *
 * <p>Follows the Problem Details pattern (RFC 9457) adapted for FTGO services.
 * All error responses across microservices should use this format for consistency.</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard error response for FTGO APIs")
public class ApiErrorResponse {

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "Short error classification", example = "Bad Request")
    private String error;

    @Schema(description = "Human-readable error message", example = "Validation failed")
    private String message;

    @Schema(description = "API path that produced the error", example = "/api/v1/orders/123")
    private String path;

    @Schema(description = "ISO-8601 timestamp of the error", example = "2024-01-15T10:30:00Z")
    private Instant timestamp;

    @Schema(description = "List of field-level validation errors, if applicable")
    private List<FieldError> fieldErrors;

    public ApiErrorResponse() {
        this.timestamp = Instant.now();
    }

    public ApiErrorResponse(int status, String error, String message, String path) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.timestamp = Instant.now();
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public List<FieldError> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(List<FieldError> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }

    /**
     * Represents a single field-level validation error.
     */
    @Schema(description = "Field-level validation error detail")
    public static class FieldError {

        @Schema(description = "Name of the field that failed validation", example = "email")
        private String field;

        @Schema(description = "Validation error message", example = "must not be blank")
        private String message;

        @Schema(description = "The rejected value", example = "")
        private Object rejectedValue;

        public FieldError() {
        }

        public FieldError(String field, String message, Object rejectedValue) {
            this.field = field;
            this.message = message;
            this.rejectedValue = rejectedValue;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Object getRejectedValue() {
            return rejectedValue;
        }

        public void setRejectedValue(Object rejectedValue) {
            this.rejectedValue = rejectedValue;
        }
    }
}
