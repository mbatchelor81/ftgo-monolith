package com.ftgo.resilience.discovery;

import com.ftgo.resilience.config.ServiceDiscoveryProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Kubernetes DNS-based service registry for FTGO microservices.
 *
 * <p>Resolves service names to their cluster-internal URLs using the Kubernetes
 * DNS naming convention: {@code <service-name>.<namespace>.svc.<cluster-domain>}
 *
 * <p>Example usage:
 * <pre>
 * String orderServiceUrl = serviceRegistry.getServiceUrl("ftgo-order-service");
 * // Returns: http://ftgo-order-service.ftgo.svc.cluster.local:8080
 * </pre>
 *
 * <p>This approach leverages Kubernetes' built-in DNS resolution, eliminating
 * the need for external service discovery tools like Eureka or Consul.
 */
public class KubernetesServiceRegistry {

    private static final Logger log = LoggerFactory.getLogger(KubernetesServiceRegistry.class);

    private final ServiceDiscoveryProperties properties;

    public KubernetesServiceRegistry(ServiceDiscoveryProperties properties) {
        this.properties = properties;
    }

    /**
     * Returns the fully qualified cluster-internal URL for a service.
     *
     * @param serviceName the Kubernetes Service name (e.g., "ftgo-order-service")
     * @return the full URL (e.g., "http://ftgo-order-service.ftgo.svc.cluster.local:8080")
     */
    public String getServiceUrl(String serviceName) {
        String url = String.format("%s://%s.%s.svc.%s:%d",
                properties.getDefaultScheme(),
                serviceName,
                properties.getNamespace(),
                properties.getClusterDomain(),
                properties.getDefaultPort());
        log.debug("Resolved service '{}' to URL: {}", serviceName, url);
        return url;
    }

    /**
     * Returns the fully qualified cluster-internal URL for a service with a custom port.
     *
     * @param serviceName the Kubernetes Service name
     * @param port        the service port
     * @return the full URL
     */
    public String getServiceUrl(String serviceName, int port) {
        String url = String.format("%s://%s.%s.svc.%s:%d",
                properties.getDefaultScheme(),
                serviceName,
                properties.getNamespace(),
                properties.getClusterDomain(),
                port);
        log.debug("Resolved service '{}' to URL: {}", serviceName, url);
        return url;
    }

    /**
     * Returns the Kubernetes DNS hostname for a service (without scheme or port).
     *
     * @param serviceName the Kubernetes Service name
     * @return the DNS hostname (e.g., "ftgo-order-service.ftgo.svc.cluster.local")
     */
    public String getServiceHost(String serviceName) {
        return String.format("%s.%s.svc.%s",
                serviceName,
                properties.getNamespace(),
                properties.getClusterDomain());
    }
}
