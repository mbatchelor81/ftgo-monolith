package com.ftgo.observability.resilience;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tests for bulkhead behavior — limits concurrent calls per service. */
@DisplayName("Bulkhead")
class BulkheadTest {

    private Bulkhead bulkhead;

    @BeforeEach
    void setUp() {
        BulkheadConfig config =
                BulkheadConfig.custom()
                        .maxConcurrentCalls(3)
                        .maxWaitDuration(Duration.ofMillis(100))
                        .build();

        BulkheadRegistry registry = BulkheadRegistry.of(config);
        bulkhead = registry.bulkhead("test-service");
    }

    @Test
    @DisplayName("should allow calls within concurrency limit")
    void bulkhead_withinLimit_allowsCalls() {
        String result = Bulkhead.decorateSupplier(bulkhead, () -> "success").get();
        assertThat(result).isEqualTo("success");
    }

    @Test
    @DisplayName("should reject calls exceeding concurrency limit")
    void bulkhead_exceedsLimit_rejectsCalls() throws InterruptedException {
        CountDownLatch holdLatch = new CountDownLatch(1);
        CountDownLatch startedLatch = new CountDownLatch(3);
        ExecutorService executor = Executors.newFixedThreadPool(4);
        AtomicInteger rejections = new AtomicInteger(0);

        try {
            // Fill the bulkhead with 3 blocking calls
            for (int i = 0; i < 3; i++) {
                executor.submit(
                        () -> {
                            try {
                                Bulkhead.decorateRunnable(
                                                bulkhead,
                                                () -> {
                                                    startedLatch.countDown();
                                                    try {
                                                        holdLatch.await(5, TimeUnit.SECONDS);
                                                    } catch (InterruptedException e) {
                                                        Thread.currentThread().interrupt();
                                                    }
                                                })
                                        .run();
                            } catch (BulkheadFullException e) {
                                rejections.incrementAndGet();
                            }
                        });
            }

            // Wait for all 3 calls to start
            startedLatch.await(5, TimeUnit.SECONDS);

            // Try one more call — should be rejected
            try {
                Bulkhead.decorateSupplier(bulkhead, () -> "overflow").get();
            } catch (BulkheadFullException e) {
                rejections.incrementAndGet();
            }

            assertThat(rejections.get()).isGreaterThanOrEqualTo(1);
        } finally {
            holdLatch.countDown();
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("should track available concurrent call permits")
    void bulkhead_metrics_tracksAvailablePermits() {
        Bulkhead.Metrics metrics = bulkhead.getMetrics();
        assertThat(metrics.getAvailableConcurrentCalls()).isEqualTo(3);
        assertThat(metrics.getMaxAllowedConcurrentCalls()).isEqualTo(3);
    }
}
