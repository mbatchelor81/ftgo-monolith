package com.ftgo.observability.discovery;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Kubernetes-native DNS-based service discovery.
 *
 * <p>In Kubernetes, services are discoverable via DNS at: {@code
 * <service-name>.<namespace>.svc.cluster.local:<port>}
 *
 * <p>Each FTGO service declares the base URLs of its downstream dependencies under the {@code
 * ftgo.discovery} prefix. Example configuration:
 *
 * <pre>{@code
 * ftgo:
 *   discovery:
 *     namespace: ftgo
 *     services:
 *       order-service: http://ftgo-order-service.ftgo.svc.cluster.local:8080
 *       consumer-service: http://ftgo-consumer-service.ftgo.svc.cluster.local:8080
 * }</pre>
 */
@ConfigurationProperties(prefix = "ftgo.discovery")
public class ServiceDiscoveryProperties {

    /** The Kubernetes namespace where FTGO services are deployed. */
    private String namespace = "default";

    /** The cluster domain suffix used in Kubernetes DNS resolution. */
    private String clusterDomain = "svc.cluster.local";

    /** The default port for inter-service communication. */
    private int defaultPort = 8080;

    /** Map of service name to base URL. Overrides DNS-derived URLs when explicitly set. */
    private Map<String, String> services = new HashMap<>();

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getClusterDomain() {
        return clusterDomain;
    }

    public void setClusterDomain(String clusterDomain) {
        this.clusterDomain = clusterDomain;
    }

    public int getDefaultPort() {
        return defaultPort;
    }

    public void setDefaultPort(int defaultPort) {
        this.defaultPort = defaultPort;
    }

    public Map<String, String> getServices() {
        return services;
    }

    public void setServices(Map<String, String> services) {
        this.services = services;
    }

    /**
     * Resolves the base URL for a given service name.
     *
     * <p>If an explicit URL is configured in the {@code services} map, it is returned directly.
     * Otherwise, a Kubernetes DNS-based URL is constructed using the namespace and cluster domain.
     *
     * @param serviceName the logical name of the service (e.g., "ftgo-order-service")
     * @return the fully-qualified base URL for the service
     */
    public String resolveServiceUrl(String serviceName) {
        String explicitUrl = services.get(serviceName);
        if (explicitUrl != null && !explicitUrl.isBlank()) {
            return explicitUrl;
        }
        return String.format(
                "http://%s.%s.%s:%d", serviceName, namespace, clusterDomain, defaultPort);
    }
}
