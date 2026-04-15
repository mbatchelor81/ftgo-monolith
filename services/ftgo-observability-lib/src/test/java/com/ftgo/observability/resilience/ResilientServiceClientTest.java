package com.ftgo.observability.resilience;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tests for the ResilientServiceClient combining all resilience patterns. */
@DisplayName("ResilientServiceClient")
class ResilientServiceClientTest {

    private ResilientServiceClient client;
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeEach
    void setUp() {
        CircuitBreakerConfig cbConfig =
                CircuitBreakerConfig.custom()
                        .failureRateThreshold(50)
                        .slidingWindowSize(10)
                        .minimumNumberOfCalls(5)
                        .waitDurationInOpenState(Duration.ofSeconds(30))
                        .permittedNumberOfCallsInHalfOpenState(3)
                        .recordExceptions(IOException.class, RuntimeException.class)
                        .build();

        RetryConfig retryConfig =
                RetryConfig.custom()
                        .maxAttempts(3)
                        .waitDuration(Duration.ofMillis(50))
                        .retryExceptions(IOException.class, RuntimeException.class)
                        .build();

        BulkheadConfig bulkheadConfig =
                BulkheadConfig.custom()
                        .maxConcurrentCalls(25)
                        .maxWaitDuration(Duration.ofMillis(500))
                        .build();

        RateLimiterConfig rateLimiterConfig =
                RateLimiterConfig.custom()
                        .limitForPeriod(50)
                        .limitRefreshPeriod(Duration.ofSeconds(1))
                        .timeoutDuration(Duration.ofMillis(500))
                        .build();

        circuitBreakerRegistry = CircuitBreakerRegistry.of(cbConfig);

        client =
                new ResilientServiceClient(
                        "test-downstream",
                        circuitBreakerRegistry,
                        RetryRegistry.of(retryConfig),
                        BulkheadRegistry.of(bulkheadConfig),
                        RateLimiterRegistry.of(rateLimiterConfig));
    }

    @Test
    @DisplayName("should execute successful calls through all patterns")
    void execute_successfulCall_returnsResult() {
        String result = client.execute(() -> "response-data");
        assertThat(result).isEqualTo("response-data");
    }

    @Test
    @DisplayName("should retry transient failures and succeed")
    void execute_transientFailure_retriesAndSucceeds() {
        AtomicInteger attempts = new AtomicInteger(0);

        String result =
                client.execute(
                        () -> {
                            int attempt = attempts.incrementAndGet();
                            if (attempt < 2) {
                                throw new RuntimeException(new IOException("Transient"));
                            }
                            return "success-after-retry";
                        });

        assertThat(result).isEqualTo("success-after-retry");
        assertThat(attempts.get()).isEqualTo(2);
    }

    @Test
    @DisplayName("should expose circuit breaker for inspection")
    void getCircuitBreaker_returnsNamedInstance() {
        CircuitBreaker cb = client.getCircuitBreaker();
        assertThat(cb.getName()).isEqualTo("test-downstream");
        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    @DisplayName("should execute runnable through all patterns")
    void executeRunnable_successfulCall_completes() {
        AtomicInteger counter = new AtomicInteger(0);
        client.execute((Runnable) counter::incrementAndGet);
        assertThat(counter.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("should propagate exceptions after exhausting retries")
    void execute_persistentFailure_throwsAfterRetries() {
        assertThatThrownBy(
                        () ->
                                client.execute(
                                        () -> {
                                            throw new RuntimeException(
                                                    new IOException("Persistent failure"));
                                        }))
                .isInstanceOf(RuntimeException.class);
    }
}
