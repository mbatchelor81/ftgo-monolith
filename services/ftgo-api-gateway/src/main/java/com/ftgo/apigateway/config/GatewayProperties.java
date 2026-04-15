package com.ftgo.apigateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the FTGO API Gateway.
 *
 * <p>Bind to {@code ftgo.gateway.*} in application YAML.
 *
 * <pre>
 * ftgo:
 *   gateway:
 *     services:
 *       order-service-url: http://ftgo-order-service
 *       consumer-service-url: http://ftgo-consumer-service
 *       restaurant-service-url: http://ftgo-restaurant-service
 *       courier-service-url: http://ftgo-courier-service
 * </pre>
 */
@ConfigurationProperties(prefix = "ftgo.gateway")
public class GatewayProperties {

    private Services services = new Services();

    public Services getServices() {
        return services;
    }

    public void setServices(Services services) {
        this.services = services;
    }

    /** Upstream service URLs. */
    public static class Services {

        private String orderServiceUrl = "http://ftgo-order-service";
        private String consumerServiceUrl = "http://ftgo-consumer-service";
        private String restaurantServiceUrl = "http://ftgo-restaurant-service";
        private String courierServiceUrl = "http://ftgo-courier-service";

        public String getOrderServiceUrl() {
            return orderServiceUrl;
        }

        public void setOrderServiceUrl(String orderServiceUrl) {
            this.orderServiceUrl = orderServiceUrl;
        }

        public String getConsumerServiceUrl() {
            return consumerServiceUrl;
        }

        public void setConsumerServiceUrl(String consumerServiceUrl) {
            this.consumerServiceUrl = consumerServiceUrl;
        }

        public String getRestaurantServiceUrl() {
            return restaurantServiceUrl;
        }

        public void setRestaurantServiceUrl(String restaurantServiceUrl) {
            this.restaurantServiceUrl = restaurantServiceUrl;
        }

        public String getCourierServiceUrl() {
            return courierServiceUrl;
        }

        public void setCourierServiceUrl(String courierServiceUrl) {
            this.courierServiceUrl = courierServiceUrl;
        }
    }
}
