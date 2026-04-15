package com.ftgo.consumerservice.health;

import com.ftgo.observability.discovery.ServiceDiscoveryProperties;
import com.ftgo.observability.health.DownstreamServiceHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Health indicator configuration for the Consumer Service.
 *
 * <p>Registers downstream health checks for the Order Service, which the Consumer Service
 * communicates with.
 */
@Configuration
public class ConsumerServiceHealthConfiguration {

    @Bean
    public HealthIndicator orderServiceHealth(ServiceDiscoveryProperties discovery) {
        String url = discovery.resolveServiceUrl("ftgo-order-service");
        return new DownstreamServiceHealthIndicator("order-service", url);
    }
}
