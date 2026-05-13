package net.chrisrichardson.ftgo.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(MeterRegistry.class)
public class FtgoMetricsAutoConfiguration {

    @Bean
    public FtgoCommonMetrics ftgoCommonMetrics(MeterRegistry registry) {
        return new FtgoCommonMetrics(registry);
    }

    @Bean
    @ConditionalOnProperty(name = "ftgo.metrics.order.enabled")
    public OrderMetrics orderMetrics(MeterRegistry registry) {
        return new OrderMetrics(registry);
    }

    @Bean
    @ConditionalOnProperty(name = "ftgo.metrics.consumer.enabled")
    public ConsumerMetrics consumerMetrics(MeterRegistry registry) {
        return new ConsumerMetrics(registry);
    }

    @Bean
    @ConditionalOnProperty(name = "ftgo.metrics.restaurant.enabled")
    public RestaurantMetrics restaurantMetrics(MeterRegistry registry) {
        return new RestaurantMetrics(registry);
    }

    @Bean
    @ConditionalOnProperty(name = "ftgo.metrics.courier.enabled")
    public CourierMetrics courierMetrics(MeterRegistry registry) {
        return new CourierMetrics(registry);
    }
}
