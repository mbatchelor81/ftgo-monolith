package net.chrisrichardson.ftgo.resilience;

import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import net.chrisrichardson.ftgo.resilience.health.ConsumerServiceHealthIndicator;
import net.chrisrichardson.ftgo.resilience.health.CourierServiceHealthIndicator;
import net.chrisrichardson.ftgo.resilience.health.OrderServiceHealthIndicator;
import net.chrisrichardson.ftgo.resilience.health.RestaurantServiceHealthIndicator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class FtgoResilienceAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(FtgoResilienceAutoConfiguration.class));

    @Test
    void registriesCreatedByDefault() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(CircuitBreakerRegistry.class);
            assertThat(context).hasSingleBean(RetryRegistry.class);
            assertThat(context).hasSingleBean(BulkheadRegistry.class);
        });
    }

    @Test
    void healthIndicatorsCreatedByDefault() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(OrderServiceHealthIndicator.class);
            assertThat(context).hasSingleBean(ConsumerServiceHealthIndicator.class);
            assertThat(context).hasSingleBean(RestaurantServiceHealthIndicator.class);
            assertThat(context).hasSingleBean(CourierServiceHealthIndicator.class);
        });
    }

    @Test
    void disabledWhenPropertySetToFalse() {
        contextRunner
                .withPropertyValues("ftgo.resilience.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(CircuitBreakerRegistry.class);
                    assertThat(context).doesNotHaveBean(RetryRegistry.class);
                    assertThat(context).doesNotHaveBean(BulkheadRegistry.class);
                });
    }

    @Test
    void circuitBreakerRegistryContainsServiceInstances() {
        contextRunner.run(context -> {
            CircuitBreakerRegistry registry = context.getBean(CircuitBreakerRegistry.class);
            assertThat(registry.circuitBreaker("orderService")).isNotNull();
            assertThat(registry.circuitBreaker("consumerService")).isNotNull();
            assertThat(registry.circuitBreaker("restaurantService")).isNotNull();
            assertThat(registry.circuitBreaker("courierService")).isNotNull();
        });
    }

    @Test
    void retryRegistryContainsServiceInstances() {
        contextRunner.run(context -> {
            RetryRegistry registry = context.getBean(RetryRegistry.class);
            assertThat(registry.retry("orderService")).isNotNull();
            assertThat(registry.retry("consumerService")).isNotNull();
            assertThat(registry.retry("restaurantService")).isNotNull();
            assertThat(registry.retry("courierService")).isNotNull();
        });
    }

    @Test
    void bulkheadRegistryContainsServiceInstances() {
        contextRunner.run(context -> {
            BulkheadRegistry registry = context.getBean(BulkheadRegistry.class);
            assertThat(registry.bulkhead("orderService")).isNotNull();
            assertThat(registry.bulkhead("consumerService")).isNotNull();
            assertThat(registry.bulkhead("restaurantService")).isNotNull();
            assertThat(registry.bulkhead("courierService")).isNotNull();
        });
    }

    @Test
    void healthIndicatorReportsUpWhenCircuitBreakerClosed() {
        contextRunner.run(context -> {
            OrderServiceHealthIndicator indicator = context.getBean(OrderServiceHealthIndicator.class);
            Health health = indicator.health();
            assertThat(health.getStatus()).isEqualTo(Status.UP);
            assertThat(health.getDetails()).containsEntry("service", "order-service");
            assertThat(health.getDetails()).containsEntry("circuitBreaker.state", "CLOSED");
        });
    }

    @Test
    void customCircuitBreakerProperties() {
        contextRunner
                .withPropertyValues(
                        "ftgo.resilience.circuit-breaker.failure-rate-threshold=75",
                        "ftgo.resilience.circuit-breaker.sliding-window-size=20")
                .run(context -> {
                    CircuitBreakerRegistry registry = context.getBean(CircuitBreakerRegistry.class);
                    var cb = registry.circuitBreaker("orderService");
                    assertThat(cb.getCircuitBreakerConfig().getFailureRateThreshold()).isEqualTo(75.0f);
                    assertThat(cb.getCircuitBreakerConfig().getSlidingWindowSize()).isEqualTo(20);
                });
    }
}
