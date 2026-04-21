package net.chrisrichardson.ftgo.resilience.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Bindings for {@code ftgo.resilience.*} and {@code ftgo.services.*}
 * configuration. Kept in one class so every service has a single place to
 * declare which downstream services it depends on and the Kubernetes DNS
 * coordinates used to reach them.
 *
 * <p>Example {@code application.yml}:
 * <pre>
 * ftgo:
 *   services:
 *     consumer-service:
 *       base-url: http://consumer-service.ftgo.svc.cluster.local:8080
 *     restaurant-service:
 *       base-url: http://restaurant-service.ftgo.svc.cluster.local:8080
 *   resilience:
 *     dependent-services:
 *       - consumer-service
 *       - restaurant-service
 * </pre>
 */
@ConfigurationProperties(prefix = "ftgo")
public class FtgoResilienceProperties {

    /**
     * Map of {@code ftgo.services.<name>} entries. The key is the logical
     * service name (and, by convention, its Kubernetes {@code Service}
     * name); the value carries the base URL used for service-to-service
     * calls.
     */
    private Map<String, ServiceEndpoint> services = new LinkedHashMap<>();

    private ResilienceSettings resilience = new ResilienceSettings();

    public Map<String, ServiceEndpoint> getServices() {
        return services;
    }

    public void setServices(Map<String, ServiceEndpoint> services) {
        this.services = services;
    }

    public ResilienceSettings getResilience() {
        return resilience;
    }

    public void setResilience(ResilienceSettings resilience) {
        this.resilience = resilience;
    }

    /**
     * Coordinates for a downstream service reachable through the
     * Kubernetes cluster DNS.
     */
    public static class ServiceEndpoint {

        /**
         * Fully-qualified base URL including scheme, host, and port.
         * Populated from environment-specific config so deployments can
         * point at local compose, staging, or prod clusters without code
         * changes.
         */
        private String baseUrl;

        /**
         * Path (default {@code /actuator/health}) used when probing the
         * downstream service from this service's
         * {@code DependentServiceHealthIndicator}.
         */
        private String healthPath = "/actuator/health";

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getHealthPath() {
            return healthPath;
        }

        public void setHealthPath(String healthPath) {
            this.healthPath = healthPath;
        }
    }

    /**
     * Service-level resilience settings. The detailed circuit-breaker,
     * retry, and bulkhead configuration is expressed via the standard
     * Resilience4j Spring Boot keys (e.g.
     * {@code resilience4j.circuitbreaker.instances.*}) — this class only
     * carries the FTGO-specific bits.
     */
    public static class ResilienceSettings {

        /**
         * Logical names of downstream services whose health should roll
         * up into this service's readiness probe. Entries must appear as
         * keys in {@link FtgoResilienceProperties#services}.
         */
        private java.util.List<String> dependentServices = new java.util.ArrayList<>();

        /**
         * Name of the default Resilience4j circuit breaker / retry /
         * bulkhead configuration that outbound calls fall back to when no
         * per-instance overrides are provided.
         */
        private String defaultInstanceName = "default";

        public java.util.List<String> getDependentServices() {
            return dependentServices;
        }

        public void setDependentServices(java.util.List<String> dependentServices) {
            this.dependentServices = dependentServices;
        }

        public String getDefaultInstanceName() {
            return defaultInstanceName;
        }

        public void setDefaultInstanceName(String defaultInstanceName) {
            this.defaultInstanceName = defaultInstanceName;
        }
    }
}
