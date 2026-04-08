package com.ftgo.resilience.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that the {@link FtgoResilienceEnvironmentPostProcessor} loads
 * {@code ftgo-resilience-defaults.yml} as a low-priority property source.
 */
class ResilienceConfigurationTest {

    @Test
    void environmentPostProcessor_loadsResilienceDefaults() {
        var environment = new MockEnvironment();
        var postProcessor = new FtgoResilienceEnvironmentPostProcessor();

        postProcessor.postProcessEnvironment(environment, new SpringApplication());

        // Verify properties from ftgo-resilience-defaults.yml are loaded
        assertThat(environment.getProperty("server.shutdown")).isEqualTo("graceful");
        assertThat(environment.getProperty("spring.lifecycle.timeout-per-shutdown-phase")).isEqualTo("30s");
    }

    @Test
    void environmentPostProcessor_loadsResilienceDefaults_circuitBreaker() {
        var environment = new MockEnvironment();
        var postProcessor = new FtgoResilienceEnvironmentPostProcessor();

        postProcessor.postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty("resilience4j.circuitbreaker.configs.default.failureRateThreshold"))
                .isEqualTo("50");
        assertThat(environment.getProperty("resilience4j.circuitbreaker.configs.default.slidingWindowSize"))
                .isEqualTo("10");
        assertThat(environment.getProperty("resilience4j.circuitbreaker.configs.default.minimumNumberOfCalls"))
                .isEqualTo("5");
    }

    @Test
    void environmentPostProcessor_loadsResilienceDefaults_retry() {
        var environment = new MockEnvironment();
        var postProcessor = new FtgoResilienceEnvironmentPostProcessor();

        postProcessor.postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty("resilience4j.retry.configs.default.maxAttempts"))
                .isEqualTo("3");
        assertThat(environment.getProperty("resilience4j.retry.configs.default.waitDuration"))
                .isEqualTo("500ms");
    }

    @Test
    void environmentPostProcessor_loadsResilienceDefaults_bulkhead() {
        var environment = new MockEnvironment();
        var postProcessor = new FtgoResilienceEnvironmentPostProcessor();

        postProcessor.postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty("resilience4j.bulkhead.configs.default.maxConcurrentCalls"))
                .isEqualTo("25");
    }

    @Test
    void environmentPostProcessor_loadsResilienceDefaults_rateLimiter() {
        var environment = new MockEnvironment();
        var postProcessor = new FtgoResilienceEnvironmentPostProcessor();

        postProcessor.postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty("resilience4j.ratelimiter.configs.default.limitForPeriod"))
                .isEqualTo("50");
    }

    @Test
    void environmentPostProcessor_loadsServiceDiscoveryDefaults() {
        var environment = new MockEnvironment();
        var postProcessor = new FtgoResilienceEnvironmentPostProcessor();

        postProcessor.postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty("ftgo.service-discovery.namespace")).isEqualTo("ftgo");
        assertThat(environment.getProperty("ftgo.service-discovery.cluster-domain")).isEqualTo("cluster.local");
        assertThat(environment.getProperty("ftgo.service-discovery.default-port")).isEqualTo("8080");
        assertThat(environment.getProperty("ftgo.service-discovery.default-scheme")).isEqualTo("http");
    }

    @Test
    void environmentPostProcessor_applicationPropertiesTakePrecedence() {
        var environment = new MockEnvironment();
        // Simulate a service overriding a default
        environment.setProperty("server.shutdown", "immediate");

        var postProcessor = new FtgoResilienceEnvironmentPostProcessor();
        postProcessor.postProcessEnvironment(environment, new SpringApplication());

        // Application-level property should win (added first = higher priority)
        assertThat(environment.getProperty("server.shutdown")).isEqualTo("immediate");
    }
}
