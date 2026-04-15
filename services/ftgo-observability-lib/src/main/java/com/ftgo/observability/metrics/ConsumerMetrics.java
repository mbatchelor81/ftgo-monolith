package com.ftgo.observability.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Custom business metrics for the Consumer Service.
 *
 * <p>Tracks consumer registration and order validation events.
 */
@Component
@ConditionalOnProperty(name = "spring.application.name", havingValue = "ftgo-consumer-service")
public class ConsumerMetrics {

    private final Counter consumersRegistered;
    private final Counter consumerValidationsPerformed;
    private final Counter consumerValidationsFailed;

    public ConsumerMetrics(MeterRegistry registry) {
        this.consumersRegistered = Counter.builder("ftgo.consumers.registered")
                .description("Total number of consumers registered")
                .tag("service", "consumer-service")
                .register(registry);

        this.consumerValidationsPerformed = Counter.builder("ftgo.consumers.validations.performed")
                .description("Total number of order validations performed for consumers")
                .tag("service", "consumer-service")
                .register(registry);

        this.consumerValidationsFailed = Counter.builder("ftgo.consumers.validations.failed")
                .description("Total number of consumer order validations that failed")
                .tag("service", "consumer-service")
                .register(registry);
    }

    public void recordConsumerRegistered() {
        consumersRegistered.increment();
    }

    public void recordValidationPerformed() {
        consumerValidationsPerformed.increment();
    }

    public void recordValidationFailed() {
        consumerValidationsFailed.increment();
    }
}
