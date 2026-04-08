package com.ftgo.resilience.config;

import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResilienceConfigurationTest {

    private final ResilienceConfiguration config = new ResilienceConfiguration();

    @Test
    void circuitBreakerRegistry_createsRegistryWithDefaults() {
        CircuitBreakerRegistry registry = config.circuitBreakerRegistry();

        assertThat(registry).isNotNull();
        var cb = registry.circuitBreaker("test");
        assertThat(cb.getCircuitBreakerConfig().getFailureRateThreshold()).isEqualTo(50f);
        assertThat(cb.getCircuitBreakerConfig().getSlidingWindowSize()).isEqualTo(10);
        assertThat(cb.getCircuitBreakerConfig().getMinimumNumberOfCalls()).isEqualTo(5);
        assertThat(cb.getCircuitBreakerConfig().getPermittedNumberOfCallsInHalfOpenState()).isEqualTo(3);
    }

    @Test
    void retryRegistry_createsRegistryWithExponentialBackoff() {
        RetryRegistry registry = config.retryRegistry();

        assertThat(registry).isNotNull();
        var retry = registry.retry("test");
        assertThat(retry.getRetryConfig().getMaxAttempts()).isEqualTo(3);
    }

    @Test
    void bulkheadRegistry_createsRegistryWithDefaults() {
        BulkheadRegistry registry = config.bulkheadRegistry();

        assertThat(registry).isNotNull();
        var bulkhead = registry.bulkhead("test");
        assertThat(bulkhead.getBulkheadConfig().getMaxConcurrentCalls()).isEqualTo(25);
    }

    @Test
    void rateLimiterRegistry_createsRegistryWithDefaults() {
        RateLimiterRegistry registry = config.rateLimiterRegistry();

        assertThat(registry).isNotNull();
        var rateLimiter = registry.rateLimiter("test");
        assertThat(rateLimiter.getRateLimiterConfig().getLimitForPeriod()).isEqualTo(50);
    }
}
