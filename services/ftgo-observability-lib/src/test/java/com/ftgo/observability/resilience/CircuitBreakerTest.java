package com.ftgo.observability.resilience;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.io.IOException;
import java.time.Duration;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for circuit breaker behavior matching EM-44 acceptance criteria: 5 failures → open state,
 * 30s half-open wait.
 */
@DisplayName("Circuit Breaker")
class CircuitBreakerTest {

    private CircuitBreaker circuitBreaker;

    @BeforeEach
    void setUp() {
        CircuitBreakerConfig config =
                CircuitBreakerConfig.custom()
                        .failureRateThreshold(50)
                        .slidingWindowSize(10)
                        .minimumNumberOfCalls(5)
                        .waitDurationInOpenState(Duration.ofSeconds(30))
                        .permittedNumberOfCallsInHalfOpenState(3)
                        .recordExceptions(IOException.class, RuntimeException.class)
                        .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        circuitBreaker = registry.circuitBreaker("test-service");
    }

    @Test
    @DisplayName("should start in CLOSED state")
    void circuitBreaker_initialState_isClosed() {
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    @DisplayName("should remain CLOSED when calls succeed")
    void circuitBreaker_successfulCalls_remainsClosed() {
        for (int i = 0; i < 10; i++) {
            circuitBreaker.executeSupplier(() -> "success");
        }
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    @DisplayName(
            "should transition to OPEN after 5 failures (50% failure rate with 10-call window)")
    void circuitBreaker_fiveFailures_transitionsToOpen() {
        // Fill the sliding window: 5 successes + 5 failures = 50% failure rate
        for (int i = 0; i < 5; i++) {
            circuitBreaker.executeSupplier(() -> "success");
        }

        for (int i = 0; i < 5; i++) {
            try {
                circuitBreaker.executeSupplier(failingSupplier());
            } catch (Exception ignored) {
                // Expected
            }
        }

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }

    @Test
    @DisplayName("should reject calls in OPEN state")
    void circuitBreaker_openState_rejectsCalls() {
        // Trip the circuit breaker
        tripCircuitBreaker();

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
        assertThatThrownBy(() -> circuitBreaker.executeSupplier(() -> "should-not-execute"))
                .isInstanceOf(
                        io.github.resilience4j.circuitbreaker.CallNotPermittedException.class);
    }

    @Test
    @DisplayName("should track failure metrics")
    void circuitBreaker_failures_tracksMetrics() {
        // Execute some successful calls
        for (int i = 0; i < 3; i++) {
            circuitBreaker.executeSupplier(() -> "success");
        }

        // Execute some failing calls
        for (int i = 0; i < 2; i++) {
            try {
                circuitBreaker.executeSupplier(failingSupplier());
            } catch (Exception ignored) {
                // Expected
            }
        }

        CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();
        assertThat(metrics.getNumberOfSuccessfulCalls()).isEqualTo(3);
        assertThat(metrics.getNumberOfFailedCalls()).isEqualTo(2);
    }

    @Test
    @DisplayName("should allow forced transition to HALF_OPEN for testing")
    void circuitBreaker_forcedHalfOpen_permitsLimitedCalls() {
        tripCircuitBreaker();
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        // Force transition to HALF_OPEN
        circuitBreaker.transitionToHalfOpenState();
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.HALF_OPEN);

        // Should allow permitted number of calls (3)
        for (int i = 0; i < 3; i++) {
            circuitBreaker.executeSupplier(() -> "success");
        }

        // After successful calls in HALF_OPEN, should transition back to CLOSED
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    private void tripCircuitBreaker() {
        for (int i = 0; i < 5; i++) {
            circuitBreaker.executeSupplier(() -> "success");
        }
        for (int i = 0; i < 5; i++) {
            try {
                circuitBreaker.executeSupplier(failingSupplier());
            } catch (Exception ignored) {
                // Expected
            }
        }
    }

    private Supplier<String> failingSupplier() {
        return () -> {
            throw new RuntimeException("Connection refused");
        };
    }
}
