package com.ftgo.common.error;

/**
 * Standardized error codes for FTGO microservices.
 *
 * <p>Each code follows the format {@code SERVICE_CATEGORY_DETAIL} and maps to
 * a specific HTTP status code. Services can extend this catalog with domain-specific codes.
 */
public enum ErrorCode {

    // Common errors (FTGO-0xx)
    INTERNAL_ERROR("FTGO-001", "Internal server error", 500),
    VALIDATION_ERROR("FTGO-002", "Validation failed", 400),
    RESOURCE_NOT_FOUND("FTGO-003", "Resource not found", 404),
    METHOD_NOT_ALLOWED("FTGO-004", "Method not allowed", 405),
    CONFLICT("FTGO-005", "Resource conflict", 409),
    UNSUPPORTED_MEDIA_TYPE("FTGO-006", "Unsupported media type", 415),
    BAD_REQUEST("FTGO-007", "Bad request", 400),

    // Authentication/Authorization errors (FTGO-1xx)
    UNAUTHORIZED("FTGO-100", "Authentication required", 401),
    FORBIDDEN("FTGO-101", "Access denied", 403),
    TOKEN_EXPIRED("FTGO-102", "Token expired", 401),
    TOKEN_INVALID("FTGO-103", "Invalid token", 401),

    // Service communication errors (FTGO-2xx)
    SERVICE_UNAVAILABLE("FTGO-200", "Service unavailable", 503),
    SERVICE_TIMEOUT("FTGO-201", "Service timeout", 504),
    DOWNSTREAM_ERROR("FTGO-202", "Downstream service error", 502),

    // Domain errors (FTGO-3xx)
    INVALID_STATE_TRANSITION("FTGO-300", "Invalid state transition", 422),
    BUSINESS_RULE_VIOLATION("FTGO-301", "Business rule violation", 422),
    INSUFFICIENT_FUNDS("FTGO-302", "Insufficient funds", 422);

    private final String code;
    private final String defaultMessage;
    private final int httpStatus;

    ErrorCode(String code, String defaultMessage, int httpStatus) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
