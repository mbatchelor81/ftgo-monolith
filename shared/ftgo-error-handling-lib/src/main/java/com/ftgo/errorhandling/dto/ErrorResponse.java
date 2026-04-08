package com.ftgo.errorhandling.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Standard error response format for all FTGO microservices.
 *
 * <p>Every error returned by any FTGO service conforms to this structure,
 * enabling consistent client-side error handling and debugging.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final String errorCode;
    private final String message;
    private final List<FieldError> details;
    private final Instant timestamp;
    private final String traceId;
    private final String path;

    private ErrorResponse(Builder builder) {
        this.errorCode = builder.errorCode;
        this.message = builder.message;
        this.details = builder.details;
        this.timestamp = builder.timestamp;
        this.traceId = builder.traceId;
        this.path = builder.path;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public List<FieldError> getDetails() {
        return details;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getPath() {
        return path;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Represents a single field-level validation error.
     */
    public static class FieldError {

        private final String field;
        private final String message;
        private final Object rejectedValue;

        public FieldError(String field, String message, Object rejectedValue) {
            this.field = field;
            this.message = message;
            this.rejectedValue = rejectedValue;
        }

        public String getField() {
            return field;
        }

        public String getMessage() {
            return message;
        }

        public Object getRejectedValue() {
            return rejectedValue;
        }
    }

    public static class Builder {

        private String errorCode;
        private String message;
        private List<FieldError> details;
        private Instant timestamp = Instant.now();
        private String traceId;
        private String path;

        public Builder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder details(List<FieldError> details) {
            this.details = details;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public ErrorResponse build() {
            return new ErrorResponse(this);
        }
    }
}
