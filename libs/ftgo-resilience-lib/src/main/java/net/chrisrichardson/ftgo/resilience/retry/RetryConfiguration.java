package net.chrisrichardson.ftgo.resilience.retry;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import net.chrisrichardson.ftgo.resilience.FtgoResilienceProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(RetryRegistry.class)
public class RetryConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RetryConfig defaultRetryConfig(FtgoResilienceProperties properties) {
        FtgoResilienceProperties.Retry retry = properties.getRetry();
        return RetryConfig.custom()
                .maxAttempts(retry.getMaxAttempts())
                .intervalFunction(IntervalFunction.ofExponentialBackoff(
                        retry.getWaitDurationMillis(), retry.getMultiplier()))
                .retryExceptions(Exception.class)
                .ignoreExceptions(IllegalArgumentException.class)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public RetryRegistry retryRegistry(RetryConfig defaultConfig) {
        RetryRegistry registry = RetryRegistry.of(defaultConfig);
        registry.retry("orderService");
        registry.retry("consumerService");
        registry.retry("restaurantService");
        registry.retry("courierService");
        return registry;
    }
}
