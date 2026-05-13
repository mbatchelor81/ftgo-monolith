package net.chrisrichardson.ftgo.resilience.circuitbreaker;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import net.chrisrichardson.ftgo.resilience.FtgoResilienceProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConditionalOnClass(CircuitBreakerRegistry.class)
public class CircuitBreakerConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CircuitBreakerConfig defaultCircuitBreakerConfig(FtgoResilienceProperties properties) {
        FtgoResilienceProperties.CircuitBreaker cb = properties.getCircuitBreaker();
        return CircuitBreakerConfig.custom()
                .failureRateThreshold(cb.getFailureRateThreshold())
                .slidingWindowSize(cb.getSlidingWindowSize())
                .minimumNumberOfCalls(cb.getMinimumNumberOfCalls())
                .waitDurationInOpenState(Duration.ofMillis(cb.getWaitDurationInOpenStateMillis()))
                .permittedNumberOfCallsInHalfOpenState(cb.getPermittedNumberOfCallsInHalfOpenState())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public CircuitBreakerRegistry circuitBreakerRegistry(CircuitBreakerConfig defaultConfig) {
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(defaultConfig);
        registry.circuitBreaker("orderService");
        registry.circuitBreaker("consumerService");
        registry.circuitBreaker("restaurantService");
        registry.circuitBreaker("courierService");
        return registry;
    }
}
