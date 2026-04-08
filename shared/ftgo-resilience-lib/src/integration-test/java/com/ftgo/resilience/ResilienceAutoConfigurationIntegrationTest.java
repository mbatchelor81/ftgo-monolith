package com.ftgo.resilience;

import com.ftgo.resilience.config.FtgoResilienceAutoConfiguration;
import com.ftgo.resilience.config.ServiceDiscoveryProperties;
import com.ftgo.resilience.discovery.KubernetesServiceRegistry;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test verifying the full auto-configuration wires up correctly.
 */
@SpringBootTest(classes = ResilienceAutoConfigurationIntegrationTest.TestConfig.class)
class ResilienceAutoConfigurationIntegrationTest {

    @Configuration
    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class
    })
    @Import(FtgoResilienceAutoConfiguration.class)
    static class TestConfig {
    }

    @Autowired
    private ApplicationContext context;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    private RetryRegistry retryRegistry;

    @Autowired
    private BulkheadRegistry bulkheadRegistry;

    @Autowired
    private RateLimiterRegistry rateLimiterRegistry;

    @Autowired
    private KubernetesServiceRegistry serviceRegistry;

    @Autowired
    private ServiceDiscoveryProperties discoveryProperties;

    @Test
    void contextLoads() {
        assertThat(context).isNotNull();
    }

    @Test
    void circuitBreakerRegistry_isAvailable() {
        assertThat(circuitBreakerRegistry).isNotNull();
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("integrationTest");
        assertThat(cb).isNotNull();
        assertThat(cb.getCircuitBreakerConfig().getFailureRateThreshold()).isEqualTo(50f);
    }

    @Test
    void retryRegistry_isAvailable() {
        assertThat(retryRegistry).isNotNull();
        var retry = retryRegistry.retry("integrationTest");
        assertThat(retry).isNotNull();
        assertThat(retry.getRetryConfig().getMaxAttempts()).isEqualTo(3);
    }

    @Test
    void bulkheadRegistry_isAvailable() {
        assertThat(bulkheadRegistry).isNotNull();
        var bulkhead = bulkheadRegistry.bulkhead("integrationTest");
        assertThat(bulkhead).isNotNull();
        assertThat(bulkhead.getBulkheadConfig().getMaxConcurrentCalls()).isEqualTo(25);
    }

    @Test
    void rateLimiterRegistry_isAvailable() {
        assertThat(rateLimiterRegistry).isNotNull();
        var rl = rateLimiterRegistry.rateLimiter("integrationTest");
        assertThat(rl).isNotNull();
        assertThat(rl.getRateLimiterConfig().getLimitForPeriod()).isEqualTo(50);
    }

    @Test
    void kubernetesServiceRegistry_resolvesUrls() {
        assertThat(serviceRegistry).isNotNull();
        String url = serviceRegistry.getServiceUrl("ftgo-order-service");
        assertThat(url).contains("ftgo-order-service");
        assertThat(url).contains("svc.cluster.local");
    }

    @Test
    void serviceDiscoveryProperties_hasDefaults() {
        assertThat(discoveryProperties.getNamespace()).isEqualTo("ftgo");
        assertThat(discoveryProperties.getClusterDomain()).isEqualTo("cluster.local");
        assertThat(discoveryProperties.getDefaultPort()).isEqualTo(8080);
        assertThat(discoveryProperties.getDefaultScheme()).isEqualTo("http");
    }

    @Test
    void circuitBreaker_transitionsToOpenOnFailures() {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("failureTest");

        // Record failures to trigger circuit breaker
        Supplier<String> decoratedSupplier = CircuitBreaker.decorateSupplier(cb, () -> {
            throw new RuntimeException("simulated failure");
        });

        // Execute enough calls to trigger the circuit breaker (minimum 5 calls)
        for (int i = 0; i < 10; i++) {
            try {
                decoratedSupplier.get();
            } catch (Exception ignored) {
                // Expected failures
            }
        }

        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }
}
