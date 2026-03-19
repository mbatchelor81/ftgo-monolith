package com.ftgo.common.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class BusinessMetricsTest {

    private MeterRegistry registry;
    private BusinessMetrics businessMetrics;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        businessMetrics = new BusinessMetrics(registry, "order");
    }

    @Test
    void incrementCounterCreatesAndIncrements() {
        businessMetrics.incrementCounter("created");

        assertThat(registry.find("order.created").counter()).isNotNull();
        assertThat(registry.find("order.created").counter().count()).isEqualTo(1.0);
    }

    @Test
    void incrementCounterMultipleTimes() {
        businessMetrics.incrementCounter("created");
        businessMetrics.incrementCounter("created");
        businessMetrics.incrementCounter("created");

        assertThat(registry.find("order.created").counter().count()).isEqualTo(3.0);
    }

    @Test
    void recordTimerCreatesAndRecords() {
        businessMetrics.recordTimer("processing.time", Duration.ofMillis(150));

        assertThat(registry.find("order.processing.time").timer()).isNotNull();
        assertThat(registry.find("order.processing.time").timer().count()).isEqualTo(1);
    }

    @Test
    void getRegistryReturnsUnderlyingRegistry() {
        assertThat(businessMetrics.getRegistry()).isSameAs(registry);
    }
}
