package net.chrisrichardson.ftgo.logging;

/**
 * Shared SLF4J MDC keys used by every FTGO service.
 *
 * <p>These constants keep log field names consistent across services so
 * downstream tooling (Kibana dashboards, Elasticsearch queries, Fluentd
 * filters) can rely on a single schema.
 */
public final class MdcKeys {

    /** Correlation ID that spans an end-to-end request across services. */
    public static final String CORRELATION_ID = "correlationId";

    /** Per-service request ID, set by the edge filter. */
    public static final String REQUEST_ID = "requestId";

    /** Logical service name (typically matches spring.application.name). */
    public static final String SERVICE = "service";

    /** Authenticated user identifier, populated by security filters. */
    public static final String USER_ID = "userId";

    /**
     * Distributed-tracing trace identifier. Populated automatically by
     * Micrometer Tracing (Brave bridge); listed here so downstream code
     * can reference the key without string literals.
     */
    public static final String TRACE_ID = "traceId";

    /**
     * Distributed-tracing span identifier. Populated automatically by
     * Micrometer Tracing (Brave bridge).
     */
    public static final String SPAN_ID = "spanId";

    private MdcKeys() {
        // Utility class.
    }
}
