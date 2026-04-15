package com.ftgo.observability.resilience;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tests for rate limiter behavior — limits request rate per service. */
@DisplayName("Rate Limiter")
class RateLimiterTest {

    private RateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        RateLimiterConfig config =
                RateLimiterConfig.custom()
                        .limitForPeriod(5)
                        .limitRefreshPeriod(Duration.ofSeconds(1))
                        .timeoutDuration(Duration.ofMillis(100))
                        .build();

        RateLimiterRegistry registry = RateLimiterRegistry.of(config);
        rateLimiter = registry.rateLimiter("test-service");
    }

    @Test
    @DisplayName("should allow calls within rate limit")
    void rateLimiter_withinLimit_allowsCalls() {
        for (int i = 0; i < 5; i++) {
            String result = RateLimiter.decorateSupplier(rateLimiter, () -> "success").get();
            assertThat(result).isEqualTo("success");
        }
    }

    @Test
    @DisplayName("should reject calls exceeding rate limit")
    void rateLimiter_exceedsLimit_rejectsCalls() {
        // Use up all permits
        for (int i = 0; i < 5; i++) {
            RateLimiter.decorateSupplier(rateLimiter, () -> "ok").get();
        }

        // Next call should be rejected
        assertThatThrownBy(() -> RateLimiter.decorateSupplier(rateLimiter, () -> "overflow").get())
                .isInstanceOf(RequestNotPermitted.class);
    }

    @Test
    @DisplayName("should track rate limiter metrics")
    void rateLimiter_metrics_tracksAvailablePermissions() {
        RateLimiter.Metrics metrics = rateLimiter.getMetrics();
        assertThat(metrics.getAvailablePermissions()).isEqualTo(5);

        // Use one permit
        RateLimiter.decorateSupplier(rateLimiter, () -> "ok").get();

        assertThat(rateLimiter.getMetrics().getAvailablePermissions()).isEqualTo(4);
    }
}
