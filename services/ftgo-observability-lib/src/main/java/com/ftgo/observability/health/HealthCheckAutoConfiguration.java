package com.ftgo.observability.health;

import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for shared FTGO health check infrastructure.
 *
 * <p>Registers health indicators that are common across all FTGO services: database connectivity
 * (provided automatically by Spring Boot Actuator when JPA is on the classpath), disk space, and
 * custom downstream service checks (registered per-service via {@link
 * DownstreamServiceHealthIndicator}).
 *
 * <p>Kubernetes readiness and liveness probe groups are configured in each service's {@code
 * application.yml} to map to {@code /actuator/health/readiness} and {@code
 * /actuator/health/liveness}.
 */
@Configuration
@ConditionalOnClass(HealthIndicator.class)
public class HealthCheckAutoConfiguration {

    /**
     * Registers a custom health indicator that verifies the service can perform basic operations.
     *
     * <p>This indicator is included in the liveness probe group to detect deadlocked or
     * unresponsive services.
     */
    @Bean
    public ServiceLivenessHealthIndicator serviceLivenessHealthIndicator() {
        return new ServiceLivenessHealthIndicator();
    }
}
