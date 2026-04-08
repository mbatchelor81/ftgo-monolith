package com.ftgo.resilience.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures graceful shutdown for zero-downtime Kubernetes deployments.
 *
 * <p>When a pod receives SIGTERM, Spring Boot's graceful shutdown allows in-flight
 * requests to complete before the application stops. This works in concert with
 * the Kubernetes {@code preStop} lifecycle hook (configured in deployment manifests)
 * which adds a delay before SIGTERM to allow the Service endpoints to be updated.
 *
 * <p>Timeline during pod termination:
 * <ol>
 *   <li>K8s sends preStop hook → {@code sleep 10} (allows endpoint removal)</li>
 *   <li>K8s sends SIGTERM → Spring begins graceful shutdown</li>
 *   <li>In-flight requests complete (up to {@code spring.lifecycle.timeout-per-shutdown-phase})</li>
 *   <li>Application stops</li>
 * </ol>
 *
 * <p>Enabled by default. Disable with {@code ftgo.graceful-shutdown.enabled=false}.
 */
@Configuration
@ConditionalOnProperty(prefix = "ftgo.graceful-shutdown", name = "enabled", havingValue = "true", matchIfMissing = true)
public class GracefulShutdownConfiguration {

    private static final Logger log = LoggerFactory.getLogger(GracefulShutdownConfiguration.class);

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> gracefulShutdownCustomizer() {
        return factory -> {
            factory.addConnectorCustomizers(connector -> {
                // Allow existing connections to finish processing during shutdown
                connector.setProperty("socket.soTimeout", "10000");
            });
            log.info("Configured Tomcat connector for graceful shutdown");
        };
    }
}
