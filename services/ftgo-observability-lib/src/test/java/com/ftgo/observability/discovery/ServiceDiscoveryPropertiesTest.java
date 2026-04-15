package com.ftgo.observability.discovery;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tests for Kubernetes DNS-based service discovery resolution. */
@DisplayName("ServiceDiscoveryProperties")
class ServiceDiscoveryPropertiesTest {

    private ServiceDiscoveryProperties properties;

    @BeforeEach
    void setUp() {
        properties = new ServiceDiscoveryProperties();
        properties.setNamespace("ftgo");
        properties.setClusterDomain("svc.cluster.local");
        properties.setDefaultPort(8080);
    }

    @Test
    @DisplayName("should resolve URL using K8s DNS convention when no explicit URL is configured")
    void resolveServiceUrl_noExplicitUrl_usesKubernetesDns() {
        String url = properties.resolveServiceUrl("ftgo-order-service");

        assertThat(url).isEqualTo("http://ftgo-order-service.ftgo.svc.cluster.local:8080");
    }

    @Test
    @DisplayName("should return explicit URL when configured")
    void resolveServiceUrl_explicitUrl_returnsConfiguredUrl() {
        properties.setServices(Map.of("ftgo-order-service", "http://localhost:8081"));

        String url = properties.resolveServiceUrl("ftgo-order-service");

        assertThat(url).isEqualTo("http://localhost:8081");
    }

    @Test
    @DisplayName("should use custom namespace in DNS resolution")
    void resolveServiceUrl_customNamespace_usesNamespace() {
        properties.setNamespace("production");

        String url = properties.resolveServiceUrl("ftgo-consumer-service");

        assertThat(url).isEqualTo("http://ftgo-consumer-service.production.svc.cluster.local:8080");
    }

    @Test
    @DisplayName("should use custom port in DNS resolution")
    void resolveServiceUrl_customPort_usesPort() {
        properties.setDefaultPort(9090);

        String url = properties.resolveServiceUrl("ftgo-restaurant-service");

        assertThat(url).isEqualTo("http://ftgo-restaurant-service.ftgo.svc.cluster.local:9090");
    }

    @Test
    @DisplayName("should fall back to DNS when explicit URL is blank")
    void resolveServiceUrl_blankExplicitUrl_fallsBackToDns() {
        properties.setServices(Map.of("ftgo-order-service", "  "));

        String url = properties.resolveServiceUrl("ftgo-order-service");

        assertThat(url).isEqualTo("http://ftgo-order-service.ftgo.svc.cluster.local:8080");
    }

    @Test
    @DisplayName("should have sensible defaults")
    void defaults_areConfigured() {
        ServiceDiscoveryProperties defaults = new ServiceDiscoveryProperties();

        assertThat(defaults.getNamespace()).isEqualTo("default");
        assertThat(defaults.getClusterDomain()).isEqualTo("svc.cluster.local");
        assertThat(defaults.getDefaultPort()).isEqualTo(8080);
        assertThat(defaults.getServices()).isEmpty();
    }
}
