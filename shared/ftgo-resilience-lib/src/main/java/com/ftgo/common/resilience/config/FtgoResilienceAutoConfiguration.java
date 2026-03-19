package com.ftgo.common.resilience.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Auto-configuration for FTGO resilience patterns.
 *
 * <p>Provides default Resilience4j circuit breaker, retry, bulkhead,
 * and rate limiter configurations as well as custom health indicators
 * for downstream service dependencies.
 */
@AutoConfiguration
@ComponentScan(basePackages = "com.ftgo.common.resilience")
public class FtgoResilienceAutoConfiguration {
}
