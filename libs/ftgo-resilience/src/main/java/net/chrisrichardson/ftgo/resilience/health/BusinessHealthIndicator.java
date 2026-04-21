package net.chrisrichardson.ftgo.resilience.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

/**
 * Base class for lightweight business health checks that each service
 * layers on top of the stock Spring Boot indicators (disk space,
 * database, ping). Typical subclasses verify invariants such as
 * "order-processing queue depth is under budget" or "restaurant catalog
 * is not empty" — anything a human operator would inspect before
 * declaring the service healthy.
 *
 * <p>Implementations return a {@link HealthStatus} so they do not need
 * to depend on the Actuator {@link Health} API directly — the base class
 * handles the conversion and pins a stable {@code component} detail so
 * the JSON rendering is uniform across services.
 */
public abstract class BusinessHealthIndicator implements HealthIndicator {

    private final String componentName;

    protected BusinessHealthIndicator(String componentName) {
        this.componentName = componentName;
    }

    @Override
    public final Health health() {
        HealthStatus status;
        try {
            status = check();
        } catch (RuntimeException ex) {
            return Health.down(ex).withDetail("component", componentName).build();
        }
        Health.Builder builder = status.healthy() ? Health.up() : Health.down();
        builder.withDetail("component", componentName);
        if (status.message() != null) {
            builder.withDetail("message", status.message());
        }
        status.details().forEach(builder::withDetail);
        return builder.build();
    }

    /**
     * @return the concrete business-level check result. Subclasses should
     *     return quickly; anything expensive belongs behind an async
     *     component and a cached last-known-good value.
     */
    protected abstract HealthStatus check();

    /**
     * Simple tri-detail payload returned from {@link #check()}.
     */
    public record HealthStatus(boolean healthy,
                                String message,
                                java.util.Map<String, Object> details) {

        public HealthStatus {
            if (details == null) {
                details = java.util.Map.of();
            }
        }

        public static HealthStatus up() {
            return new HealthStatus(true, null, java.util.Map.of());
        }

        public static HealthStatus up(String message) {
            return new HealthStatus(true, message, java.util.Map.of());
        }

        public static HealthStatus down(String message) {
            return new HealthStatus(false, message, java.util.Map.of());
        }
    }
}
