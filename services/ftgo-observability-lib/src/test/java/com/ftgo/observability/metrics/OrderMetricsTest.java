package com.ftgo.observability.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrderMetricsTest {

    private MeterRegistry registry;
    private OrderMetrics orderMetrics;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        orderMetrics = new OrderMetrics(registry);
    }

    @Test
    void recordOrderCreated_incrementsCounter() {
        orderMetrics.recordOrderCreated();
        orderMetrics.recordOrderCreated();

        double count = registry.counter("ftgo.orders.created", "service", "order-service").count();
        assertThat(count).isEqualTo(2.0);
    }

    @Test
    void recordOrderAccepted_incrementsCounter() {
        orderMetrics.recordOrderAccepted();

        double count = registry.counter("ftgo.orders.accepted", "service", "order-service").count();
        assertThat(count).isEqualTo(1.0);
    }

    @Test
    void recordOrderCancelled_incrementsCounter() {
        orderMetrics.recordOrderCancelled();

        double count =
                registry.counter("ftgo.orders.cancelled", "service", "order-service").count();
        assertThat(count).isEqualTo(1.0);
    }

    @Test
    void recordOrderDelivered_incrementsCounter() {
        orderMetrics.recordOrderDelivered();

        double count =
                registry.counter("ftgo.orders.delivered", "service", "order-service").count();
        assertThat(count).isEqualTo(1.0);
    }

    @Test
    void orderProcessingTimer_isRegistered() {
        assertThat(orderMetrics.getOrderProcessingTime()).isNotNull();
        assertThat(registry.timer("ftgo.orders.processing.time", "service", "order-service"))
                .isNotNull();
    }
}
