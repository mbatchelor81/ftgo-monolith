package net.chrisrichardson.ftgo.resilience.health;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

public class OrderServiceHealthIndicator implements HealthIndicator {

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public OrderServiceHealthIndicator(CircuitBreakerRegistry circuitBreakerRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    @Override
    public Health health() {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("orderService");
        CircuitBreaker.State state = cb.getState();
        CircuitBreaker.Metrics metrics = cb.getMetrics();

        Health.Builder builder = (state == CircuitBreaker.State.OPEN || state == CircuitBreaker.State.FORCED_OPEN)
                ? Health.down()
                : Health.up();

        return builder
                .withDetail("service", "order-service")
                .withDetail("circuitBreaker.state", state.name())
                .withDetail("circuitBreaker.failureRate", metrics.getFailureRate())
                .withDetail("circuitBreaker.bufferedCalls", metrics.getNumberOfBufferedCalls())
                .withDetail("circuitBreaker.failedCalls", metrics.getNumberOfFailedCalls())
                .withDetail("circuitBreaker.successfulCalls", metrics.getNumberOfSuccessfulCalls())
                .build();
    }
}
