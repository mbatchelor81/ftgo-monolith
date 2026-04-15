package com.ftgo.observability.health;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

/** Tests for the ServiceLivenessHealthIndicator. */
@DisplayName("ServiceLivenessHealthIndicator")
class ServiceLivenessHealthIndicatorTest {

    @Test
    @DisplayName("should report UP when JVM is healthy")
    void health_normalConditions_reportsUp() {
        ServiceLivenessHealthIndicator indicator = new ServiceLivenessHealthIndicator();

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsKey("memoryUsagePercent");
    }
}
