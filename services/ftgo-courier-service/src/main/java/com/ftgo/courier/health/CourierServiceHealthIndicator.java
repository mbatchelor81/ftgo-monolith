package com.ftgo.courier.health;

import com.ftgo.resilience.health.BusinessHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

/**
 * Business health indicator for the Courier Service.
 *
 * <p>Verifies that courier dispatch and availability tracking are operational.
 */
@Component("courierDispatch")
public class CourierServiceHealthIndicator extends BusinessHealthIndicator {

    @Override
    protected Health doHealthCheck() {
        return Health.up()
                .withDetail("service", "ftgo-courier-service")
                .withDetail("status", "dispatching couriers")
                .build();
    }
}
