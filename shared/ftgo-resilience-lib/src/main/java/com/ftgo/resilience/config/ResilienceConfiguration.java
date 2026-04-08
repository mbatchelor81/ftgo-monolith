package com.ftgo.resilience.config;

import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Default Resilience4j configuration for FTGO microservices.
 *
 * <p>Provides sensible defaults for circuit breaker, retry with exponential backoff,
 * bulkhead isolation, and rate limiting. Services can override individual registries
 * by defining their own beans.
 */
@Configuration
public class ResilienceConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ResilienceConfiguration.class);

    /**
     * Circuit breaker registry with FTGO defaults.
     *
     * <p>Configuration:
     * <ul>
     *   <li>Failure rate threshold: 50%</li>
     *   <li>Slow call rate threshold: 80%</li>
     *   <li>Slow call duration: 2 seconds</li>
     *   <li>Wait duration in open state: 30 seconds</li>
     *   <li>Sliding window: count-based, size 10</li>
     *   <li>Minimum number of calls: 5</li>
     *   <li>Permitted calls in half-open: 3</li>
     * </ul>
     */
    @Bean
    @ConditionalOnMissingBean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig defaultConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slowCallRateThreshold(80)
                .slowCallDurationThreshold(Duration.ofSeconds(2))
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .permittedNumberOfCallsInHalfOpenState(3)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .build();

        log.info("Initialized FTGO default CircuitBreakerRegistry");
        return CircuitBreakerRegistry.of(defaultConfig);
    }

    /**
     * Retry registry with exponential backoff defaults.
     *
     * <p>Configuration:
     * <ul>
     *   <li>Max attempts: 3</li>
     *   <li>Initial wait: 500ms</li>
     *   <li>Multiplier: 2.0 (exponential backoff)</li>
     *   <li>Retries on: Exception.class</li>
     * </ul>
     */
    @Bean
    @ConditionalOnMissingBean
    public RetryRegistry retryRegistry() {
        RetryConfig defaultConfig = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(500))
                .enableExponentialBackoff()
                .exponentialBackoffMultiplier(2.0)
                .retryExceptions(Exception.class)
                .ignoreExceptions(IllegalArgumentException.class)
                .build();

        log.info("Initialized FTGO default RetryRegistry with exponential backoff");
        return RetryRegistry.of(defaultConfig);
    }

    /**
     * Bulkhead registry for concurrent call isolation.
     *
     * <p>Configuration:
     * <ul>
     *   <li>Max concurrent calls: 25</li>
     *   <li>Max wait duration: 500ms</li>
     * </ul>
     */
    @Bean
    @ConditionalOnMissingBean
    public BulkheadRegistry bulkheadRegistry() {
        BulkheadConfig defaultConfig = BulkheadConfig.custom()
                .maxConcurrentCalls(25)
                .maxWaitDuration(Duration.ofMillis(500))
                .build();

        log.info("Initialized FTGO default BulkheadRegistry");
        return BulkheadRegistry.of(defaultConfig);
    }

    /**
     * Rate limiter registry with FTGO defaults.
     *
     * <p>Configuration:
     * <ul>
     *   <li>Limit for period: 50 calls</li>
     *   <li>Limit refresh period: 1 second</li>
     *   <li>Timeout duration: 500ms</li>
     * </ul>
     */
    @Bean
    @ConditionalOnMissingBean
    public RateLimiterRegistry rateLimiterRegistry() {
        RateLimiterConfig defaultConfig = RateLimiterConfig.custom()
                .limitForPeriod(50)
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .timeoutDuration(Duration.ofMillis(500))
                .build();

        log.info("Initialized FTGO default RateLimiterRegistry");
        return RateLimiterRegistry.of(defaultConfig);
    }
}
