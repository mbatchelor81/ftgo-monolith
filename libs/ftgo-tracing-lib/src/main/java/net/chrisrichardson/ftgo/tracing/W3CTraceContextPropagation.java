package net.chrisrichardson.ftgo.tracing;

import brave.propagation.Propagation;
import brave.propagation.TraceContext;
import brave.propagation.TraceContextOrSamplingFlags;

import java.util.Arrays;
import java.util.List;

/**
 * W3C Trace Context propagation implementation for Brave.
 *
 * Propagates trace context using the standard W3C headers:
 * <ul>
 *   <li>{@code traceparent} — version, trace-id, parent-id, trace-flags</li>
 *   <li>{@code tracestate}  — vendor-specific key-value pairs (passed through)</li>
 * </ul>
 *
 * @see <a href="https://www.w3.org/TR/trace-context/">W3C Trace Context</a>
 */
public final class W3CTraceContextPropagation {

    static final String TRACEPARENT = "traceparent";
    static final String TRACESTATE = "tracestate";
    private static final String VERSION = "00";
    private static final List<String> FIELDS = Arrays.asList(TRACEPARENT, TRACESTATE);

    public static final Propagation.Factory FACTORY = new Propagation.Factory() {
        @Override
        public <K> Propagation<K> create(Propagation.KeyFactory<K> keyFactory) {
            return new W3CPropagation<>(keyFactory);
        }
    };

    private W3CTraceContextPropagation() {
    }

    private static final class W3CPropagation<K> implements Propagation<K> {

        private final K traceparentKey;
        private final K tracestateKey;

        W3CPropagation(KeyFactory<K> keyFactory) {
            this.traceparentKey = keyFactory.create(TRACEPARENT);
            this.tracestateKey = keyFactory.create(TRACESTATE);
        }

        @Override
        public List<K> keys() {
            return Arrays.asList(traceparentKey, tracestateKey);
        }

        @Override
        public <R> TraceContext.Injector<R> injector(Setter<R, K> setter) {
            return (context, request) -> {
                String traceId = padLeft(context.traceIdString(), 32);
                String spanId = context.spanIdString();
                String flags = context.sampled() != null && context.sampled() ? "01" : "00";
                setter.put(request, traceparentKey,
                        VERSION + "-" + traceId + "-" + spanId + "-" + flags);
            };
        }

        @Override
        public <R> TraceContext.Extractor<R> extractor(Getter<R, K> getter) {
            return request -> {
                String traceparent = getter.get(request, traceparentKey);
                if (traceparent == null) {
                    return TraceContextOrSamplingFlags.EMPTY;
                }
                return parseTraceparent(traceparent);
            };
        }

        private TraceContextOrSamplingFlags parseTraceparent(String header) {
            String[] parts = header.split("-");
            if (parts.length < 4) {
                return TraceContextOrSamplingFlags.EMPTY;
            }
            try {
                long traceIdHigh = hexToLong(parts[1].substring(0, 16));
                long traceId = hexToLong(parts[1].substring(16));
                long spanId = hexToLong(parts[2]);
                boolean sampled = "01".equals(parts[3]);

                TraceContext context = TraceContext.newBuilder()
                        .traceIdHigh(traceIdHigh)
                        .traceId(traceId)
                        .spanId(spanId)
                        .sampled(sampled)
                        .build();
                return TraceContextOrSamplingFlags.create(context);
            } catch (RuntimeException e) {
                return TraceContextOrSamplingFlags.EMPTY;
            }
        }
    }

    private static String padLeft(String value, int length) {
        if (value.length() >= length) {
            return value;
        }
        StringBuilder sb = new StringBuilder(length);
        for (int i = value.length(); i < length; i++) {
            sb.append('0');
        }
        sb.append(value);
        return sb.toString();
    }

    private static long hexToLong(String hex) {
        return Long.parseUnsignedLong(hex, 16);
    }
}
