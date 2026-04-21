package net.chrisrichardson.ftgo.logging;

import org.slf4j.MDC;

import java.util.Map;
import java.util.Objects;

/**
 * Convenience facade over SLF4J's {@link MDC} for the canonical FTGO log
 * fields ({@code userId}, {@code requestId}, {@code correlationId},
 * {@code service}).
 *
 * <p>Prefer the {@code with*} helpers over direct MDC access so field names
 * stay consistent across services and typos fail at compile time.
 *
 * <p>Every {@code set*} method has a matching {@code clear()} /
 * {@link CloseableMdc} pairing so MDC entries never leak between threads
 * when the servlet container reuses a worker.
 *
 * <pre>
 *   try (var ignored = LogContext.withUserId(user.getId())) {
 *       orderService.placeOrder(request);
 *   }
 * </pre>
 */
public final class LogContext {

    private LogContext() {
        // Utility class.
    }

    /** Sets {@link MdcKeys#USER_ID} on the current thread's MDC. */
    public static CloseableMdc withUserId(Object userId) {
        return put(MdcKeys.USER_ID, userId);
    }

    /** Sets {@link MdcKeys#REQUEST_ID} on the current thread's MDC. */
    public static CloseableMdc withRequestId(Object requestId) {
        return put(MdcKeys.REQUEST_ID, requestId);
    }

    /** Sets {@link MdcKeys#CORRELATION_ID} on the current thread's MDC. */
    public static CloseableMdc withCorrelationId(Object correlationId) {
        return put(MdcKeys.CORRELATION_ID, correlationId);
    }

    /** Sets an arbitrary MDC key; prefer the typed helpers above. */
    public static CloseableMdc put(String key, Object value) {
        Objects.requireNonNull(key, "MDC key must not be null");
        String previous = MDC.get(key);
        if (value == null) {
            MDC.remove(key);
        } else {
            MDC.put(key, String.valueOf(value));
        }
        return new CloseableMdc(key, previous);
    }

    /** Snapshot of the current MDC, suitable for passing across threads. */
    public static Map<String, String> snapshot() {
        Map<String, String> copy = MDC.getCopyOfContextMap();
        return copy == null ? Map.of() : Map.copyOf(copy);
    }

    /**
     * Removes all canonical FTGO log keys. Callers typically invoke this
     * in a {@code finally} block when a top-level task finishes.
     */
    public static void clear() {
        MDC.remove(MdcKeys.USER_ID);
        MDC.remove(MdcKeys.REQUEST_ID);
        MDC.remove(MdcKeys.CORRELATION_ID);
    }

    /**
     * Auto-closeable MDC entry that restores the prior value on close,
     * so nested scopes don't clobber each other.
     */
    public static final class CloseableMdc implements AutoCloseable {

        private final String key;
        private final String previousValue;

        private CloseableMdc(String key, String previousValue) {
            this.key = key;
            this.previousValue = previousValue;
        }

        @Override
        public void close() {
            if (previousValue == null) {
                MDC.remove(key);
            } else {
                MDC.put(key, previousValue);
            }
        }
    }
}
