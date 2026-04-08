package com.ftgo.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * Redis-backed rate limiting configuration.
 *
 * <p>Uses the client IP address as the rate-limiting key. When a Redis
 * instance is available the built-in {@code RedisRateLimiter} will
 * enforce the limits configured per-route in {@code application.yml}.
 */
@Configuration
public class RateLimiterConfiguration {

    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(
                exchange.getRequest().getRemoteAddress() != null
                        ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                        : "unknown");
    }
}
