package com.ftgo.observability.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConsumerMetricsTest {

    private MeterRegistry registry;
    private ConsumerMetrics consumerMetrics;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        consumerMetrics = new ConsumerMetrics(registry);
    }

    @Test
    void recordConsumerRegistered_incrementsCounter() {
        consumerMetrics.recordConsumerRegistered();

        double count = registry.counter("ftgo.consumers.registered", "service", "consumer-service").count();
        assertThat(count).isEqualTo(1.0);
    }

    @Test
    void recordValidationPerformed_incrementsCounter() {
        consumerMetrics.recordValidationPerformed();
        consumerMetrics.recordValidationPerformed();

        double count = registry.counter("ftgo.consumers.validations.performed", "service", "consumer-service").count();
        assertThat(count).isEqualTo(2.0);
    }

    @Test
    void recordValidationFailed_incrementsCounter() {
        consumerMetrics.recordValidationFailed();

        double count = registry.counter("ftgo.consumers.validations.failed", "service", "consumer-service").count();
        assertThat(count).isEqualTo(1.0);
    }
}
