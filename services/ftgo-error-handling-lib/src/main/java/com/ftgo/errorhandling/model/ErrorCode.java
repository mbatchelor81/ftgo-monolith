package com.ftgo.errorhandling.model;

/**
 * Standardized error codes for all FTGO services.
 *
 * <p>Each code is a short, machine-readable identifier that categorizes the error. Clients can
 * switch on these codes to drive programmatic behaviour without parsing human-readable messages.
 */
public enum ErrorCode {

    // --- Validation (400) ---
    VALIDATION_FAILED("FTGO-400-001", "Request validation failed"),
    INVALID_REQUEST_BODY("FTGO-400-002", "Request body is malformed or unreadable"),
    MISSING_REQUIRED_FIELD("FTGO-400-003", "A required field is missing"),
    TYPE_MISMATCH("FTGO-400-004", "A request parameter has an invalid type"),
    MISSING_REQUEST_PARAMETER("FTGO-400-005", "A required request parameter is missing"),

    // --- Authentication (401) ---
    AUTHENTICATION_REQUIRED("FTGO-401-001", "Authentication is required"),

    // --- Authorization (403) ---
    ACCESS_DENIED("FTGO-403-001", "Access to the requested resource is denied"),

    // --- Not Found (404) ---
    RESOURCE_NOT_FOUND("FTGO-404-001", "The requested resource was not found"),
    ORDER_NOT_FOUND("FTGO-404-002", "The specified order was not found"),
    CONSUMER_NOT_FOUND("FTGO-404-003", "The specified consumer was not found"),
    RESTAURANT_NOT_FOUND("FTGO-404-004", "The specified restaurant was not found"),
    COURIER_NOT_FOUND("FTGO-404-005", "The specified courier was not found"),

    // --- Method Not Allowed (405) ---
    METHOD_NOT_ALLOWED("FTGO-405-001", "The HTTP method is not allowed for this endpoint"),

    // --- Conflict (409) ---
    STATE_TRANSITION_CONFLICT("FTGO-409-001", "The requested state transition is not valid"),
    RESOURCE_CONFLICT("FTGO-409-002", "The request conflicts with the current resource state"),

    // --- Unprocessable Entity (422) ---
    ORDER_MINIMUM_NOT_MET("FTGO-422-001", "The order total does not meet the restaurant minimum"),
    BUSINESS_RULE_VIOLATION("FTGO-422-002", "A business rule prevented the operation"),

    // --- Internal Server Error (500) ---
    INTERNAL_ERROR("FTGO-500-001", "An unexpected internal error occurred"),

    // --- Service Communication (502/503) ---
    SERVICE_COMMUNICATION_FAILURE(
            "FTGO-502-001", "Failed to communicate with a downstream service"),
    SERVICE_UNAVAILABLE("FTGO-503-001", "A required service is temporarily unavailable");

    private final String code;
    private final String defaultMessage;

    ErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    /** Returns the machine-readable error code (e.g. {@code FTGO-400-001}). */
    public String getCode() {
        return code;
    }

    /** Returns the default human-readable message for this error code. */
    public String getDefaultMessage() {
        return defaultMessage;
    }
}
