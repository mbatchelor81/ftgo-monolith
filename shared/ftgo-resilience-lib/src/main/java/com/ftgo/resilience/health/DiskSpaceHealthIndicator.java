package com.ftgo.resilience.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Custom health indicator that monitors available disk space.
 *
 * <p>Reports the service as unhealthy when free disk space drops below
 * a configurable threshold (default: 100 MB). This prevents services
 * from failing due to full disks when writing logs or temporary files.
 *
 * <p>This indicator is registered under the key {@code ftgoDiskSpace} in the
 * actuator health endpoint.
 */
@Component("ftgoDiskSpace")
public class DiskSpaceHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(DiskSpaceHealthIndicator.class);
    private static final long BYTES_PER_MB = 1024L * 1024L;

    private final long thresholdBytes;
    private final String path;

    public DiskSpaceHealthIndicator(
            @Value("${ftgo.health.disk-space.threshold-mb:100}") long thresholdMb,
            @Value("${ftgo.health.disk-space.path:/}") String path) {
        this.thresholdBytes = thresholdMb * BYTES_PER_MB;
        this.path = path;
    }

    @Override
    public Health health() {
        try {
            File diskPartition = new File(path);
            long freeSpace = diskPartition.getFreeSpace();
            long totalSpace = diskPartition.getTotalSpace();
            long usableSpace = diskPartition.getUsableSpace();

            Health.Builder builder = (freeSpace >= thresholdBytes)
                    ? Health.up()
                    : Health.down().withDetail("error",
                        String.format("Free disk space (%d MB) below threshold (%d MB)",
                                freeSpace / BYTES_PER_MB, thresholdBytes / BYTES_PER_MB));

            return builder
                    .withDetail("total", formatBytes(totalSpace))
                    .withDetail("free", formatBytes(freeSpace))
                    .withDetail("usable", formatBytes(usableSpace))
                    .withDetail("threshold", formatBytes(thresholdBytes))
                    .withDetail("path", path)
                    .build();

        } catch (Exception e) {
            log.error("Disk space health check failed", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }

    private String formatBytes(long bytes) {
        return String.format("%d MB", bytes / BYTES_PER_MB);
    }
}
