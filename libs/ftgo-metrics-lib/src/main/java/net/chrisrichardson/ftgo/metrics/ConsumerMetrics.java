package net.chrisrichardson.ftgo.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
/**
 * Business metrics for the Consumer Service.
 * Tracks consumer registration and validation events.
 */
public class ConsumerMetrics implements MeterBinder {

    private final MeterRegistry registry;

    private Counter consumersRegistered;
    private Counter consumerValidationsSucceeded;
    private Counter consumerValidationsFailed;

    public ConsumerMetrics(MeterRegistry registry) {
        this.registry = registry;
        bindTo(registry);
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        consumersRegistered = Counter.builder("ftgo.consumers.registered")
                .description("Total number of consumers registered")
                .register(registry);

        consumerValidationsSucceeded = Counter.builder("ftgo.consumers.validations.succeeded")
                .description("Total number of successful consumer validations for orders")
                .register(registry);

        consumerValidationsFailed = Counter.builder("ftgo.consumers.validations.failed")
                .description("Total number of failed consumer validations")
                .register(registry);
    }

    public Counter getConsumersRegistered() { return consumersRegistered; }
    public Counter getConsumerValidationsSucceeded() { return consumerValidationsSucceeded; }
    public Counter getConsumerValidationsFailed() { return consumerValidationsFailed; }
}
