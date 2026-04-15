package com.ftgo.courierservice.health;

import com.ftgo.observability.discovery.ServiceDiscoveryProperties;
import com.ftgo.observability.health.DownstreamServiceHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Health indicator configuration for the Courier Service.
 *
 * <p>Registers downstream health checks for the Order Service, which the Courier Service
 * communicates with for delivery scheduling.
 */
@Configuration
public class CourierServiceHealthConfiguration {

    @Bean
    public HealthIndicator orderServiceHealth(ServiceDiscoveryProperties discovery) {
        String url = discovery.resolveServiceUrl("ftgo-order-service");
        return new DownstreamServiceHealthIndicator("order-service", url);
    }
}
