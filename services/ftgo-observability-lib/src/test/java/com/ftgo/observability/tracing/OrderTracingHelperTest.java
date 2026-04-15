package com.ftgo.observability.tracing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.micrometer.tracing.test.simple.SimpleSpan;
import io.micrometer.tracing.test.simple.SimpleTracer;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrderTracingHelperTest {

    private SimpleTracer tracer;
    private OrderTracingHelper orderTracingHelper;

    @BeforeEach
    void setUp() {
        tracer = new SimpleTracer();
        orderTracingHelper = new OrderTracingHelper(tracer);
    }

    private List<SimpleSpan> spans() {
        return new ArrayList<>(tracer.getSpans());
    }

    @Test
    void traceOrderCreation_createsSpanWithTags() {
        String result = orderTracingHelper.traceOrderCreation(1L, 2L, () -> "order-123");

        assertThat(result).isEqualTo("order-123");
        assertThat(spans()).hasSize(1);
        assertThat(spans().get(0).getName()).isEqualTo("order.create");
        assertThat(spans().get(0).getTags())
                .containsEntry("order.consumerId", "1")
                .containsEntry("order.restaurantId", "2");
    }

    @Test
    void traceOrderCreation_recordsErrorOnFailure() {
        assertThatThrownBy(
                        () ->
                                orderTracingHelper.<String>traceOrderCreation(
                                        1L,
                                        2L,
                                        () -> {
                                            throw new RuntimeException("creation failed");
                                        }))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("creation failed");

        assertThat(spans()).hasSize(1);
        assertThat(spans().get(0).getError()).isNotNull();
    }

    @Test
    void traceOrderAcceptance_createsSpanWithOrderId() {
        orderTracingHelper.traceOrderAcceptance(42L, () -> {});

        assertThat(spans()).hasSize(1);
        assertThat(spans().get(0).getName()).isEqualTo("order.accept");
        assertThat(spans().get(0).getTags()).containsEntry("order.id", "42");
    }

    @Test
    void traceOrderCancellation_createsSpanWithOrderId() {
        orderTracingHelper.traceOrderCancellation(99L, () -> {});

        assertThat(spans()).hasSize(1);
        assertThat(spans().get(0).getName()).isEqualTo("order.cancel");
        assertThat(spans().get(0).getTags()).containsEntry("order.id", "99");
    }

    @Test
    void traceOrderRevision_createsSpanAndReturnsResult() {
        Object result = orderTracingHelper.traceOrderRevision(10L, () -> "revised");

        assertThat(result).isEqualTo("revised");
        assertThat(spans()).hasSize(1);
        assertThat(spans().get(0).getName()).isEqualTo("order.revise");
        assertThat(spans().get(0).getTags()).containsEntry("order.id", "10");
    }
}
