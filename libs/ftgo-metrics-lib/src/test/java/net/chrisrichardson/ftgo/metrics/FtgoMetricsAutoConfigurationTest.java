package net.chrisrichardson.ftgo.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class FtgoMetricsAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withBean(MeterRegistry.class, SimpleMeterRegistry::new)
            .withConfiguration(AutoConfigurations.of(FtgoMetricsAutoConfiguration.class));

    @Test
    void autoConfigurationRegistersAllMetricsBeans() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(FtgoCommonMetrics.class);
            assertThat(context).hasSingleBean(OrderMetrics.class);
            assertThat(context).hasSingleBean(ConsumerMetrics.class);
            assertThat(context).hasSingleBean(RestaurantMetrics.class);
            assertThat(context).hasSingleBean(CourierMetrics.class);
        });
    }

    @Test
    void orderMetricsRegistersExpectedCounters() {
        contextRunner.run(context -> {
            MeterRegistry registry = context.getBean(MeterRegistry.class);
            assertThat(registry.find("ftgo.orders.created").counter()).isNotNull();
            assertThat(registry.find("ftgo.orders.approved").counter()).isNotNull();
            assertThat(registry.find("ftgo.orders.rejected").counter()).isNotNull();
            assertThat(registry.find("ftgo.orders.cancelled").counter()).isNotNull();
            assertThat(registry.find("ftgo.orders.delivered").counter()).isNotNull();
        });
    }

    @Test
    void consumerMetricsRegistersExpectedCounters() {
        contextRunner.run(context -> {
            MeterRegistry registry = context.getBean(MeterRegistry.class);
            assertThat(registry.find("ftgo.consumers.registered").counter()).isNotNull();
            assertThat(registry.find("ftgo.consumers.validations.succeeded").counter()).isNotNull();
        });
    }

    @Test
    void restaurantMetricsRegistersExpectedCounters() {
        contextRunner.run(context -> {
            MeterRegistry registry = context.getBean(MeterRegistry.class);
            assertThat(registry.find("ftgo.restaurants.created").counter()).isNotNull();
            assertThat(registry.find("ftgo.restaurants.tickets.created").counter()).isNotNull();
            assertThat(registry.find("ftgo.restaurants.tickets.accepted").counter()).isNotNull();
        });
    }

    @Test
    void courierMetricsRegistersExpectedCounters() {
        contextRunner.run(context -> {
            MeterRegistry registry = context.getBean(MeterRegistry.class);
            assertThat(registry.find("ftgo.couriers.created").counter()).isNotNull();
            assertThat(registry.find("ftgo.couriers.deliveries.completed").counter()).isNotNull();
            assertThat(registry.find("ftgo.couriers.currently.available").gauge()).isNotNull();
        });
    }

    @Test
    void domainMetricsCanBeDisabledViaProperty() {
        new ApplicationContextRunner()
                .withBean(MeterRegistry.class, SimpleMeterRegistry::new)
                .withConfiguration(AutoConfigurations.of(FtgoMetricsAutoConfiguration.class))
                .withPropertyValues("ftgo.metrics.order.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(OrderMetrics.class);
                    assertThat(context).hasSingleBean(ConsumerMetrics.class);
                });
    }
}
