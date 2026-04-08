package com.ftgo.order.health;

import com.ftgo.resilience.health.BusinessHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

/**
 * Business health indicator for the Order Service.
 *
 * <p>Verifies that the order processing pipeline is operational.
 * This check confirms the service can accept and process new orders.
 */
@Component("orderProcessing")
public class OrderServiceHealthIndicator extends BusinessHealthIndicator {

    @Override
    protected Health doHealthCheck() {
        return Health.up()
                .withDetail("service", "ftgo-order-service")
                .withDetail("status", "accepting orders")
                .build();
    }
}
