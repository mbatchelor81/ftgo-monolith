package com.ftgo.resilience.discovery;

import com.ftgo.resilience.config.ServiceDiscoveryProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KubernetesServiceRegistryTest {

    private KubernetesServiceRegistry registry;

    @BeforeEach
    void setUp() {
        ServiceDiscoveryProperties properties = new ServiceDiscoveryProperties();
        properties.setNamespace("ftgo");
        properties.setClusterDomain("cluster.local");
        properties.setDefaultPort(8080);
        properties.setDefaultScheme("http");
        registry = new KubernetesServiceRegistry(properties);
    }

    @Test
    void getServiceUrl_returnsFullyQualifiedUrl() {
        String url = registry.getServiceUrl("ftgo-order-service");

        assertThat(url).isEqualTo("http://ftgo-order-service.ftgo.svc.cluster.local:8080");
    }

    @Test
    void getServiceUrl_withCustomPort_usesProvidedPort() {
        String url = registry.getServiceUrl("ftgo-order-service", 9090);

        assertThat(url).isEqualTo("http://ftgo-order-service.ftgo.svc.cluster.local:9090");
    }

    @Test
    void getServiceHost_returnsDnsHostname() {
        String host = registry.getServiceHost("ftgo-order-service");

        assertThat(host).isEqualTo("ftgo-order-service.ftgo.svc.cluster.local");
    }

    @Test
    void getServiceUrl_withCustomNamespace_usesConfiguredNamespace() {
        ServiceDiscoveryProperties customProps = new ServiceDiscoveryProperties();
        customProps.setNamespace("production");
        customProps.setClusterDomain("cluster.local");
        customProps.setDefaultPort(8080);
        customProps.setDefaultScheme("https");
        KubernetesServiceRegistry customRegistry = new KubernetesServiceRegistry(customProps);

        String url = customRegistry.getServiceUrl("ftgo-order-service");

        assertThat(url).isEqualTo("https://ftgo-order-service.production.svc.cluster.local:8080");
    }
}
