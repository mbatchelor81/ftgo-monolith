package com.ftgo.common.resilience.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.web.client.RestTemplate;

/**
 * Health indicator that checks reachability of a downstream service
 * via its actuator health endpoint.
 *
 * <p>Register one instance per downstream dependency in each service's
 * configuration to surface dependency health in the composite
 * {@code /actuator/health} response.
 */
public class DownstreamServiceHealthIndicator implements HealthIndicator {

    private final String serviceName;
    private final String healthUrl;
    private final RestTemplate restTemplate;

    public DownstreamServiceHealthIndicator(String serviceName, String healthUrl) {
        this.serviceName = serviceName;
        this.healthUrl = healthUrl;
        this.restTemplate = new RestTemplate();
    }

    public DownstreamServiceHealthIndicator(String serviceName, String healthUrl, RestTemplate restTemplate) {
        this.serviceName = serviceName;
        this.healthUrl = healthUrl;
        this.restTemplate = restTemplate;
    }

    @Override
    public Health health() {
        try {
            restTemplate.getForEntity(healthUrl, String.class);
            return Health.up()
                    .withDetail("service", serviceName)
                    .withDetail("url", healthUrl)
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("service", serviceName)
                    .withDetail("url", healthUrl)
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
