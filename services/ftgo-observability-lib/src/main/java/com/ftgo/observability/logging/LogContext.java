package com.ftgo.observability.logging;

import org.slf4j.MDC;

/**
 * Utility class for programmatic MDC management.
 *
 * <p>Use this class in non-HTTP contexts (message consumers, scheduled tasks, async workers) where
 * the servlet filters ({@link CorrelationIdFilter}, {@link ServiceMdcFilter}) are not active.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * LogContext.setUserId("user-123");
 * LogContext.setRequestId("batch-456");
 * try {
 *     // business logic — all log statements include MDC fields
 * } finally {
 *     LogContext.clear();
 * }
 * }</pre>
 */
public final class LogContext {

    /** MDC key for the authenticated user identifier. */
    public static final String USER_ID_KEY = CorrelationIdFilter.USER_ID_MDC_KEY;

    /** MDC key for the unique request identifier. */
    public static final String REQUEST_ID_KEY = CorrelationIdFilter.REQUEST_ID_MDC_KEY;

    /** MDC key for the correlation identifier (mirrors {@link CorrelationIdFilter}). */
    public static final String CORRELATION_ID_KEY = CorrelationIdFilter.CORRELATION_ID_MDC_KEY;

    /** MDC key for the service name (mirrors {@link ServiceMdcFilter}). */
    public static final String SERVICE_KEY = ServiceMdcFilter.SERVICE_MDC_KEY;

    private LogContext() {
        // utility class
    }

    /** Sets the {@code userId} MDC field. */
    public static void setUserId(String userId) {
        if (userId != null && !userId.isBlank()) {
            MDC.put(USER_ID_KEY, userId);
        }
    }

    /** Sets the {@code requestId} MDC field. */
    public static void setRequestId(String requestId) {
        if (requestId != null && !requestId.isBlank()) {
            MDC.put(REQUEST_ID_KEY, requestId);
        }
    }

    /** Sets the {@code correlationId} MDC field. */
    public static void setCorrelationId(String correlationId) {
        if (correlationId != null && !correlationId.isBlank()) {
            MDC.put(CORRELATION_ID_KEY, correlationId);
        }
    }

    /** Sets the {@code service} MDC field. */
    public static void setServiceName(String serviceName) {
        if (serviceName != null && !serviceName.isBlank()) {
            MDC.put(SERVICE_KEY, serviceName);
        }
    }

    /** Removes all FTGO-managed MDC fields. */
    public static void clear() {
        MDC.remove(USER_ID_KEY);
        MDC.remove(REQUEST_ID_KEY);
        MDC.remove(CORRELATION_ID_KEY);
        MDC.remove(SERVICE_KEY);
        MDC.remove(ServiceMdcFilter.REQUEST_METHOD_MDC_KEY);
        MDC.remove(ServiceMdcFilter.REQUEST_URI_MDC_KEY);
    }

    /** Returns the current {@code userId} from MDC, or {@code null} if not set. */
    public static String getUserId() {
        return MDC.get(USER_ID_KEY);
    }

    /** Returns the current {@code requestId} from MDC, or {@code null} if not set. */
    public static String getRequestId() {
        return MDC.get(REQUEST_ID_KEY);
    }

    /** Returns the current {@code correlationId} from MDC, or {@code null} if not set. */
    public static String getCorrelationId() {
        return MDC.get(CORRELATION_ID_KEY);
    }

    /** Returns the current {@code service} from MDC, or {@code null} if not set. */
    public static String getServiceName() {
        return MDC.get(SERVICE_KEY);
    }
}
