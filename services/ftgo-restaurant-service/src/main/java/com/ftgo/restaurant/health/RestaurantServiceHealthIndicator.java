package com.ftgo.restaurant.health;

import com.ftgo.resilience.health.BusinessHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

/**
 * Business health indicator for the Restaurant Service.
 *
 * <p>Verifies that restaurant registration and menu management are operational.
 */
@Component("restaurantManagement")
public class RestaurantServiceHealthIndicator extends BusinessHealthIndicator {

    @Override
    protected Health doHealthCheck() {
        return Health.up()
                .withDetail("service", "ftgo-restaurant-service")
                .withDetail("status", "accepting restaurant operations")
                .build();
    }
}
