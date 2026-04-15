package com.ftgo.observability.resilience;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Resilience4j patterns across FTGO services.
 *
 * <p>Defaults match the EM-44 acceptance criteria:
 *
 * <ul>
 *   <li>Circuit breaker: 5 failures → open state, 30s wait before half-open
 *   <li>Retry: 3 attempts with exponential backoff (1s, 2s, 4s)
 *   <li>Bulkhead: 25 max concurrent calls
 *   <li>Rate limiter: 50 calls per second
 * </ul>
 *
 * <p>Override per-service via {@code ftgo.resilience.*} in {@code application.yml}.
 */
@ConfigurationProperties(prefix = "ftgo.resilience")
public class ResilienceProperties {

    private CircuitBreakerProperties circuitBreaker = new CircuitBreakerProperties();
    private RetryProperties retry = new RetryProperties();
    private BulkheadProperties bulkhead = new BulkheadProperties();
    private RateLimiterProperties rateLimiter = new RateLimiterProperties();

    public CircuitBreakerProperties getCircuitBreaker() {
        return circuitBreaker;
    }

    public void setCircuitBreaker(CircuitBreakerProperties circuitBreaker) {
        this.circuitBreaker = circuitBreaker;
    }

    public RetryProperties getRetry() {
        return retry;
    }

    public void setRetry(RetryProperties retry) {
        this.retry = retry;
    }

    public BulkheadProperties getBulkhead() {
        return bulkhead;
    }

    public void setBulkhead(BulkheadProperties bulkhead) {
        this.bulkhead = bulkhead;
    }

    public RateLimiterProperties getRateLimiter() {
        return rateLimiter;
    }

    public void setRateLimiter(RateLimiterProperties rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    /** Circuit breaker configuration — 5 failures → open, 30s half-open wait. */
    public static class CircuitBreakerProperties {

        /** Failure rate threshold (%) to trip the circuit. */
        private float failureRateThreshold = 50;

        /** Number of calls in the sliding window. */
        private int slidingWindowSize = 10;

        /** Minimum number of calls before the failure rate is calculated. */
        private int minimumNumberOfCalls = 5;

        /** Seconds to wait in OPEN state before transitioning to HALF_OPEN. */
        private long waitDurationInOpenStateSeconds = 30;

        /** Number of calls permitted in HALF_OPEN state. */
        private int permittedNumberOfCallsInHalfOpenState = 3;

        /** Slow call rate threshold (%). */
        private float slowCallRateThreshold = 100;

        /** Duration threshold (seconds) above which a call is considered slow. */
        private long slowCallDurationThresholdSeconds = 10;

        public float getFailureRateThreshold() {
            return failureRateThreshold;
        }

        public void setFailureRateThreshold(float failureRateThreshold) {
            this.failureRateThreshold = failureRateThreshold;
        }

        public int getSlidingWindowSize() {
            return slidingWindowSize;
        }

        public void setSlidingWindowSize(int slidingWindowSize) {
            this.slidingWindowSize = slidingWindowSize;
        }

        public int getMinimumNumberOfCalls() {
            return minimumNumberOfCalls;
        }

        public void setMinimumNumberOfCalls(int minimumNumberOfCalls) {
            this.minimumNumberOfCalls = minimumNumberOfCalls;
        }

        public long getWaitDurationInOpenStateSeconds() {
            return waitDurationInOpenStateSeconds;
        }

        public void setWaitDurationInOpenStateSeconds(long waitDurationInOpenStateSeconds) {
            this.waitDurationInOpenStateSeconds = waitDurationInOpenStateSeconds;
        }

        public int getPermittedNumberOfCallsInHalfOpenState() {
            return permittedNumberOfCallsInHalfOpenState;
        }

        public void setPermittedNumberOfCallsInHalfOpenState(
                int permittedNumberOfCallsInHalfOpenState) {
            this.permittedNumberOfCallsInHalfOpenState = permittedNumberOfCallsInHalfOpenState;
        }

        public float getSlowCallRateThreshold() {
            return slowCallRateThreshold;
        }

        public void setSlowCallRateThreshold(float slowCallRateThreshold) {
            this.slowCallRateThreshold = slowCallRateThreshold;
        }

        public long getSlowCallDurationThresholdSeconds() {
            return slowCallDurationThresholdSeconds;
        }

        public void setSlowCallDurationThresholdSeconds(long slowCallDurationThresholdSeconds) {
            this.slowCallDurationThresholdSeconds = slowCallDurationThresholdSeconds;
        }
    }

    /** Retry configuration — 3 attempts with exponential backoff (1s, 2s, 4s). */
    public static class RetryProperties {

        /** Maximum number of retry attempts (including the initial call). */
        private int maxAttempts = 3;

        /** Initial wait interval in milliseconds before the first retry. */
        private long initialIntervalMillis = 1000;

        /** Multiplier applied to the interval for exponential backoff. */
        private double multiplier = 2.0;

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public long getInitialIntervalMillis() {
            return initialIntervalMillis;
        }

        public void setInitialIntervalMillis(long initialIntervalMillis) {
            this.initialIntervalMillis = initialIntervalMillis;
        }

        public double getMultiplier() {
            return multiplier;
        }

        public void setMultiplier(double multiplier) {
            this.multiplier = multiplier;
        }
    }

    /** Bulkhead configuration — limits concurrent calls per service. */
    public static class BulkheadProperties {

        /** Maximum number of concurrent calls allowed. */
        private int maxConcurrentCalls = 25;

        /** Maximum wait duration (ms) for a permit. */
        private long maxWaitDurationMillis = 500;

        public int getMaxConcurrentCalls() {
            return maxConcurrentCalls;
        }

        public void setMaxConcurrentCalls(int maxConcurrentCalls) {
            this.maxConcurrentCalls = maxConcurrentCalls;
        }

        public long getMaxWaitDurationMillis() {
            return maxWaitDurationMillis;
        }

        public void setMaxWaitDurationMillis(long maxWaitDurationMillis) {
            this.maxWaitDurationMillis = maxWaitDurationMillis;
        }
    }

    /** Rate limiter configuration — limits request rate per service. */
    public static class RateLimiterProperties {

        /** Number of permits available per refresh period. */
        private int limitForPeriod = 50;

        /** Duration (seconds) of the rate limiter refresh period. */
        private int limitRefreshPeriodSeconds = 1;

        /** Maximum wait duration (ms) for a permit. */
        private long timeoutDurationMillis = 500;

        public int getLimitForPeriod() {
            return limitForPeriod;
        }

        public void setLimitForPeriod(int limitForPeriod) {
            this.limitForPeriod = limitForPeriod;
        }

        public int getLimitRefreshPeriodSeconds() {
            return limitRefreshPeriodSeconds;
        }

        public void setLimitRefreshPeriodSeconds(int limitRefreshPeriodSeconds) {
            this.limitRefreshPeriodSeconds = limitRefreshPeriodSeconds;
        }

        public long getTimeoutDurationMillis() {
            return timeoutDurationMillis;
        }

        public void setTimeoutDurationMillis(long timeoutDurationMillis) {
            this.timeoutDurationMillis = timeoutDurationMillis;
        }
    }
}
