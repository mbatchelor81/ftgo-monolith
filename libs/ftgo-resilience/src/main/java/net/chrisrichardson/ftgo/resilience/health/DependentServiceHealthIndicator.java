package net.chrisrichardson.ftgo.resilience.health;

import java.time.Duration;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.web.reactive.function.client.WebClient;

import net.chrisrichardson.ftgo.resilience.client.ServiceEndpoints;

/**
 * Rolls up the liveness of one downstream FTGO service into the caller's
 * Spring Boot Actuator health endpoint by hitting the dependency's
 * {@code /actuator/health} and converting the response status to a
 * {@link Health} object.
 *
 * <p>One indicator bean is registered per entry in
 * {@code ftgo.resilience.dependent-services}. The bean name becomes
 * {@code <serviceName>HealthIndicator}, which Spring Boot abbreviates to
 * {@code <serviceName>} in the health JSON — so operators see a clean
 * per-dependency breakdown in the readiness probe output.
 *
 * <p>Timeouts are deliberately short so the probe never stalls and so a
 * slow dependency does not starve the pod's own readiness.
 */
public class DependentServiceHealthIndicator implements HealthIndicator {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(2);

    private final String serviceName;
    private final String healthUrl;
    private final WebClient webClient;

    public DependentServiceHealthIndicator(String serviceName,
                                           ServiceEndpoints endpoints,
                                           WebClient.Builder webClientBuilder) {
        this.serviceName = serviceName;
        this.healthUrl = endpoints.healthUrl(serviceName);
        this.webClient = webClientBuilder.build();
    }

    @Override
    public Health health() {
        try {
            String body = webClient.get()
                    .uri(healthUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(DEFAULT_TIMEOUT)
                    .block();
            return Health.up()
                    .withDetail("service", serviceName)
                    .withDetail("url", healthUrl)
                    .withDetail("payload", body == null ? "" : body)
                    .build();
        } catch (RuntimeException ex) {
            return Health.down(ex)
                    .withDetail("service", serviceName)
                    .withDetail("url", healthUrl)
                    .build();
        }
    }
}
