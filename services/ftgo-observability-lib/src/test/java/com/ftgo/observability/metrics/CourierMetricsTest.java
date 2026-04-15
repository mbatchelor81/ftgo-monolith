package com.ftgo.observability.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CourierMetricsTest {

    private MeterRegistry registry;
    private CourierMetrics courierMetrics;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        courierMetrics = new CourierMetrics(registry);
    }

    @Test
    void recordCourierCreated_incrementsCounter() {
        courierMetrics.recordCourierCreated();

        double count =
                registry.counter("ftgo.couriers.created", "service", "courier-service").count();
        assertThat(count).isEqualTo(1.0);
    }

    @Test
    void recordDeliveryAssigned_incrementsCounter() {
        courierMetrics.recordDeliveryAssigned();

        double count =
                registry.counter("ftgo.couriers.deliveries.assigned", "service", "courier-service")
                        .count();
        assertThat(count).isEqualTo(1.0);
    }

    @Test
    void setCouriersAvailable_updatesGauge() {
        courierMetrics.setCouriersAvailable(5);

        double value = registry.get("ftgo.couriers.available").gauge().value();
        assertThat(value).isEqualTo(5.0);
    }
}
