package net.chrisrichardson.ftgo.resilience;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ftgo.resilience")
public class FtgoResilienceProperties {

    private boolean enabled = true;
    private CircuitBreaker circuitBreaker = new CircuitBreaker();
    private Retry retry = new Retry();
    private Bulkhead bulkhead = new Bulkhead();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public CircuitBreaker getCircuitBreaker() {
        return circuitBreaker;
    }

    public void setCircuitBreaker(CircuitBreaker circuitBreaker) {
        this.circuitBreaker = circuitBreaker;
    }

    public Retry getRetry() {
        return retry;
    }

    public void setRetry(Retry retry) {
        this.retry = retry;
    }

    public Bulkhead getBulkhead() {
        return bulkhead;
    }

    public void setBulkhead(Bulkhead bulkhead) {
        this.bulkhead = bulkhead;
    }

    public static class CircuitBreaker {

        private float failureRateThreshold = 50.0f;
        private int slidingWindowSize = 10;
        private int minimumNumberOfCalls = 5;
        private long waitDurationInOpenStateMillis = 30000;
        private int permittedNumberOfCallsInHalfOpenState = 3;

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

        public long getWaitDurationInOpenStateMillis() {
            return waitDurationInOpenStateMillis;
        }

        public void setWaitDurationInOpenStateMillis(long waitDurationInOpenStateMillis) {
            this.waitDurationInOpenStateMillis = waitDurationInOpenStateMillis;
        }

        public int getPermittedNumberOfCallsInHalfOpenState() {
            return permittedNumberOfCallsInHalfOpenState;
        }

        public void setPermittedNumberOfCallsInHalfOpenState(int permittedNumberOfCallsInHalfOpenState) {
            this.permittedNumberOfCallsInHalfOpenState = permittedNumberOfCallsInHalfOpenState;
        }
    }

    public static class Retry {

        private int maxAttempts = 3;
        private long waitDurationMillis = 1000;
        private double multiplier = 2.0;

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public long getWaitDurationMillis() {
            return waitDurationMillis;
        }

        public void setWaitDurationMillis(long waitDurationMillis) {
            this.waitDurationMillis = waitDurationMillis;
        }

        public double getMultiplier() {
            return multiplier;
        }

        public void setMultiplier(double multiplier) {
            this.multiplier = multiplier;
        }
    }

    public static class Bulkhead {

        private int maxConcurrentCalls = 25;
        private long maxWaitDurationMillis = 0;

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
}
