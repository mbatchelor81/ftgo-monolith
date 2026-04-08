package com.ftgo.resilience.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Kubernetes DNS-based service discovery.
 *
 * <p>Example configuration in {@code application.yml}:
 * <pre>
 * ftgo:
 *   service-discovery:
 *     namespace: ftgo
 *     cluster-domain: cluster.local
 *     default-port: 8080
 *     default-scheme: http
 * </pre>
 */
@ConfigurationProperties(prefix = "ftgo.service-discovery")
public class ServiceDiscoveryProperties {

    /** Kubernetes namespace where FTGO services are deployed. */
    private String namespace = "ftgo";

    /** Kubernetes cluster DNS domain. */
    private String clusterDomain = "cluster.local";

    /** Default port for inter-service communication. */
    private int defaultPort = 8080;

    /** Default scheme (http/https) for service URLs. */
    private String defaultScheme = "http";

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

    public String getDefaultScheme() {
        return defaultScheme;
    }

    public void setDefaultScheme(String defaultScheme) {
        this.defaultScheme = defaultScheme;
    }
}
