package net.chrisrichardson.ftgo.errorhandling;

/**
 * Standardized error codes for all FTGO platform services.
 * Each code maps to a fixed HTTP status and a human-readable description.
 *
 * @see ErrorResponse
 * @see GlobalExceptionHandler
 */
public enum ErrorCode {

    // --- 400 Bad Request ---
    VALIDATION_ERROR(400, "One or more fields failed validation"),
    INVALID_REQUEST(400, "The request body is malformed or unreadable"),

    // --- 404 Not Found ---
    RESOURCE_NOT_FOUND(404, "The requested resource does not exist"),

    // --- 405 Method Not Allowed ---
    METHOD_NOT_ALLOWED(405, "The HTTP method is not supported for this endpoint"),

    // --- 409 Conflict ---
    STATE_CONFLICT(409, "The operation conflicts with the current resource state"),
    OPTIMISTIC_LOCK_CONFLICT(409, "The resource was modified by another request"),

    // --- 415 Unsupported Media Type ---
    UNSUPPORTED_MEDIA_TYPE(415, "The request content type is not supported"),

    // --- 422 Unprocessable Entity ---
    BUSINESS_RULE_VIOLATION(422, "A business rule prevented the operation"),
    ORDER_MINIMUM_NOT_MET(422, "The order total does not meet the restaurant minimum"),
    CONSUMER_VERIFICATION_FAILED(422, "Consumer verification failed"),

    // --- 500 Internal Server Error ---
    INTERNAL_ERROR(500, "An unexpected internal error occurred"),

    // --- 501 Not Implemented ---
    NOT_IMPLEMENTED(501, "This operation is not yet implemented"),

    // --- 503 Service Unavailable ---
    SERVICE_UNAVAILABLE(503, "A downstream service is temporarily unavailable");

    private final int httpStatus;
    private final String defaultMessage;

    ErrorCode(int httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
