package com.ftgo.courier.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Configures custom business metrics for the Courier Service.
 *
 * <p>Metrics exposed:
 * <ul>
 *   <li>{@code ftgo.couriers.created} — counter of couriers created</li>
 *   <li>{@code ftgo.couriers.availability.changed} — counter of courier availability changes</li>
 *   <li>{@code ftgo.couriers.deliveries.assigned} — counter of deliveries assigned</li>
 *   <li>{@code ftgo.couriers.deliveries.completed} — counter of deliveries completed</li>
 *   <li>{@code ftgo.couriers.available} — gauge of currently available couriers</li>
 *   <li>{@code ftgo.couriers.delivery.duration} — timer for delivery duration</li>
 * </ul>
 */
@Configuration
public class CourierMetricsConfiguration {

    private final AtomicLong availableCouriers = new AtomicLong(0);

    @Bean
    public Counter couriersCreatedCounter(MeterRegistry registry) {
        return Counter.builder("ftgo.couriers.created")
                .description("Total number of couriers created")
                .tag("service", "ftgo-courier-service")
                .register(registry);
    }

    @Bean
    public Counter couriersAvailabilityChangedCounter(MeterRegistry registry) {
        return Counter.builder("ftgo.couriers.availability.changed")
                .description("Total number of courier availability state changes")
                .tag("service", "ftgo-courier-service")
                .register(registry);
    }

    @Bean
    public Counter couriersDeliveriesAssignedCounter(MeterRegistry registry) {
        return Counter.builder("ftgo.couriers.deliveries.assigned")
                .description("Total number of deliveries assigned to couriers")
                .tag("service", "ftgo-courier-service")
                .register(registry);
    }

    @Bean
    public Counter couriersDeliveriesCompletedCounter(MeterRegistry registry) {
        return Counter.builder("ftgo.couriers.deliveries.completed")
                .description("Total number of deliveries completed by couriers")
                .tag("service", "ftgo-courier-service")
                .register(registry);
    }

    @Bean
    public AtomicLong availableCouriersGauge(MeterRegistry registry) {
        Gauge.builder("ftgo.couriers.available", availableCouriers, AtomicLong::doubleValue)
                .description("Number of currently available couriers")
                .tag("service", "ftgo-courier-service")
                .register(registry);
        return availableCouriers;
    }

    @Bean
    public Timer courierDeliveryTimer(MeterRegistry registry) {
        return Timer.builder("ftgo.couriers.delivery.duration")
                .description("Time taken for a courier to complete a delivery")
                .tag("service", "ftgo-courier-service")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
    }
}
