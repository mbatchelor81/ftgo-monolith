package net.chrisrichardson.ftgo.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.springboot3.circuitbreaker.autoconfigure.CircuitBreakerAutoConfiguration;
import io.github.resilience4j.springboot3.bulkhead.autoconfigure.BulkheadAutoConfiguration;
import io.github.resilience4j.springboot3.retry.autoconfigure.RetryAutoConfiguration;
import net.chrisrichardson.ftgo.resilience.bulkhead.BulkheadConfiguration;
import net.chrisrichardson.ftgo.resilience.circuitbreaker.CircuitBreakerConfiguration;
import net.chrisrichardson.ftgo.resilience.health.ConsumerServiceHealthIndicator;
import net.chrisrichardson.ftgo.resilience.health.CourierServiceHealthIndicator;
import net.chrisrichardson.ftgo.resilience.health.OrderServiceHealthIndicator;
import net.chrisrichardson.ftgo.resilience.health.RestaurantServiceHealthIndicator;
import net.chrisrichardson.ftgo.resilience.retry.RetryConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@AutoConfigureBefore({CircuitBreakerAutoConfiguration.class, BulkheadAutoConfiguration.class, RetryAutoConfiguration.class})
@ConditionalOnClass(CircuitBreakerRegistry.class)
@ConditionalOnProperty(name = "ftgo.resilience.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(FtgoResilienceProperties.class)
@Import({CircuitBreakerConfiguration.class, RetryConfiguration.class, BulkheadConfiguration.class})
public class FtgoResilienceAutoConfiguration {

    @Bean
    @ConditionalOnProperty(name = "ftgo.resilience.health.order.enabled", matchIfMissing = true)
    public OrderServiceHealthIndicator orderServiceHealthIndicator(CircuitBreakerRegistry registry) {
        return new OrderServiceHealthIndicator(registry);
    }

    @Bean
    @ConditionalOnProperty(name = "ftgo.resilience.health.consumer.enabled", matchIfMissing = true)
    public ConsumerServiceHealthIndicator consumerServiceHealthIndicator(CircuitBreakerRegistry registry) {
        return new ConsumerServiceHealthIndicator(registry);
    }

    @Bean
    @ConditionalOnProperty(name = "ftgo.resilience.health.restaurant.enabled", matchIfMissing = true)
    public RestaurantServiceHealthIndicator restaurantServiceHealthIndicator(CircuitBreakerRegistry registry) {
        return new RestaurantServiceHealthIndicator(registry);
    }

    @Bean
    @ConditionalOnProperty(name = "ftgo.resilience.health.courier.enabled", matchIfMissing = true)
    public CourierServiceHealthIndicator courierServiceHealthIndicator(CircuitBreakerRegistry registry) {
        return new CourierServiceHealthIndicator(registry);
    }
}
