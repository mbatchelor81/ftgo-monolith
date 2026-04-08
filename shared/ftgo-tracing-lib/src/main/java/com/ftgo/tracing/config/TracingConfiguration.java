package com.ftgo.tracing.config;

import com.ftgo.tracing.aspect.TracedAspect;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
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
     * Provides a {@link RestTemplate} instrumented with Micrometer Tracing
     * to propagate trace context (traceId/spanId headers) to downstream services.
     *
     * <p>Uses {@link RestTemplateBuilder} so that Spring Boot's
     * {@code ObservationRestTemplateCustomizer} applies tracing interceptors.
     * A plain {@code new RestTemplate()} would bypass this instrumentation.
     */
    @Bean
    @ConditionalOnMissingBean(RestTemplate.class)
    @ConditionalOnProperty(name = "management.tracing.enabled", havingValue = "true", matchIfMissing = true)
    public RestTemplate tracedRestTemplate(RestTemplateBuilder builder) {
        log.info("Creating traced RestTemplate for distributed trace propagation");
        return builder.build();
    }

    /**
     * Registers the {@link TracedAspect} as a bean, guarded by the presence
     * of a {@link Tracer} bean. Defined here (rather than via {@code @Component})
     * so that {@code @ConditionalOnBean} evaluation happens reliably after
     * Spring Boot's {@code BraveAutoConfiguration} has registered the tracer.
     */
    @Bean
    @ConditionalOnBean(Tracer.class)
    public TracedAspect tracedAspect(Tracer tracer) {
        return new TracedAspect(tracer);
    }
}
