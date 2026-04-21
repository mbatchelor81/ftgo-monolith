package net.chrisrichardson.ftgo.resilience.client;

import java.util.Map;
import java.util.Objects;

import net.chrisrichardson.ftgo.resilience.config.FtgoResilienceProperties;
import net.chrisrichardson.ftgo.resilience.config.FtgoResilienceProperties.ServiceEndpoint;

/**
 * Read-only resolver for Kubernetes-native service endpoints. Backed by
 * the {@code ftgo.services.*} map so every service ships with explicit,
 * environment-specific DNS names (no Eureka / Consul client in the
 * classpath).
 *
 * <p>The resolver intentionally fails fast when a caller asks for a
 * service that has not been configured — missing wiring should surface
 * during startup or the first health check, not as a silent {@code null}
 * URL somewhere deep inside a request handler.
 */
public class ServiceEndpoints {

    private final Map<String, ServiceEndpoint> services;

    public ServiceEndpoints(FtgoResilienceProperties properties) {
        this.services = Objects.requireNonNull(properties, "properties").getServices();
    }

    /**
     * @return the configured base URL (scheme + host + port) for the
     *     given logical service name.
     * @throws IllegalStateException if the service is not configured or
     *     has a blank {@code base-url}.
     */
    public String baseUrl(String serviceName) {
        ServiceEndpoint endpoint = requireEndpoint(serviceName);
        String url = endpoint.getBaseUrl();
        if (url == null || url.isBlank()) {
            throw new IllegalStateException(
                    "ftgo.services." + serviceName + ".base-url is not configured");
        }
        return url;
    }

    /**
     * @return the fully qualified URL of the downstream health endpoint
     *     (base URL + configured health path).
     */
    public String healthUrl(String serviceName) {
        ServiceEndpoint endpoint = requireEndpoint(serviceName);
        String base = baseUrl(serviceName);
        String path = endpoint.getHealthPath();
        if (path == null || path.isBlank()) {
            return base;
        }
        if (base.endsWith("/") && path.startsWith("/")) {
            return base + path.substring(1);
        }
        if (!base.endsWith("/") && !path.startsWith("/")) {
            return base + "/" + path;
        }
        return base + path;
    }

    private ServiceEndpoint requireEndpoint(String serviceName) {
        ServiceEndpoint endpoint = services.get(serviceName);
        if (endpoint == null) {
            throw new IllegalStateException(
                    "No ftgo.services." + serviceName + " entry configured");
        }
        return endpoint;
    }
}
