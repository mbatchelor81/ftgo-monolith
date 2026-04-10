package com.ftgo.consumer.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Configures custom business metrics for the Consumer Service.
 *
 * <p>Metrics exposed:
 * <ul>
 *   <li>{@code ftgo.consumers.registered} — counter of consumers registered</li>
 *   <li>{@code ftgo.consumers.validated} — counter of consumer validations performed</li>
 *   <li>{@code ftgo.consumers.validation.failed} — counter of failed consumer validations</li>
 *   <li>{@code ftgo.consumers.total} — gauge of total registered consumers</li>
 *   <li>{@code ftgo.consumers.validation.duration} — timer for validation latency</li>
 * </ul>
 */
@Configuration
public class ConsumerMetricsConfiguration {

    private final AtomicLong totalConsumers = new AtomicLong(0);

    @Bean
    public Counter consumersRegisteredCounter(MeterRegistry registry) {
        return Counter.builder("ftgo.consumers.registered")
                .description("Total number of consumers registered")
                .tag("service", "ftgo-consumer-service")
                .register(registry);
    }

    @Bean
    public Counter consumersValidatedCounter(MeterRegistry registry) {
        return Counter.builder("ftgo.consumers.validated")
                .description("Total number of consumer order validations performed")
                .tag("service", "ftgo-consumer-service")
                .register(registry);
    }

    @Bean
    public Counter consumersValidationFailedCounter(MeterRegistry registry) {
        return Counter.builder("ftgo.consumers.validation.failed")
                .description("Total number of failed consumer order validations")
                .tag("service", "ftgo-consumer-service")
                .register(registry);
    }

    @Bean
    public AtomicLong totalConsumersGauge(MeterRegistry registry) {
        Gauge.builder("ftgo.consumers.total", totalConsumers, AtomicLong::doubleValue)
                .description("Total number of registered consumers")
                .tag("service", "ftgo-consumer-service")
                .register(registry);
        return totalConsumers;
    }

    @Bean
    public Timer consumerValidationTimer(MeterRegistry registry) {
        return Timer.builder("ftgo.consumers.validation.duration")
                .description("Time taken to validate a consumer for an order")
                .tag("service", "ftgo-consumer-service")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
    }
}
