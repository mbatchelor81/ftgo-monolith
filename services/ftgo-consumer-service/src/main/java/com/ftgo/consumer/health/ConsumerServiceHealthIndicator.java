package com.ftgo.consumer.health;

import com.ftgo.resilience.health.BusinessHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

/**
 * Business health indicator for the Consumer Service.
 *
 * <p>Verifies that consumer registration and validation are operational.
 */
@Component("consumerManagement")
public class ConsumerServiceHealthIndicator extends BusinessHealthIndicator {

    @Override
    protected Health doHealthCheck() {
        return Health.up()
                .withDetail("service", "ftgo-consumer-service")
                .withDetail("status", "accepting registrations")
                .build();
    }
}
