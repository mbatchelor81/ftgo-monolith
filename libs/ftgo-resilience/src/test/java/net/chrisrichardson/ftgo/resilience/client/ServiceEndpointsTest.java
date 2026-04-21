package net.chrisrichardson.ftgo.resilience.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import net.chrisrichardson.ftgo.resilience.config.FtgoResilienceProperties;
import net.chrisrichardson.ftgo.resilience.config.FtgoResilienceProperties.ServiceEndpoint;

class ServiceEndpointsTest {

    @Test
    void baseUrl_returnsConfiguredDnsName() {
        ServiceEndpoints endpoints = endpointsWith(
                "consumer-service", "http://consumer-service.ftgo.svc.cluster.local:8080", null);

        assertThat(endpoints.baseUrl("consumer-service"))
                .isEqualTo("http://consumer-service.ftgo.svc.cluster.local:8080");
    }

    @Test
    void healthUrl_concatenatesBaseUrlAndHealthPath() {
        ServiceEndpoints endpoints = endpointsWith(
                "order-service", "http://order-service:8080", "/actuator/health");

        assertThat(endpoints.healthUrl("order-service"))
                .isEqualTo("http://order-service:8080/actuator/health");
    }

    @Test
    void healthUrl_handlesTrailingSlashInBaseUrl() {
        ServiceEndpoints endpoints = endpointsWith(
                "courier-service", "http://courier-service:8080/", "/actuator/health");

        assertThat(endpoints.healthUrl("courier-service"))
                .isEqualTo("http://courier-service:8080/actuator/health");
    }

    @Test
    void baseUrl_missingService_throwsIllegalState() {
        ServiceEndpoints endpoints = new ServiceEndpoints(new FtgoResilienceProperties());

        assertThatThrownBy(() -> endpoints.baseUrl("unknown"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("unknown");
    }

    @Test
    void baseUrl_blankBaseUrl_throwsIllegalState() {
        ServiceEndpoints endpoints = endpointsWith("restaurant-service", "   ", null);

        assertThatThrownBy(() -> endpoints.baseUrl("restaurant-service"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("restaurant-service");
    }

    private static ServiceEndpoints endpointsWith(String serviceName, String baseUrl, String healthPath) {
        FtgoResilienceProperties properties = new FtgoResilienceProperties();
        ServiceEndpoint endpoint = new ServiceEndpoint();
        endpoint.setBaseUrl(baseUrl);
        if (healthPath != null) {
            endpoint.setHealthPath(healthPath);
        }
        properties.getServices().put(serviceName, endpoint);
        return new ServiceEndpoints(properties);
    }
}
