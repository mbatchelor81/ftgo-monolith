package com.ftgo.resilience.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration that activates all FTGO resilience components.
 *
 * <p>Importing this configuration (or depending on ftgo-resilience-lib with
 * Spring Boot auto-configuration) provides:
 * <ul>
 *   <li>Resilience4j circuit breaker, retry, bulkhead, and rate limiter defaults</li>
 *   <li>Custom health indicators (database, disk, business)</li>
 *   <li>Kubernetes DNS-based service discovery</li>
 *   <li>Graceful shutdown configuration</li>
 * </ul>
 */
@Configuration
@ComponentScan(basePackages = "com.ftgo.resilience")
@Import({
    ResilienceConfiguration.class,
    GracefulShutdownConfiguration.class,
    ServiceDiscoveryConfiguration.class
})
public class FtgoResilienceAutoConfiguration {
}
