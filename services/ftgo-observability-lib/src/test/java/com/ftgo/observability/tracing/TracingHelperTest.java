package com.ftgo.observability.tracing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.test.simple.SimpleSpan;
import io.micrometer.tracing.test.simple.SimpleTracer;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TracingHelperTest {

    private SimpleTracer tracer;
    private TracingHelper tracingHelper;

    @BeforeEach
    void setUp() {
        tracer = new SimpleTracer();
        tracingHelper = new TracingHelper(tracer);
    }

    private List<SimpleSpan> spans() {
        return new ArrayList<>(tracer.getSpans());
    }

    @Test
    void executeInSpan_runnable_createsAndClosesSpan() {
        tracingHelper.executeInSpan("test-operation", () -> {});

        assertThat(spans()).hasSize(1);
        assertThat(spans().get(0).getName()).isEqualTo("test-operation");
    }

    @Test
    void executeInSpan_supplier_returnsValueAndCreatesSpan() {
        String result = tracingHelper.executeInSpan("test-supplier", () -> "hello");

        assertThat(result).isEqualTo("hello");
        assertThat(spans()).hasSize(1);
        assertThat(spans().get(0).getName()).isEqualTo("test-supplier");
    }

    @Test
    void executeInSpan_runnableWithTag_createsSpanWithTag() {
        tracingHelper.executeInSpan("tagged-op", "key", "value", () -> {});

        assertThat(spans()).hasSize(1);
        assertThat(spans().get(0).getName()).isEqualTo("tagged-op");
        assertThat(spans().get(0).getTags()).containsEntry("key", "value");
    }

    @Test
    void executeInSpan_supplierWithTag_returnsValueAndCreatesTaggedSpan() {
        int result = tracingHelper.executeInSpan("tagged-supplier", "key", "val", () -> 42);

        assertThat(result).isEqualTo(42);
        assertThat(spans()).hasSize(1);
        assertThat(spans().get(0).getTags()).containsEntry("key", "val");
    }

    @Test
    void executeInSpan_runnable_recordsErrorOnException() {
        assertThatThrownBy(
                        () ->
                                tracingHelper.executeInSpan(
                                        "failing-op",
                                        (Runnable)
                                                () -> {
                                                    throw new RuntimeException("boom");
                                                }))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("boom");

        assertThat(spans()).hasSize(1);
        assertThat(spans().get(0).getError()).isNotNull();
    }

    @Test
    void executeInSpan_supplier_recordsErrorOnException() {
        assertThatThrownBy(
                        () ->
                                tracingHelper.<String>executeInSpan(
                                        "failing-supplier",
                                        () -> {
                                            throw new RuntimeException("supplier boom");
                                        }))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("supplier boom");

        assertThat(spans()).hasSize(1);
        assertThat(spans().get(0).getError()).isNotNull();
    }

    @Test
    void getTracer_returnsUnderlyingTracer() {
        Tracer result = tracingHelper.getTracer();
        assertThat(result).isSameAs(tracer);
    }
}
