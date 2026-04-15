package com.ftgo.apigateway.config;

import com.ftgo.apigateway.filter.CorrelationIdGatewayFilterFactory;
import com.ftgo.apigateway.filter.JwtClaimsPropagationGatewayFilterFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures gateway routes for all FTGO microservices.
 *
 * <p>Each route maps a public URL prefix to the corresponding internal service, applying JWT claims
 * propagation, correlation ID injection, and circuit-breaker protection.
 *
 * <p>API versioning is supported via URL path prefix ({@code /v1/}, {@code /v2/}) and the {@code
 * X-Api-Version} header. The URL path prefix is stripped before forwarding to downstream services.
 */
@Configuration
@EnableConfigurationProperties(GatewayProperties.class)
public class GatewayRouteConfiguration {

    private final GatewayProperties gatewayProperties;
    private final JwtClaimsPropagationGatewayFilterFactory jwtClaimsFilterFactory;
    private final CorrelationIdGatewayFilterFactory correlationIdFilterFactory;

    public GatewayRouteConfiguration(
            GatewayProperties gatewayProperties,
            JwtClaimsPropagationGatewayFilterFactory jwtClaimsFilterFactory,
            CorrelationIdGatewayFilterFactory correlationIdFilterFactory) {
        this.gatewayProperties = gatewayProperties;
        this.jwtClaimsFilterFactory = jwtClaimsFilterFactory;
        this.correlationIdFilterFactory = correlationIdFilterFactory;
    }

    @Bean
    public RouteLocator ftgoRouteLocator(RouteLocatorBuilder builder) {
        var jwtCfg = new JwtClaimsPropagationGatewayFilterFactory.Config();
        var corrCfg = new CorrelationIdGatewayFilterFactory.Config();
        String rewriteRegex = "/v\\d+/api/(?<remaining>.*)";
        String rewriteReplacement = "/api/${remaining}";

        return builder.routes()
                .route(
                        "order-service",
                        r ->
                                r.path("/api/orders/**", "/v{version}/api/orders/**")
                                        .filters(
                                                f ->
                                                        f.filter(
                                                                        jwtClaimsFilterFactory
                                                                                .apply(jwtCfg))
                                                                .filter(
                                                                        correlationIdFilterFactory
                                                                                .apply(corrCfg))
                                                                .circuitBreaker(
                                                                        cb ->
                                                                                cb.setName(
                                                                                                "orderServiceCB")
                                                                                        .setFallbackUri(
                                                                                                "forward:/fallback"))
                                                                .rewritePath(
                                                                        rewriteRegex,
                                                                        rewriteReplacement))
                                        .uri(gatewayProperties.getServices().getOrderServiceUrl()))
                .route(
                        "consumer-service",
                        r ->
                                r.path("/api/consumers/**", "/v{version}/api/consumers/**")
                                        .filters(
                                                f ->
                                                        f.filter(
                                                                        jwtClaimsFilterFactory
                                                                                .apply(jwtCfg))
                                                                .filter(
                                                                        correlationIdFilterFactory
                                                                                .apply(corrCfg))
                                                                .circuitBreaker(
                                                                        cb ->
                                                                                cb.setName(
                                                                                                "consumerServiceCB")
                                                                                        .setFallbackUri(
                                                                                                "forward:/fallback"))
                                                                .rewritePath(
                                                                        rewriteRegex,
                                                                        rewriteReplacement))
                                        .uri(
                                                gatewayProperties
                                                        .getServices()
                                                        .getConsumerServiceUrl()))
                .route(
                        "restaurant-service",
                        r ->
                                r.path("/api/restaurants/**", "/v{version}/api/restaurants/**")
                                        .filters(
                                                f ->
                                                        f.filter(
                                                                        jwtClaimsFilterFactory
                                                                                .apply(jwtCfg))
                                                                .filter(
                                                                        correlationIdFilterFactory
                                                                                .apply(corrCfg))
                                                                .circuitBreaker(
                                                                        cb ->
                                                                                cb.setName(
                                                                                                "restaurantServiceCB")
                                                                                        .setFallbackUri(
                                                                                                "forward:/fallback"))
                                                                .rewritePath(
                                                                        rewriteRegex,
                                                                        rewriteReplacement))
                                        .uri(
                                                gatewayProperties
                                                        .getServices()
                                                        .getRestaurantServiceUrl()))
                .route(
                        "courier-service",
                        r ->
                                r.path("/api/couriers/**", "/v{version}/api/couriers/**")
                                        .filters(
                                                f ->
                                                        f.filter(
                                                                        jwtClaimsFilterFactory
                                                                                .apply(jwtCfg))
                                                                .filter(
                                                                        correlationIdFilterFactory
                                                                                .apply(corrCfg))
                                                                .circuitBreaker(
                                                                        cb ->
                                                                                cb.setName(
                                                                                                "courierServiceCB")
                                                                                        .setFallbackUri(
                                                                                                "forward:/fallback"))
                                                                .rewritePath(
                                                                        rewriteRegex,
                                                                        rewriteReplacement))
                                        .uri(
                                                gatewayProperties
                                                        .getServices()
                                                        .getCourierServiceUrl()))
                .build();
    }
}
