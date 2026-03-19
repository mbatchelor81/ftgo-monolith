package com.ftgo.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Defines routes from the API Gateway to downstream microservices.
 *
 * <p>Each route includes circuit breaker protection and path rewriting.
 * Service URLs are resolved via Kubernetes DNS in production
 * (e.g., {@code consumer-service.ftgo.svc.cluster.local}).
 */
@Configuration
public class GatewayRouteConfiguration {

    @Bean
    public RouteLocator ftgoRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                // Consumer Service
                .route("consumer-service", r -> r
                        .path("/api/consumers/**")
                        .filters(f -> f
                                .circuitBreaker(cb -> cb
                                        .setName("consumerServiceCB")
                                        .setFallbackUri("forward:/fallback/service-unavailable"))
                                .rewritePath("/api/consumers/(?<segment>.*)", "/consumers/${segment}")
                                .rewritePath("/api/consumers$", "/consumers"))
                        .uri("${ftgo.gateway.routes.consumer-service:http://consumer-service:8080}"))

                // Order Service
                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .filters(f -> f
                                .circuitBreaker(cb -> cb
                                        .setName("orderServiceCB")
                                        .setFallbackUri("forward:/fallback/service-unavailable"))
                                .rewritePath("/api/orders/(?<segment>.*)", "/orders/${segment}")
                                .rewritePath("/api/orders$", "/orders"))
                        .uri("${ftgo.gateway.routes.order-service:http://order-service:8080}"))

                // Restaurant Service
                .route("restaurant-service", r -> r
                        .path("/api/restaurants/**")
                        .filters(f -> f
                                .circuitBreaker(cb -> cb
                                        .setName("restaurantServiceCB")
                                        .setFallbackUri("forward:/fallback/service-unavailable"))
                                .rewritePath("/api/restaurants/(?<segment>.*)", "/restaurants/${segment}")
                                .rewritePath("/api/restaurants$", "/restaurants"))
                        .uri("${ftgo.gateway.routes.restaurant-service:http://restaurant-service:8080}"))

                // Courier Service
                .route("courier-service", r -> r
                        .path("/api/couriers/**")
                        .filters(f -> f
                                .circuitBreaker(cb -> cb
                                        .setName("courierServiceCB")
                                        .setFallbackUri("forward:/fallback/service-unavailable"))
                                .rewritePath("/api/couriers/(?<segment>.*)", "/couriers/${segment}")
                                .rewritePath("/api/couriers$", "/couriers"))
                        .uri("${ftgo.gateway.routes.courier-service:http://courier-service:8080}"))

                .build();
    }
}
