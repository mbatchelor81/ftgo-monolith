package com.ftgo.observability.resilience;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A resilient wrapper for inter-service calls that applies circuit breaker, retry, bulkhead, and
 * rate limiter patterns.
 *
 * <p>Each FTGO service creates named instances of this client for each downstream dependency. The
 * resilience patterns are layered in the following order (outermost to innermost):
 *
 * <ol>
 *   <li>Retry — retries transient failures with exponential backoff
 *   <li>Circuit Breaker — prevents calls to failing services
 *   <li>Rate Limiter — throttles call rate to protect downstream services
 *   <li>Bulkhead — limits concurrent calls to prevent resource exhaustion
 * </ol>
 */
public class ResilientServiceClient {

    private static final Logger LOG = LoggerFactory.getLogger(ResilientServiceClient.class);

    private final String serviceName;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;
    private final Bulkhead bulkhead;
    private final RateLimiter rateLimiter;

    public ResilientServiceClient(
            String serviceName,
            CircuitBreakerRegistry circuitBreakerRegistry,
            RetryRegistry retryRegistry,
            BulkheadRegistry bulkheadRegistry,
            RateLimiterRegistry rateLimiterRegistry) {
        this.serviceName = serviceName;
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker(serviceName);
        this.retry = retryRegistry.retry(serviceName);
        this.bulkhead = bulkheadRegistry.bulkhead(serviceName);
        this.rateLimiter = rateLimiterRegistry.rateLimiter(serviceName);
    }

    /**
     * Executes a supplier with all resilience patterns applied.
     *
     * @param <T> the return type
     * @param supplier the operation to execute
     * @return the result of the operation
     */
    public <T> T execute(Supplier<T> supplier) {
        Supplier<T> decoratedSupplier =
                Retry.decorateSupplier(
                        retry,
                        CircuitBreaker.decorateSupplier(
                                circuitBreaker,
                                RateLimiter.decorateSupplier(
                                        rateLimiter,
                                        Bulkhead.decorateSupplier(bulkhead, supplier))));

        try {
            return decoratedSupplier.get();
        } catch (Exception e) {
            LOG.error(
                    "Resilient call to {} failed after applying all patterns: {}",
                    serviceName,
                    e.getMessage());
            throw e;
        }
    }

    /**
     * Executes a runnable with all resilience patterns applied.
     *
     * @param runnable the operation to execute
     */
    public void execute(Runnable runnable) {
        Runnable decoratedRunnable =
                Retry.decorateRunnable(
                        retry,
                        CircuitBreaker.decorateRunnable(
                                circuitBreaker,
                                RateLimiter.decorateRunnable(
                                        rateLimiter,
                                        Bulkhead.decorateRunnable(bulkhead, runnable))));

        try {
            decoratedRunnable.run();
        } catch (Exception e) {
            LOG.error(
                    "Resilient call to {} failed after applying all patterns: {}",
                    serviceName,
                    e.getMessage());
            throw e;
        }
    }

    public CircuitBreaker getCircuitBreaker() {
        return circuitBreaker;
    }

    public Retry getRetry() {
        return retry;
    }

    public Bulkhead getBulkhead() {
        return bulkhead;
    }

    public RateLimiter getRateLimiter() {
        return rateLimiter;
    }

    public String getServiceName() {
        return serviceName;
    }
}
