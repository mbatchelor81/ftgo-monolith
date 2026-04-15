package com.ftgo.apigateway.ratelimit;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * Configures Redis-backed rate limiting for the API Gateway.
 *
 * <p>Rate limiting is applied per client, resolved by the {@code X-Api-Key} header when present, or
 * falling back to the remote IP address. This protects downstream services from excessive traffic.
 *
 * <p>Default limits:
 *
 * <ul>
 *   <li>{@code replenishRate} — 20 requests per second
 *   <li>{@code burstCapacity} — 40 requests (allows short bursts)
 *   <li>{@code requestedTokens} — 1 token per request
 * </ul>
 *
 * <p>These defaults can be overridden per-route in {@code application.yml} via the {@code
 * RequestRateLimiter} filter configuration.
 */
@Configuration
public class RedisRateLimiterConfiguration {

    /**
     * Resolves the rate-limit key from the {@code X-Api-Key} header, falling back to the client IP
     * address.
     */
    @Bean
    public KeyResolver apiKeyResolver() {
        return exchange -> {
            String apiKey = exchange.getRequest().getHeaders().getFirst("X-Api-Key");
            if (apiKey != null && !apiKey.isBlank()) {
                return Mono.just(apiKey);
            }
            String remoteAddress;
            var socketAddr = exchange.getRequest().getRemoteAddress();
            if (socketAddr != null && socketAddr.getAddress() != null) {
                remoteAddress = socketAddr.getAddress().getHostAddress();
            } else if (socketAddr != null) {
                remoteAddress = socketAddr.getHostString();
            } else {
                remoteAddress = "unknown";
            }
            return Mono.just(remoteAddress);
        };
    }

    /** Default rate limiter bean with sensible production defaults. */
    @Bean
    public RedisRateLimiter defaultRedisRateLimiter() {
        return new RedisRateLimiter(20, 40, 1);
    }
}
