package net.chrisrichardson.ftgo.tracing;

import brave.propagation.Propagation;
import brave.propagation.TraceContext;
import brave.propagation.TraceContextOrSamplingFlags;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class W3CTraceContextPropagationTest {

    private final Propagation<String> propagation =
            W3CTraceContextPropagation.FACTORY.create(Propagation.KeyFactory.STRING);

    @Test
    void injectProducesValidTraceparent() {
        TraceContext context = TraceContext.newBuilder()
                .traceIdHigh(1L)
                .traceId(2L)
                .spanId(3L)
                .sampled(true)
                .build();

        Map<String, String> carrier = new LinkedHashMap<>();
        propagation.injector((Map<String, String> map, String key, String value) -> map.put(key, value))
                .inject(context, carrier);

        String traceparent = carrier.get("traceparent");
        assertThat(traceparent).startsWith("00-");
        assertThat(traceparent).endsWith("-01");
        assertThat(traceparent.split("-")).hasSize(4);
    }

    @Test
    void extractParsesValidTraceparent() {
        String traceparent = "00-0af7651916cd43dd8448eb211c80319c-b7ad6b7169203331-01";
        Map<String, String> carrier = new LinkedHashMap<>();
        carrier.put("traceparent", traceparent);

        TraceContextOrSamplingFlags result =
                propagation.extractor((Map<String, String> map, String key) -> map.get(key))
                        .extract(carrier);

        assertThat(result.context()).isNotNull();
        assertThat(result.context().sampled()).isTrue();
    }

    @Test
    void extractReturnsEmptyForMissingHeader() {
        Map<String, String> carrier = new LinkedHashMap<>();

        TraceContextOrSamplingFlags result =
                propagation.extractor((Map<String, String> map, String key) -> map.get(key))
                        .extract(carrier);

        assertThat(result).isEqualTo(TraceContextOrSamplingFlags.EMPTY);
    }

    @Test
    void extractReturnsEmptyForMalformedHeader() {
        Map<String, String> carrier = new LinkedHashMap<>();
        carrier.put("traceparent", "invalid");

        TraceContextOrSamplingFlags result =
                propagation.extractor((Map<String, String> map, String key) -> map.get(key))
                        .extract(carrier);

        assertThat(result).isEqualTo(TraceContextOrSamplingFlags.EMPTY);
    }

    @Test
    void unsampledFlagIsPropagated() {
        TraceContext context = TraceContext.newBuilder()
                .traceIdHigh(1L)
                .traceId(2L)
                .spanId(3L)
                .sampled(false)
                .build();

        Map<String, String> carrier = new LinkedHashMap<>();
        propagation.injector((Map<String, String> map, String key, String value) -> map.put(key, value))
                .inject(context, carrier);

        assertThat(carrier.get("traceparent")).endsWith("-00");
    }
}
