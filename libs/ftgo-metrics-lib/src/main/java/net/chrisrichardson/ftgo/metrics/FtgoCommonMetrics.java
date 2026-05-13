package net.chrisrichardson.ftgo.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/**
 * Cross-cutting metrics shared across all FTGO services.
 * Provides factory methods for tagged API request counters, error counters, and latency timers.
 */
public class FtgoCommonMetrics {

    private final MeterRegistry registry;

    public FtgoCommonMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public Counter requestCounter(String service, String operation) {
        return Counter.builder("ftgo.api.requests")
                .description("Total API requests")
                .tag("service", service)
                .tag("operation", operation)
                .register(registry);
    }

    public Counter errorCounter(String service, String operation) {
        return Counter.builder("ftgo.api.errors")
                .description("Total API errors")
                .tag("service", service)
                .tag("operation", operation)
                .register(registry);
    }

    public Timer latencyTimer(String service, String operation) {
        return Timer.builder("ftgo.api.latency")
                .description("API request latency")
                .tag("service", service)
                .tag("operation", operation)
                .register(registry);
    }
}
