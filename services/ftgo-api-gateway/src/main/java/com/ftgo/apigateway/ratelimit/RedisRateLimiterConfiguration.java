package com.ftgo.apigateway.ratelimit;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * Configures Redis-backed rate limiting for the API Gateway.
 *
 * <p>Rate limiting is applied per client, resolved by the client's remote IP address. This protects
 * downstream services from excessive traffic.
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
     * Resolves the rate-limit key from the client's remote IP address.
     *
     * <p>Handles unresolved {@link java.net.InetSocketAddress} instances (common behind load
     * balancers with {@code server.forward-headers-strategy: framework}) by falling back to {@link
     * java.net.InetSocketAddress#getHostString()}.
     */
    @Bean
    public KeyResolver apiKeyResolver() {
        return exchange -> {
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
