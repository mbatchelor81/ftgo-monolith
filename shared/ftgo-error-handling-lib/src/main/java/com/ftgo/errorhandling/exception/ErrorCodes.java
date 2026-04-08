package com.ftgo.errorhandling.exception;

/**
 * Centralized error code constants for all FTGO microservices.
 *
 * <p>Error codes follow the pattern: {@code DOMAIN_CATEGORY}
 * where DOMAIN identifies the bounded context and CATEGORY describes
 * the error type. Generic codes use the {@code FTGO_} prefix.
 *
 * @see <a href="docs/error-code-catalog.md">Error Code Catalog</a>
 */
public final class ErrorCodes {

    private ErrorCodes() {
        // Utility class — no instantiation
    }

    // =========================================================================
    // Generic / Cross-cutting error codes
    // =========================================================================

    /** Request body or parameters failed bean validation. */
    public static final String VALIDATION_ERROR = "FTGO_VALIDATION_ERROR";

    /** The requested resource was not found. */
    public static final String RESOURCE_NOT_FOUND = "FTGO_RESOURCE_NOT_FOUND";

    /** The operation conflicts with the current state of the resource. */
    public static final String STATE_CONFLICT = "FTGO_STATE_CONFLICT";

    /** The request is semantically invalid (business rule violation). */
    public static final String UNPROCESSABLE_ENTITY = "FTGO_UNPROCESSABLE_ENTITY";

    /** Authentication is required or credentials are invalid. */
    public static final String UNAUTHORIZED = "FTGO_UNAUTHORIZED";

    /** The authenticated user lacks permission for the operation. */
    public static final String FORBIDDEN = "FTGO_FORBIDDEN";

    /** An unexpected internal error occurred. */
    public static final String INTERNAL_ERROR = "FTGO_INTERNAL_ERROR";

    /** A downstream service call failed. */
    public static final String SERVICE_COMMUNICATION_ERROR = "FTGO_SERVICE_COMMUNICATION_ERROR";

    /** The request method is not supported for the endpoint. */
    public static final String METHOD_NOT_ALLOWED = "FTGO_METHOD_NOT_ALLOWED";

    /** The request content type is not supported. */
    public static final String UNSUPPORTED_MEDIA_TYPE = "FTGO_UNSUPPORTED_MEDIA_TYPE";

    /** The request body is malformed or unreadable. */
    public static final String MALFORMED_REQUEST = "FTGO_MALFORMED_REQUEST";

    // =========================================================================
    // Order domain error codes
    // =========================================================================

    /** The order cannot transition to the requested state. */
    public static final String ORDER_STATE_CONFLICT = "ORDER_STATE_CONFLICT";

    /** The order total does not meet the restaurant's minimum. */
    public static final String ORDER_MINIMUM_NOT_MET = "ORDER_MINIMUM_NOT_MET";

    /** The requested order was not found. */
    public static final String ORDER_NOT_FOUND = "ORDER_NOT_FOUND";

    // =========================================================================
    // Consumer domain error codes
    // =========================================================================

    /** The requested consumer was not found. */
    public static final String CONSUMER_NOT_FOUND = "CONSUMER_NOT_FOUND";

    // =========================================================================
    // Restaurant domain error codes
    // =========================================================================

    /** The requested restaurant was not found. */
    public static final String RESTAURANT_NOT_FOUND = "RESTAURANT_NOT_FOUND";

    // =========================================================================
    // Courier domain error codes
    // =========================================================================

    /** The requested courier was not found. */
    public static final String COURIER_NOT_FOUND = "COURIER_NOT_FOUND";
}
