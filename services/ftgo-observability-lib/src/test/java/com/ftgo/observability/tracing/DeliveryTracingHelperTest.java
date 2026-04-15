package com.ftgo.observability.tracing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.micrometer.tracing.test.simple.SimpleSpan;
import io.micrometer.tracing.test.simple.SimpleTracer;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeliveryTracingHelperTest {

    private SimpleTracer tracer;
    private DeliveryTracingHelper deliveryTracingHelper;

    @BeforeEach
    void setUp() {
        tracer = new SimpleTracer();
        deliveryTracingHelper = new DeliveryTracingHelper(tracer);
    }

    private List<SimpleSpan> spans() {
        return new ArrayList<>(tracer.getSpans());
    }

    @Test
    void traceCourierAssignment_createsSpanWithTags() {
        deliveryTracingHelper.traceCourierAssignment(5L, 10L, () -> {});

        assertThat(spans()).hasSize(1);
        assertThat(spans().get(0).getName()).isEqualTo("delivery.assign");
        assertThat(spans().get(0).getTags())
                .containsEntry("courier.id", "5")
                .containsEntry("order.id", "10");
    }

    @Test
    void traceCourierAssignment_recordsErrorOnFailure() {
        assertThatThrownBy(
                        () ->
                                deliveryTracingHelper.traceCourierAssignment(
                                        5L,
                                        10L,
                                        () -> {
                                            throw new RuntimeException("assignment failed");
                                        }))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("assignment failed");

        assertThat(spans()).hasSize(1);
        assertThat(spans().get(0).getError()).isNotNull();
    }

    @Test
    void traceCourierAvailability_createsSpanWithTags() {
        deliveryTracingHelper.traceCourierAvailability(3L, true, () -> {});

        assertThat(spans()).hasSize(1);
        assertThat(spans().get(0).getName()).isEqualTo("delivery.availability");
        assertThat(spans().get(0).getTags())
                .containsEntry("courier.id", "3")
                .containsEntry("courier.available", "true");
    }

    @Test
    void tracePickup_createsSpanWithOrderId() {
        deliveryTracingHelper.tracePickup(7L, () -> {});

        assertThat(spans()).hasSize(1);
        assertThat(spans().get(0).getName()).isEqualTo("delivery.pickup");
        assertThat(spans().get(0).getTags()).containsEntry("order.id", "7");
    }

    @Test
    void traceDeliveryComplete_createsSpanWithOrderId() {
        deliveryTracingHelper.traceDeliveryComplete(15L, () -> {});

        assertThat(spans()).hasSize(1);
        assertThat(spans().get(0).getName()).isEqualTo("delivery.complete");
        assertThat(spans().get(0).getTags()).containsEntry("order.id", "15");
    }

    @Test
    void traceDeliveryComplete_recordsErrorOnFailure() {
        assertThatThrownBy(
                        () ->
                                deliveryTracingHelper.traceDeliveryComplete(
                                        15L,
                                        () -> {
                                            throw new RuntimeException("delivery failed");
                                        }))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("delivery failed");

        assertThat(spans()).hasSize(1);
        assertThat(spans().get(0).getError()).isNotNull();
    }
}
