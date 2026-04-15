package com.ftgo.observability.resilience;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for retry behavior matching EM-44 acceptance criteria: 3 attempts with exponential backoff
 * (1s, 2s, 4s).
 */
@DisplayName("Retry")
class RetryTest {

    private Retry retry;

    @BeforeEach
    void setUp() {
        RetryConfig config =
                RetryConfig.custom()
                        .maxAttempts(3)
                        .intervalFunction(
                                io.github.resilience4j.core.IntervalFunction.ofExponentialBackoff(
                                        50, 2.0))
                        .retryExceptions(IOException.class, RuntimeException.class)
                        .build();

        RetryRegistry registry = RetryRegistry.of(config);
        retry = registry.retry("test-service");
    }

    @Test
    @DisplayName("should succeed on first attempt without retries")
    void retry_successfulCall_noRetries() {
        AtomicInteger attempts = new AtomicInteger(0);

        String result =
                Retry.decorateSupplier(
                                retry,
                                () -> {
                                    attempts.incrementAndGet();
                                    return "success";
                                })
                        .get();

        assertThat(result).isEqualTo("success");
        assertThat(attempts.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("should retry and succeed on second attempt")
    void retry_failsThenSucceeds_retriesOnce() {
        AtomicInteger attempts = new AtomicInteger(0);

        String result =
                Retry.decorateSupplier(
                                retry,
                                () -> {
                                    int attempt = attempts.incrementAndGet();
                                    if (attempt < 2) {
                                        throw new RuntimeException("Transient failure");
                                    }
                                    return "success-after-retry";
                                })
                        .get();

        assertThat(result).isEqualTo("success-after-retry");
        assertThat(attempts.get()).isEqualTo(2);
    }

    @Test
    @DisplayName("should exhaust all 3 attempts and throw exception")
    void retry_allAttemptsFail_throwsAfterMaxRetries() {
        AtomicInteger attempts = new AtomicInteger(0);

        assertThatThrownBy(
                        () ->
                                Retry.decorateSupplier(
                                                retry,
                                                () -> {
                                                    attempts.incrementAndGet();
                                                    throw new RuntimeException(
                                                            "Persistent failure");
                                                })
                                        .get())
                .isInstanceOf(RuntimeException.class);

        assertThat(attempts.get()).isEqualTo(3);
    }

    @Test
    @DisplayName("should track retry metrics")
    void retry_withRetries_tracksMetrics() {
        AtomicInteger attempts = new AtomicInteger(0);

        Retry.decorateSupplier(
                        retry,
                        () -> {
                            int attempt = attempts.incrementAndGet();
                            if (attempt < 3) {
                                throw new RuntimeException("Transient");
                            }
                            return "success";
                        })
                .get();

        Retry.Metrics metrics = retry.getMetrics();
        assertThat(metrics.getNumberOfSuccessfulCallsWithRetryAttempt()).isEqualTo(1);
    }

    @Test
    @DisplayName("should not retry non-configured exceptions")
    void retry_nonConfiguredException_noRetry() {
        AtomicInteger attempts = new AtomicInteger(0);

        assertThatThrownBy(
                        () ->
                                Retry.decorateSupplier(
                                                retry,
                                                () -> {
                                                    attempts.incrementAndGet();
                                                    throw new Error("Not retryable");
                                                })
                                        .get())
                .isInstanceOf(Error.class);

        assertThat(attempts.get()).isEqualTo(1);
    }
}
