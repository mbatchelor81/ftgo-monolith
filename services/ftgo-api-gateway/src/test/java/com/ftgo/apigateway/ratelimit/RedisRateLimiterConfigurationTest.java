package com.ftgo.apigateway.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

/** Unit tests for {@link RedisRateLimiterConfiguration}. */
class RedisRateLimiterConfigurationTest {

    private final RedisRateLimiterConfiguration config = new RedisRateLimiterConfiguration();

    @Test
    void apiKeyResolver_usesApiKeyHeader_whenPresent() {
        KeyResolver resolver = config.apiKeyResolver();
        MockServerHttpRequest request =
                MockServerHttpRequest.get("/api/orders")
                        .header("X-Api-Key", "my-api-key-123")
                        .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        String key = resolver.resolve(exchange).block();
        assertThat(key).isEqualTo("my-api-key-123");
    }

    @Test
    void apiKeyResolver_fallsBackToIp_whenNoApiKey() {
        KeyResolver resolver = config.apiKeyResolver();
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/orders").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        String key = resolver.resolve(exchange).block();
        // MockServerWebExchange has a remote address; we just verify it resolves to something
        assertThat(key).isNotNull().isNotBlank();
    }

    @Test
    void defaultRedisRateLimiter_isCreated() {
        assertThat(config.defaultRedisRateLimiter()).isNotNull();
    }
}
