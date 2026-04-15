package com.ftgo.apigateway.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link GatewayProperties}. */
class GatewayPropertiesTest {

    @Test
    void defaultServiceUrls_arePopulated() {
        GatewayProperties properties = new GatewayProperties();
        GatewayProperties.Services services = properties.getServices();

        assertThat(services.getOrderServiceUrl()).isEqualTo("http://ftgo-order-service");
        assertThat(services.getConsumerServiceUrl()).isEqualTo("http://ftgo-consumer-service");
        assertThat(services.getRestaurantServiceUrl()).isEqualTo("http://ftgo-restaurant-service");
        assertThat(services.getCourierServiceUrl()).isEqualTo("http://ftgo-courier-service");
    }

    @Test
    void serviceUrls_canBeOverridden() {
        GatewayProperties properties = new GatewayProperties();
        GatewayProperties.Services services = properties.getServices();

        services.setOrderServiceUrl("http://custom-order:8081");
        services.setConsumerServiceUrl("http://custom-consumer:8082");
        services.setRestaurantServiceUrl("http://custom-restaurant:8083");
        services.setCourierServiceUrl("http://custom-courier:8084");

        assertThat(services.getOrderServiceUrl()).isEqualTo("http://custom-order:8081");
        assertThat(services.getConsumerServiceUrl()).isEqualTo("http://custom-consumer:8082");
        assertThat(services.getRestaurantServiceUrl()).isEqualTo("http://custom-restaurant:8083");
        assertThat(services.getCourierServiceUrl()).isEqualTo("http://custom-courier:8084");
    }

    @Test
    void servicesObject_canBeReplaced() {
        GatewayProperties properties = new GatewayProperties();
        GatewayProperties.Services newServices = new GatewayProperties.Services();
        newServices.setOrderServiceUrl("http://new-order");

        properties.setServices(newServices);

        assertThat(properties.getServices().getOrderServiceUrl()).isEqualTo("http://new-order");
    }
}
