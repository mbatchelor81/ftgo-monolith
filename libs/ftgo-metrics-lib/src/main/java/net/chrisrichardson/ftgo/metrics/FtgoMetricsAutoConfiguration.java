package net.chrisrichardson.ftgo.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(MeterRegistry.class)
public class FtgoMetricsAutoConfiguration {

    @Bean
    public FtgoCommonMetrics ftgoCommonMetrics(MeterRegistry registry) {
        return new FtgoCommonMetrics(registry);
    }

    @Bean
    public OrderMetrics orderMetrics(MeterRegistry registry) {
        return new OrderMetrics(registry);
    }

    @Bean
    public ConsumerMetrics consumerMetrics(MeterRegistry registry) {
        return new ConsumerMetrics(registry);
    }

    @Bean
    public RestaurantMetrics restaurantMetrics(MeterRegistry registry) {
        return new RestaurantMetrics(registry);
    }

    @Bean
    public CourierMetrics courierMetrics(MeterRegistry registry) {
        return new CourierMetrics(registry);
    }
}
