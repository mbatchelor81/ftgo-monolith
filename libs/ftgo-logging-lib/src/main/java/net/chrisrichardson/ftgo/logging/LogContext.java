package net.chrisrichardson.ftgo.logging;

import org.slf4j.MDC;

import java.util.Map;

/**
 * Utility for managing MDC (Mapped Diagnostic Context) fields in a
 * structured and consistent way across FTGO services.
 *
 * Standard MDC keys used by the FTGO platform:
 * <ul>
 *   <li>{@code userId} — authenticated user identifier</li>
 *   <li>{@code requestId} — unique request identifier (alias for correlationId)</li>
 *   <li>{@code traceId} — distributed trace identifier (set by ftgo-tracing-lib)</li>
 *   <li>{@code spanId} — current span identifier (set by ftgo-tracing-lib)</li>
 *   <li>{@code serviceName} — logical service name (set by ServiceContextFilter)</li>
 *   <li>{@code correlationId} — correlation identifier (set by CorrelationIdFilter)</li>
 * </ul>
 */
public final class LogContext {

    public static final String USER_ID = "userId";
    public static final String REQUEST_ID = "requestId";
    public static final String TRACE_ID = "traceId";
    public static final String SPAN_ID = "spanId";
    public static final String SERVICE_NAME = "serviceName";
    public static final String CORRELATION_ID = "correlationId";
    public static final String ORDER_ID = "orderId";
    public static final String RESTAURANT_ID = "restaurantId";

    private LogContext() {
    }

    public static void putUserId(String userId) {
        MDC.put(USER_ID, userId);
    }

    public static void putRequestId(String requestId) {
        MDC.put(REQUEST_ID, requestId);
    }

    public static void putOrderId(String orderId) {
        MDC.put(ORDER_ID, orderId);
    }

    public static void putRestaurantId(String restaurantId) {
        MDC.put(RESTAURANT_ID, restaurantId);
    }

    public static void put(String key, String value) {
        MDC.put(key, value);
    }

    public static String get(String key) {
        return MDC.get(key);
    }

    public static void remove(String key) {
        MDC.remove(key);
    }

    /**
     * Clears all MDC context. Typically called at the end of a request
     * or async task boundary.
     */
    public static void clear() {
        MDC.clear();
    }

    /**
     * Returns a snapshot of the current MDC context map.
     * Useful for propagating context to async threads.
     */
    public static Map<String, String> snapshot() {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        return contextMap != null ? contextMap : Map.of();
    }

    /**
     * Restores a previously captured MDC context snapshot.
     * Useful for propagating context into async threads.
     */
    public static void restore(Map<String, String> contextMap) {
        if (contextMap != null) {
            MDC.setContextMap(contextMap);
        } else {
            MDC.clear();
        }
    }

    /**
     * Executes a {@link Runnable} with the given MDC context map,
     * restoring the previous context afterward.
     */
    public static Runnable wrap(Runnable task) {
        Map<String, String> contextMap = snapshot();
        return () -> {
            Map<String, String> previous = MDC.getCopyOfContextMap();
            restore(contextMap);
            try {
                task.run();
            } finally {
                restore(previous);
            }
        };
    }
}
