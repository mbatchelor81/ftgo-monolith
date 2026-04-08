package com.ftgo.logging.mdc;

import org.slf4j.MDC;

/**
 * Utility class for managing MDC (Mapped Diagnostic Context) fields across
 * FTGO microservices.
 *
 * <p>Provides type-safe methods for setting and clearing standard MDC fields
 * that are included in all structured log output. Fields set via this class
 * appear in both JSON and plain-text log formats.
 *
 * <p>Standard MDC fields:
 * <ul>
 *   <li>{@code userId} — authenticated user identifier</li>
 *   <li>{@code requestId} — unique per-request identifier</li>
 *   <li>{@code correlationId} — cross-service correlation identifier</li>
 *   <li>{@code serviceName} — name of the current microservice</li>
 * </ul>
 *
 * <p>{@code traceId} and {@code spanId} are managed automatically by
 * Micrometer Tracing (via {@code ftgo-tracing-lib}) and should not be
 * set manually.
 *
 * <p>Usage:
 * <pre>{@code
 * LogContext.setUserId("consumer-42");
 * LogContext.setRequestId(UUID.randomUUID().toString());
 * try {
 *     // ... business logic — all log entries include userId and requestId
 * } finally {
 *     LogContext.clear();
 * }
 * }</pre>
 *
 * <p>For request-scoped MDC management, prefer the {@code CorrelationIdFilter}
 * which automatically sets and clears MDC fields per HTTP request.
 */
public final class LogContext {

    /** MDC key for the authenticated user identifier. */
    public static final String KEY_USER_ID = "userId";

    /** MDC key for the unique request identifier. */
    public static final String KEY_REQUEST_ID = "requestId";

    /** MDC key for the cross-service correlation identifier. */
    public static final String KEY_CORRELATION_ID = "correlationId";

    /** MDC key for the current microservice name. */
    public static final String KEY_SERVICE_NAME = "serviceName";

    /** MDC key for the HTTP request method. */
    public static final String KEY_REQUEST_METHOD = "requestMethod";

    /** MDC key for the HTTP request URI. */
    public static final String KEY_REQUEST_URI = "requestUri";

    private LogContext() {
        // Utility class — no instantiation
    }

    /**
     * Sets the authenticated user ID in the MDC.
     *
     * @param userId the user identifier, or {@code null} to remove the field
     */
    public static void setUserId(String userId) {
        putOrRemove(KEY_USER_ID, userId);
    }

    /**
     * Sets the unique request ID in the MDC.
     *
     * @param requestId the request identifier, or {@code null} to remove the field
     */
    public static void setRequestId(String requestId) {
        putOrRemove(KEY_REQUEST_ID, requestId);
    }

    /**
     * Sets the cross-service correlation ID in the MDC.
     *
     * @param correlationId the correlation identifier, or {@code null} to remove the field
     */
    public static void setCorrelationId(String correlationId) {
        putOrRemove(KEY_CORRELATION_ID, correlationId);
    }

    /**
     * Sets the current microservice name in the MDC.
     *
     * @param serviceName the service name, or {@code null} to remove the field
     */
    public static void setServiceName(String serviceName) {
        putOrRemove(KEY_SERVICE_NAME, serviceName);
    }

    /**
     * Sets the HTTP request method in the MDC.
     *
     * @param method the HTTP method (GET, POST, etc.), or {@code null} to remove
     */
    public static void setRequestMethod(String method) {
        putOrRemove(KEY_REQUEST_METHOD, method);
    }

    /**
     * Sets the HTTP request URI in the MDC.
     *
     * @param uri the request URI, or {@code null} to remove
     */
    public static void setRequestUri(String uri) {
        putOrRemove(KEY_REQUEST_URI, uri);
    }

    /**
     * Returns the current user ID from the MDC, or {@code null} if not set.
     */
    public static String getUserId() {
        return MDC.get(KEY_USER_ID);
    }

    /**
     * Returns the current request ID from the MDC, or {@code null} if not set.
     */
    public static String getRequestId() {
        return MDC.get(KEY_REQUEST_ID);
    }

    /**
     * Returns the current correlation ID from the MDC, or {@code null} if not set.
     */
    public static String getCorrelationId() {
        return MDC.get(KEY_CORRELATION_ID);
    }

    /**
     * Returns the current service name from the MDC, or {@code null} if not set.
     */
    public static String getServiceName() {
        return MDC.get(KEY_SERVICE_NAME);
    }

    /**
     * Clears all FTGO-specific MDC fields.
     *
     * <p>Call this in a {@code finally} block after processing completes to
     * prevent MDC leakage between requests (especially important in thread
     * pools and async contexts).
     */
    public static void clear() {
        MDC.remove(KEY_USER_ID);
        MDC.remove(KEY_REQUEST_ID);
        MDC.remove(KEY_CORRELATION_ID);
        MDC.remove(KEY_SERVICE_NAME);
        MDC.remove(KEY_REQUEST_METHOD);
        MDC.remove(KEY_REQUEST_URI);
    }

    private static void putOrRemove(String key, String value) {
        if (value != null) {
            MDC.put(key, value);
        } else {
            MDC.remove(key);
        }
    }
}
