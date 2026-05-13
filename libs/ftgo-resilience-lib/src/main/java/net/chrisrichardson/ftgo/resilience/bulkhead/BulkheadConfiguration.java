package net.chrisrichardson.ftgo.resilience.bulkhead;

import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import net.chrisrichardson.ftgo.resilience.FtgoResilienceProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConditionalOnClass(BulkheadRegistry.class)
public class BulkheadConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public BulkheadConfig defaultBulkheadConfig(FtgoResilienceProperties properties) {
        FtgoResilienceProperties.Bulkhead bh = properties.getBulkhead();
        return BulkheadConfig.custom()
                .maxConcurrentCalls(bh.getMaxConcurrentCalls())
                .maxWaitDuration(Duration.ofMillis(bh.getMaxWaitDurationMillis()))
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public BulkheadRegistry bulkheadRegistry(BulkheadConfig defaultConfig) {
        BulkheadRegistry registry = BulkheadRegistry.of(defaultConfig);
        registry.bulkhead("orderService");
        registry.bulkhead("consumerService");
        registry.bulkhead("restaurantService");
        registry.bulkhead("courierService");
        return registry;
    }
}
