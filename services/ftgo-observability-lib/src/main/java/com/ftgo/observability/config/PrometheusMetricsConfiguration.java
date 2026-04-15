package com.ftgo.observability.config;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures Prometheus-specific meter registry settings.
 *
 * <p>Adds a common {@code application} tag to all metrics so that Prometheus and Grafana dashboards
 * can filter by service name. Also configures histogram buckets for HTTP request duration metrics.
 */
@Configuration
public class PrometheusMetricsConfiguration {

    @Value("${spring.application.name:unknown}")
    private String applicationName;

    /**
     * Tags every metric with {@code application=<service-name>} for multi-service
     * Prometheus/Grafana queries.
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> commonTags() {
        return registry -> registry.config().commonTags("application", applicationName);
    }

    /**
     * Publishes histogram buckets for HTTP server request duration so that Grafana can compute
     * percentile-based latency (p50, p95, p99) via PromQL.
     */
    @Bean
    public MeterFilter httpHistogramFilter() {
        return new MeterFilter() {
            @Override
            public DistributionStatisticConfig configure(
                    Meter.Id id, DistributionStatisticConfig config) {
                if (id.getName().startsWith("http.server.requests")) {
                    return DistributionStatisticConfig.builder()
                            .percentilesHistogram(true)
                            .percentiles(0.5, 0.9, 0.95, 0.99)
                            .serviceLevelObjectives(
                                    java.time.Duration.ofMillis(50).toNanos(),
                                    java.time.Duration.ofMillis(100).toNanos(),
                                    java.time.Duration.ofMillis(250).toNanos(),
                                    java.time.Duration.ofMillis(500).toNanos(),
                                    java.time.Duration.ofMillis(1000).toNanos())
                            .build()
                            .merge(config);
                }
                return config;
            }
        };
    }
}
