package com.ftgo.tracing.config;

import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Core tracing configuration for FTGO microservices.
 *
 * <p>Spring Boot 3.x auto-configures the Micrometer Tracing infrastructure
 * (Brave tracer, Zipkin reporter, HTTP instrumentation) when the dependencies
 * are on the classpath. This configuration provides additional beans that
 * enhance the default setup.
 *
 * <p>Key behaviors configured via {@code application.yml}:
 * <ul>
 *   <li>{@code management.tracing.sampling.probability} — sampling rate</li>
 *   <li>{@code management.zipkin.tracing.endpoint} — Zipkin collector URL</li>
 *   <li>{@code management.tracing.propagation.type} — W3C or B3 propagation</li>
 * </ul>
 */
@Configuration
@ConditionalOnClass(Tracer.class)
public class TracingConfiguration {

    private static final Logger log = LoggerFactory.getLogger(TracingConfiguration.class);

    /**
     * Provides a {@link RestTemplate} that is automatically instrumented
     * by Micrometer Tracing to propagate trace context to downstream services.
     *
     * <p>Spring Boot 3.x auto-configures an {@code ObservationRestTemplateCustomizer}
     * that instruments any {@code RestTemplate} bean with trace propagation headers.
     */
    @Bean
    @ConditionalOnMissingBean(RestTemplate.class)
    @ConditionalOnProperty(name = "management.tracing.enabled", havingValue = "true", matchIfMissing = true)
    public RestTemplate tracedRestTemplate() {
        log.info("Creating traced RestTemplate for distributed trace propagation");
        return new RestTemplate();
    }
}
