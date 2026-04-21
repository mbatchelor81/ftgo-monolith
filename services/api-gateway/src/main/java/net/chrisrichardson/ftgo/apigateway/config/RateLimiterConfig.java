package net.chrisrichardson.ftgo.apigateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.core.publisher.Mono;

/**
 * Key resolvers used by the Redis-backed {@code RequestRateLimiter} filter.
 *
 * <p>{@link #userKeyResolver()} is the default: it rate-limits per authenticated
 * principal so a single user cannot exhaust capacity for everyone else on the
 * same IP. Unauthenticated traffic (the login/public allowlist) falls back to
 * the literal {@code anonymous} bucket.
 *
 * <p>{@link #ipKeyResolver()} is exposed for routes that stay anonymous but
 * still need abuse protection — reference it by bean name from a route's
 * {@code key-resolver} property.
 */
@Configuration
public class RateLimiterConfig {

    @Bean
    @Primary
    public KeyResolver userKeyResolver() {
        return exchange -> ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication().getName())
                .defaultIfEmpty("anonymous");
    }

    @Bean("ipKeyResolver")
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.justOrEmpty(exchange.getRequest().getRemoteAddress())
                .map(addr -> addr.getAddress().getHostAddress())
                .defaultIfEmpty("unknown");
    }
}
