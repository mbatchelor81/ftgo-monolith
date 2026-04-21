package com.ftgo.consumer.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * Business metrics published by the Consumer microservice.
 *
 * <p>See {@link com.ftgo.consumer.ConsumerServiceApplication} for the
 * Spring Boot entry point and {@code config/application.yml} for the
 * Prometheus endpoint configuration. The metric names below map 1:1 to
 * panels in the Grafana dashboard shipped under
 * {@code platform/observability/grafana/dashboards/}.
 */
@Component
public class ConsumerMetrics {

    public static final String CONSUMERS_REGISTERED = "consumers.registered";
    public static final String CONSUMERS_VALIDATED = "consumers.order.validated";
    public static final String CONSUMERS_VALIDATION_FAILED = "consumers.order.validation.failed";

    private final Counter consumersRegistered;
    private final Counter consumersValidated;
    private final Counter consumersValidationFailed;

    public ConsumerMetrics(MeterRegistry registry) {
        this.consumersRegistered = Counter.builder(CONSUMERS_REGISTERED)
                .description("Total number of consumers successfully registered")
                .tag("service", "consumer-service")
                .register(registry);

        this.consumersValidated = Counter.builder(CONSUMERS_VALIDATED)
                .description("Total number of consumer-order validations that succeeded")
                .tag("service", "consumer-service")
                .register(registry);

        this.consumersValidationFailed = Counter.builder(CONSUMERS_VALIDATION_FAILED)
                .description("Total number of consumer-order validations that failed (e.g. order amount exceeded the consumer's limit)")
                .tag("service", "consumer-service")
                .register(registry);
    }

    public void recordConsumerRegistered() {
        consumersRegistered.increment();
    }

    public void recordConsumerValidated() {
        consumersValidated.increment();
    }

    public void recordConsumerValidationFailed() {
        consumersValidationFailed.increment();
    }
}
