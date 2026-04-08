package com.ftgo.resilience.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

/**
 * Marker configuration for FTGO resilience defaults.
 *
 * <p>Resilience4j registries (circuit breaker, retry, bulkhead, rate limiter) are
 * auto-configured by the {@code resilience4j-spring-boot3} starter from properties
 * loaded via {@link FtgoResilienceEnvironmentPostProcessor} from
 * {@code ftgo-resilience-defaults.yml}. Services can override any default in their
 * own {@code application.yml}.
 *
 * <p>This class serves as a documentation anchor and can host additional
 * resilience-related beans in the future (e.g., custom event consumers,
 * fallback handlers).
 */
@Configuration
public class ResilienceConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ResilienceConfiguration.class);
}
