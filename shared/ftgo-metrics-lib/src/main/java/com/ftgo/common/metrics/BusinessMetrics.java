package com.ftgo.common.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for creating and accessing business metrics.
 * Each service creates an instance via its MeterRegistry and registers
 * domain-specific counters and timers.
 */
public class BusinessMetrics {

    private final MeterRegistry registry;
    private final String servicePrefix;
    private final ConcurrentHashMap<String, Counter> counters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Timer> timers = new ConcurrentHashMap<>();

    public BusinessMetrics(MeterRegistry registry, String servicePrefix) {
        this.registry = registry;
        this.servicePrefix = servicePrefix;
    }

    /**
     * Increment a named counter metric.
     * Counter is created lazily on first use.
     */
    public void incrementCounter(String name, String... tags) {
        String fullName = servicePrefix + "." + name;
        counters.computeIfAbsent(fullName, n ->
                Counter.builder(n)
                        .tags(tags)
                        .register(registry)
        ).increment();
    }

    /**
     * Record a duration for a named timer metric.
     */
    public void recordTimer(String name, Duration duration, String... tags) {
        String fullName = servicePrefix + "." + name;
        timers.computeIfAbsent(fullName, n ->
                Timer.builder(n)
                        .tags(tags)
                        .publishPercentiles(0.5, 0.95, 0.99)
                        .register(registry)
        ).record(duration);
    }

    /**
     * Get the underlying MeterRegistry for advanced use cases.
     */
    public MeterRegistry getRegistry() {
        return registry;
    }
}
