package com.ftgo.observability.resilience;

import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.micrometer.tagged.TaggedBulkheadMetrics;
import io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics;
import io.github.resilience4j.micrometer.tagged.TaggedRateLimiterMetrics;
import io.github.resilience4j.micrometer.tagged.TaggedRetryMetrics;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for Resilience4j patterns across all FTGO services.
 *
 * <p>Configures shared registries for circuit breaker, retry, bulkhead, and rate limiter with FTGO
 * defaults that match the acceptance criteria:
 *
 * <ul>
 *   <li>Circuit breaker: 5 failures → open, 30s half-open wait
 *   <li>Retry: 3 attempts, exponential backoff 1s/2s/4s
 *   <li>Bulkhead: max 25 concurrent calls, 500ms max wait
 *   <li>Rate limiter: 50 calls per second
 * </ul>
 *
 * <p>All resilience metrics are automatically published to Micrometer for Prometheus scraping.
 */
@Configuration
@ConditionalOnClass(CircuitBreakerRegistry.class)
@EnableConfigurationProperties(ResilienceProperties.class)
public class ResilienceAutoConfiguration {

    @Bean
    public CircuitBreakerConfig defaultCircuitBreakerConfig(ResilienceProperties properties) {
        ResilienceProperties.CircuitBreakerProperties cb = properties.getCircuitBreaker();
        return CircuitBreakerConfig.custom()
                .failureRateThreshold(cb.getFailureRateThreshold())
                .slidingWindowSize(cb.getSlidingWindowSize())
                .minimumNumberOfCalls(cb.getMinimumNumberOfCalls())
                .waitDurationInOpenState(Duration.ofSeconds(cb.getWaitDurationInOpenStateSeconds()))
                .permittedNumberOfCallsInHalfOpenState(
                        cb.getPermittedNumberOfCallsInHalfOpenState())
                .slowCallRateThreshold(cb.getSlowCallRateThreshold())
                .slowCallDurationThreshold(
                        Duration.ofSeconds(cb.getSlowCallDurationThresholdSeconds()))
                .recordExceptions(IOException.class, TimeoutException.class)
                .build();
    }

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry(CircuitBreakerConfig defaultConfig) {
        return CircuitBreakerRegistry.of(defaultConfig);
    }

    @Bean
    public RetryConfig defaultRetryConfig(ResilienceProperties properties) {
        ResilienceProperties.RetryProperties retry = properties.getRetry();
        return RetryConfig.custom()
                .maxAttempts(retry.getMaxAttempts())
                .waitDuration(Duration.ofMillis(retry.getInitialIntervalMillis()))
                .intervalFunction(
                        io.github.resilience4j.core.IntervalFunction.ofExponentialBackoff(
                                retry.getInitialIntervalMillis(), retry.getMultiplier()))
                .retryExceptions(IOException.class, TimeoutException.class)
                .build();
    }

    @Bean
    public RetryRegistry retryRegistry(RetryConfig defaultConfig) {
        return RetryRegistry.of(defaultConfig);
    }

    @Bean
    public BulkheadConfig defaultBulkheadConfig(ResilienceProperties properties) {
        ResilienceProperties.BulkheadProperties bulkhead = properties.getBulkhead();
        return BulkheadConfig.custom()
                .maxConcurrentCalls(bulkhead.getMaxConcurrentCalls())
                .maxWaitDuration(Duration.ofMillis(bulkhead.getMaxWaitDurationMillis()))
                .build();
    }

    @Bean
    public BulkheadRegistry bulkheadRegistry(BulkheadConfig defaultConfig) {
        return BulkheadRegistry.of(defaultConfig);
    }

    @Bean
    public RateLimiterConfig defaultRateLimiterConfig(ResilienceProperties properties) {
        ResilienceProperties.RateLimiterProperties rateLimiter = properties.getRateLimiter();
        return RateLimiterConfig.custom()
                .limitForPeriod(rateLimiter.getLimitForPeriod())
                .limitRefreshPeriod(Duration.ofSeconds(rateLimiter.getLimitRefreshPeriodSeconds()))
                .timeoutDuration(Duration.ofMillis(rateLimiter.getTimeoutDurationMillis()))
                .build();
    }

    @Bean
    public RateLimiterRegistry rateLimiterRegistry(RateLimiterConfig defaultConfig) {
        return RateLimiterRegistry.of(defaultConfig);
    }

    // Resilience metrics → Micrometer → Prometheus

    @Bean
    public TaggedCircuitBreakerMetrics taggedCircuitBreakerMetrics(
            CircuitBreakerRegistry registry, MeterRegistry meterRegistry) {
        TaggedCircuitBreakerMetrics metrics =
                TaggedCircuitBreakerMetrics.ofCircuitBreakerRegistry(registry);
        metrics.bindTo(meterRegistry);
        return metrics;
    }

    @Bean
    public TaggedRetryMetrics taggedRetryMetrics(
            RetryRegistry registry, MeterRegistry meterRegistry) {
        TaggedRetryMetrics metrics = TaggedRetryMetrics.ofRetryRegistry(registry);
        metrics.bindTo(meterRegistry);
        return metrics;
    }

    @Bean
    public TaggedBulkheadMetrics taggedBulkheadMetrics(
            BulkheadRegistry registry, MeterRegistry meterRegistry) {
        TaggedBulkheadMetrics metrics = TaggedBulkheadMetrics.ofBulkheadRegistry(registry);
        metrics.bindTo(meterRegistry);
        return metrics;
    }

    @Bean
    public TaggedRateLimiterMetrics taggedRateLimiterMetrics(
            RateLimiterRegistry registry, MeterRegistry meterRegistry) {
        TaggedRateLimiterMetrics metrics = TaggedRateLimiterMetrics.ofRateLimiterRegistry(registry);
        metrics.bindTo(meterRegistry);
        return metrics;
    }
}
