package com.ftgo.consumerservice.health;

import com.ftgo.observability.discovery.ServiceDiscoveryProperties;
import com.ftgo.observability.health.DownstreamServiceHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Health indicator configuration for the Consumer Service.
 *
 * <p>The Consumer Service has no downstream service dependencies in the current architecture, but
 * this configuration is included to demonstrate the pattern and allow future additions.
 */
@Configuration
public class ConsumerServiceHealthConfiguration {

    @Bean
    public HealthIndicator orderServiceHealth(ServiceDiscoveryProperties discovery) {
        String url = discovery.resolveServiceUrl("ftgo-order-service");
        return new DownstreamServiceHealthIndicator("order-service", url);
    }
}
