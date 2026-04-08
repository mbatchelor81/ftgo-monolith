package com.ftgo.resilience.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

/**
 * Abstract base class for service-specific business health indicators.
 *
 * <p>Each microservice should extend this class to implement domain-specific
 * health checks. For example, the Order Service might verify that it can
 * reach the Restaurant Service, or that the order processing pipeline is
 * functioning correctly.
 *
 * <p>Example usage:
 * <pre>
 * &#64;Component("orderProcessing")
 * public class OrderProcessingHealthIndicator extends BusinessHealthIndicator {
 *     &#64;Override
 *     protected Health doHealthCheck() {
 *         // Check order processing pipeline
 *         return Health.up().withDetail("pendingOrders", 42).build();
 *     }
 * }
 * </pre>
 */
public abstract class BusinessHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(BusinessHealthIndicator.class);

    @Override
    public Health health() {
        try {
            return doHealthCheck();
        } catch (Exception e) {
            log.error("Business health check failed for {}", getClass().getSimpleName(), e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("indicator", getClass().getSimpleName())
                    .build();
        }
    }

    /**
     * Perform the service-specific health check.
     *
     * @return the health status with relevant details
     */
    protected abstract Health doHealthCheck();
}
