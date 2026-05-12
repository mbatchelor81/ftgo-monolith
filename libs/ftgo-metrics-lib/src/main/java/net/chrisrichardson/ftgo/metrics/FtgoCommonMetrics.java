package net.chrisrichardson.ftgo.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.MeterBinder;

/**
 * Cross-cutting metrics shared across all FTGO services.
 * Tracks API request counts, error rates, and latencies at the platform level.
 */
public class FtgoCommonMetrics implements MeterBinder {

    private final MeterRegistry registry;

    public FtgoCommonMetrics(MeterRegistry registry) {
        this.registry = registry;
        bindTo(registry);
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        Counter.builder("ftgo.api.requests")
                .description("Total API requests across the platform")
                .tag("platform", "ftgo")
                .register(registry);

        Counter.builder("ftgo.api.errors")
                .description("Total API errors across the platform")
                .tag("platform", "ftgo")
                .register(registry);

        Timer.builder("ftgo.api.latency")
                .description("API request latency")
                .tag("platform", "ftgo")
                .register(registry);
    }

    public Counter requestCounter(String service, String operation) {
        return Counter.builder("ftgo.api.requests")
                .tag("service", service)
                .tag("operation", operation)
                .register(registry);
    }

    public Counter errorCounter(String service, String operation) {
        return Counter.builder("ftgo.api.errors")
                .tag("service", service)
                .tag("operation", operation)
                .register(registry);
    }

    public Timer latencyTimer(String service, String operation) {
        return Timer.builder("ftgo.api.latency")
                .tag("service", service)
                .tag("operation", operation)
                .register(registry);
    }
}
