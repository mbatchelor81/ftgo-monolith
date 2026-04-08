package com.ftgo.order.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Configures custom business metrics for the Order Service.
 *
 * <p>Metrics exposed:
 * <ul>
 *   <li>{@code ftgo.orders.created} — counter of orders created</li>
 *   <li>{@code ftgo.orders.cancelled} — counter of orders cancelled</li>
 *   <li>{@code ftgo.orders.revised} — counter of orders revised</li>
 *   <li>{@code ftgo.orders.accepted} — counter of orders accepted by restaurants</li>
 *   <li>{@code ftgo.orders.delivered} — counter of orders delivered</li>
 *   <li>{@code ftgo.orders.active} — gauge of currently active (non-terminal) orders</li>
 *   <li>{@code ftgo.orders.processing.duration} — timer for order processing latency</li>
 * </ul>
 */
@Configuration
public class OrderMetricsConfiguration {

    private final AtomicLong activeOrders = new AtomicLong(0);

    @Bean
    public Counter ordersCreatedCounter(MeterRegistry registry) {
        return Counter.builder("ftgo.orders.created")
                .description("Total number of orders created")
                .tag("service", "ftgo-order-service")
                .register(registry);
    }

    @Bean
    public Counter ordersCancelledCounter(MeterRegistry registry) {
        return Counter.builder("ftgo.orders.cancelled")
                .description("Total number of orders cancelled")
                .tag("service", "ftgo-order-service")
                .register(registry);
    }

    @Bean
    public Counter ordersRevisedCounter(MeterRegistry registry) {
        return Counter.builder("ftgo.orders.revised")
                .description("Total number of orders revised")
                .tag("service", "ftgo-order-service")
                .register(registry);
    }

    @Bean
    public Counter ordersAcceptedCounter(MeterRegistry registry) {
        return Counter.builder("ftgo.orders.accepted")
                .description("Total number of orders accepted by restaurants")
                .tag("service", "ftgo-order-service")
                .register(registry);
    }

    @Bean
    public Counter ordersDeliveredCounter(MeterRegistry registry) {
        return Counter.builder("ftgo.orders.delivered")
                .description("Total number of orders delivered")
                .tag("service", "ftgo-order-service")
                .register(registry);
    }

    @Bean
    public AtomicLong activeOrdersGauge(MeterRegistry registry) {
        Gauge.builder("ftgo.orders.active", activeOrders, AtomicLong::doubleValue)
                .description("Number of currently active (non-terminal) orders")
                .tag("service", "ftgo-order-service")
                .register(registry);
        return activeOrders;
    }

    @Bean
    public Timer orderProcessingTimer(MeterRegistry registry) {
        return Timer.builder("ftgo.orders.processing.duration")
                .description("Time taken to process an order from creation to settlement")
                .tag("service", "ftgo-order-service")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
    }
}
