package com.ftgo.apigateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

@Configuration
public class GatewayRoutingConfig {

    private final RedisRateLimiter redisRateLimiter;
    private final KeyResolver keyResolver;

    public GatewayRoutingConfig(@Nullable RedisRateLimiter redisRateLimiter,
                                 @Nullable KeyResolver keyResolver) {
        this.redisRateLimiter = redisRateLimiter;
        this.keyResolver = keyResolver;
    }

    @Bean
    public RouteLocator ftgoRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .filters(f -> applyFilters(f, "orderServiceCB"))
                        .uri("lb://order-service"))

                .route("consumer-service", r -> r
                        .path("/api/consumers/**")
                        .filters(f -> applyFilters(f, "consumerServiceCB"))
                        .uri("lb://consumer-service"))

                .route("restaurant-service", r -> r
                        .path("/api/restaurants/**")
                        .filters(f -> applyFilters(f, "restaurantServiceCB"))
                        .uri("lb://restaurant-service"))

                .route("courier-service", r -> r
                        .path("/api/couriers/**")
                        .filters(f -> applyFilters(f, "courierServiceCB"))
                        .uri("lb://courier-service"))

                .build();
    }

    private GatewayFilterSpec applyFilters(GatewayFilterSpec filterSpec, String circuitBreakerName) {
        filterSpec
                .stripPrefix(1)
                .circuitBreaker(cb -> cb
                        .setName(circuitBreakerName)
                        .setFallbackUri("forward:/fallback"));

        if (redisRateLimiter != null && keyResolver != null) {
            filterSpec.requestRateLimiter(rl -> rl
                    .setRateLimiter(redisRateLimiter)
                    .setKeyResolver(keyResolver)
                    .setDenyEmptyKey(false));
        }

        return filterSpec;
    }
}
