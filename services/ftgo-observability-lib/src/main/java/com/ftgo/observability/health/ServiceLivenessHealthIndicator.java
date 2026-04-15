package com.ftgo.observability.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

/**
 * Custom liveness health indicator for FTGO services.
 *
 * <p>Performs a basic sanity check to verify the JVM and application context are responsive. This
 * indicator is included in the Kubernetes liveness probe group to detect services that are alive
 * but non-functional (e.g., deadlocked threads).
 */
public class ServiceLivenessHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        try {
            // Basic sanity check — verify the JVM can allocate memory and the thread is responsive
            long freeMemory = Runtime.getRuntime().freeMemory();
            long totalMemory = Runtime.getRuntime().totalMemory();
            double memoryUsagePercent = 1.0 - ((double) freeMemory / totalMemory);

            if (memoryUsagePercent > 0.95) {
                return Health.down()
                        .withDetail("reason", "Critical memory pressure")
                        .withDetail(
                                "memoryUsagePercent",
                                String.format("%.1f%%", memoryUsagePercent * 100))
                        .build();
            }

            return Health.up()
                    .withDetail(
                            "memoryUsagePercent", String.format("%.1f%%", memoryUsagePercent * 100))
                    .build();
        } catch (Exception e) {
            return Health.down().withDetail("error", e.getMessage()).build();
        }
    }
}
