package com.ftgo.observability.health;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

/** Tests for the DownstreamServiceHealthIndicator. */
@DisplayName("DownstreamServiceHealthIndicator")
class DownstreamServiceHealthIndicatorTest {

    @Test
    @DisplayName("should report DOWN when downstream service is unreachable")
    void health_unreachableService_reportsDown() {
        DownstreamServiceHealthIndicator indicator =
                new DownstreamServiceHealthIndicator("test-service", "http://localhost:19999");

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsKey("service");
        assertThat(health.getDetails().get("service")).isEqualTo("test-service");
        assertThat(health.getDetails()).containsKey("error");
    }

    @Test
    @DisplayName("should include service name and URL in health details")
    void health_details_containsServiceInfo() {
        DownstreamServiceHealthIndicator indicator =
                new DownstreamServiceHealthIndicator("order-service", "http://localhost:29999");

        Health health = indicator.health();

        assertThat(health.getDetails().get("service")).isEqualTo("order-service");
        assertThat(health.getDetails().get("url"))
                .isEqualTo("http://localhost:29999/actuator/health");
    }
}
