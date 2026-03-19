package com.ftgo.common.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Standard error response format for all FTGO microservices.
 *
 * <p>Every error response follows this structure regardless of the service
 * or exception type, providing a consistent API contract for clients.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final String errorCode;
    private final String message;
    private final int status;
    private final Instant timestamp;
    private final String path;
    private final String traceId;
    private final List<FieldError> fieldErrors;

    private ErrorResponse(Builder builder) {
        this.errorCode = builder.errorCode;
        this.message = builder.message;
        this.status = builder.status;
        this.timestamp = builder.timestamp;
        this.path = builder.path;
        this.traceId = builder.traceId;
        this.fieldErrors = builder.fieldErrors;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public int getStatus() {
        return status;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getPath() {
        return path;
    }

    public String getTraceId() {
        return traceId;
    }

    public List<FieldError> getFieldErrors() {
        return fieldErrors;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Represents a single field-level validation error.
     */
    public record FieldError(String field, String message, Object rejectedValue) {}

    public static class Builder {
        private String errorCode;
        private String message;
        private int status;
        private Instant timestamp = Instant.now();
        private String path;
        private String traceId;
        private List<FieldError> fieldErrors;

        public Builder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public Builder errorCode(ErrorCode errorCode) {
            this.errorCode = errorCode.getCode();
            this.status = errorCode.getHttpStatus();
            if (this.message == null) {
                this.message = errorCode.getDefaultMessage();
            }
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder status(int status) {
            this.status = status;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        public Builder fieldErrors(List<FieldError> fieldErrors) {
            this.fieldErrors = fieldErrors;
            return this;
        }

        public ErrorResponse build() {
            return new ErrorResponse(this);
        }
    }
}
