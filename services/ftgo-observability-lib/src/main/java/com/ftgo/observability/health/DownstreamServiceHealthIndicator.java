package com.ftgo.observability.health;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

/**
 * Health indicator that checks the availability of a downstream service via its health endpoint.
 *
 * <p>Used by each FTGO service to verify that dependent services are reachable. The check performs
 * an HTTP GET against {@code <baseUrl>/actuator/health} and reports UP if the response status is
 * 2xx.
 */
public class DownstreamServiceHealthIndicator implements HealthIndicator {

    private static final Logger LOG =
            LoggerFactory.getLogger(DownstreamServiceHealthIndicator.class);

    private final String serviceName;
    private final String healthUrl;
    private final HttpClient httpClient;

    public DownstreamServiceHealthIndicator(String serviceName, String baseUrl) {
        this.serviceName = serviceName;
        this.healthUrl = baseUrl + "/actuator/health";
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
    }

    @Override
    public Health health() {
        try {
            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(healthUrl))
                            .timeout(Duration.ofSeconds(5))
                            .GET()
                            .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return Health.up()
                        .withDetail("service", serviceName)
                        .withDetail("url", healthUrl)
                        .withDetail("status", response.statusCode())
                        .build();
            }
            return Health.down()
                    .withDetail("service", serviceName)
                    .withDetail("url", healthUrl)
                    .withDetail("status", response.statusCode())
                    .build();
        } catch (Exception e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getName();
            LOG.warn(
                    "Health check failed for downstream service {}: {}", serviceName, errorMessage);
            return Health.down()
                    .withDetail("service", serviceName)
                    .withDetail("url", healthUrl)
                    .withDetail("error", errorMessage)
                    .build();
        }
    }
}
