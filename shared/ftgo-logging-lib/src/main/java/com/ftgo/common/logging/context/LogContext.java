package com.ftgo.common.logging.context;

import org.slf4j.MDC;

/**
 * Utility class for managing MDC (Mapped Diagnostic Context) fields
 * in a consistent way across all FTGO services.
 *
 * Standard fields are set by filters (CorrelationIdMdcFilter, RequestLoggingMdcFilter)
 * and tracing infrastructure (Brave/Micrometer). This class provides methods for
 * application-level context that services set themselves.
 *
 * Usage:
 * <pre>
 * {@code
 * // In a security filter after authentication
 * LogContext.setUserId(authenticatedUser.getId());
 *
 * // In business logic for temporary context
 * LogContext.put("orderId", orderId.toString());
 * try {
 *     // all logs will include orderId
 * } finally {
 *     LogContext.remove("orderId");
 * }
 *
 * // Clean up all custom context (e.g., at end of request)
 * LogContext.clear();
 * }
 * </pre>
 */
public final class LogContext {

    public static final String USER_ID = "userId";
    public static final String CORRELATION_ID = "correlationId";
    public static final String SERVICE_NAME = "serviceName";

    private LogContext() {
        // utility class
    }

    /**
     * Set the authenticated user ID in MDC.
     * Should be called after successful authentication.
     */
    public static void setUserId(String userId) {
        if (userId != null) {
            MDC.put(USER_ID, userId);
        }
    }

    /**
     * Get the current user ID from MDC.
     */
    public static String getUserId() {
        return MDC.get(USER_ID);
    }

    /**
     * Get the current correlation ID from MDC.
     */
    public static String getCorrelationId() {
        return MDC.get(CORRELATION_ID);
    }

    /**
     * Set the service name in MDC.
     * Typically called once at service startup.
     */
    public static void setServiceName(String serviceName) {
        if (serviceName != null) {
            MDC.put(SERVICE_NAME, serviceName);
        }
    }

    /**
     * Put a custom key-value pair into MDC.
     * Remember to call {@link #remove(String)} when the context is no longer needed.
     */
    public static void put(String key, String value) {
        if (key != null && value != null) {
            MDC.put(key, value);
        }
    }

    /**
     * Get a value from MDC by key.
     */
    public static String get(String key) {
        return MDC.get(key);
    }

    /**
     * Remove a key from MDC.
     */
    public static void remove(String key) {
        if (key != null) {
            MDC.remove(key);
        }
    }

    /**
     * Clear all MDC entries. Call this at the end of a request
     * to prevent MDC leaking between requests on the same thread.
     */
    public static void clear() {
        MDC.clear();
    }
}
