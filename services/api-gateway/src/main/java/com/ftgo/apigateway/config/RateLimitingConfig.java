package com.ftgo.apigateway.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import reactor.core.publisher.Mono;

import java.security.Principal;

@Configuration
@ConditionalOnProperty(name = "ftgo.gateway.rate-limiting.enabled", havingValue = "true", matchIfMissing = true)
public class RateLimitingConfig {

  @Bean
  public RedisRateLimiter redisRateLimiter() {
    return new RedisRateLimiter(10, 20, 1);
  }

  @Bean
  public KeyResolver userKeyResolver() {
    return exchange -> exchange.getPrincipal()
      .map(Principal::getName)
      .switchIfEmpty(Mono.fromSupplier(() -> {
        var remoteAddress = exchange.getRequest().getRemoteAddress();
        if (remoteAddress != null && remoteAddress.getAddress() != null) {
          return remoteAddress.getAddress().getHostAddress();
        }
        return "anonymous";
      }));
  }
}
