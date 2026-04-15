package com.ftgo.orderservice.health;

import com.ftgo.observability.discovery.ServiceDiscoveryProperties;
import com.ftgo.observability.health.DownstreamServiceHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Health indicator configuration for the Order Service.
 *
 * <p>Registers downstream health checks for services that the Order Service depends on: Consumer
 * Service (for order validation) and Restaurant Service (for menu lookups).
 */
@Configuration
public class OrderServiceHealthConfiguration {

    @Bean
    public HealthIndicator consumerServiceHealth(ServiceDiscoveryProperties discovery) {
        String url = discovery.resolveServiceUrl("ftgo-consumer-service");
        return new DownstreamServiceHealthIndicator("consumer-service", url);
    }

    @Bean
    public HealthIndicator restaurantServiceHealth(ServiceDiscoveryProperties discovery) {
        String url = discovery.resolveServiceUrl("ftgo-restaurant-service");
        return new DownstreamServiceHealthIndicator("restaurant-service", url);
    }

    @Bean
    public HealthIndicator courierServiceHealth(ServiceDiscoveryProperties discovery) {
        String url = discovery.resolveServiceUrl("ftgo-courier-service");
        return new DownstreamServiceHealthIndicator("courier-service", url);
    }
}
