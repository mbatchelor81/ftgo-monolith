package com.ftgo.resilience.health;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.assertj.core.api.Assertions.assertThat;

class DiskSpaceHealthIndicatorTest {

    @Test
    void health_withSufficientDiskSpace_returnsUp() {
        // Threshold of 1 MB should always pass on any system
        DiskSpaceHealthIndicator indicator = new DiskSpaceHealthIndicator(1, "/");

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsKeys("total", "free", "usable", "threshold", "path");
    }

    @Test
    void health_withExtremeThreshold_returnsDown() {
        // Threshold of 999999999 MB (exabytes) should always fail
        DiskSpaceHealthIndicator indicator = new DiskSpaceHealthIndicator(999999999, "/");

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsKey("error");
    }
}
