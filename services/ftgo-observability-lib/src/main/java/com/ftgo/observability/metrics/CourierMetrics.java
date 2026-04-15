package com.ftgo.observability.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Custom business metrics for the Courier Service.
 *
 * <p>Tracks courier registration, availability changes, and delivery events.
 */
@Component
@ConditionalOnProperty(name = "spring.application.name", havingValue = "ftgo-courier-service")
public class CourierMetrics {

    private final Counter couriersCreated;
    private final Counter deliveriesAssigned;
    private final Counter deliveriesCompleted;
    private final AtomicInteger couriersAvailable = new AtomicInteger(0);

    public CourierMetrics(MeterRegistry registry) {
        this.couriersCreated = Counter.builder("ftgo.couriers.created")
                .description("Total number of couriers created")
                .tag("service", "courier-service")
                .register(registry);

        this.deliveriesAssigned = Counter.builder("ftgo.couriers.deliveries.assigned")
                .description("Total number of deliveries assigned to couriers")
                .tag("service", "courier-service")
                .register(registry);

        this.deliveriesCompleted = Counter.builder("ftgo.couriers.deliveries.completed")
                .description("Total number of deliveries completed")
                .tag("service", "courier-service")
                .register(registry);

        Gauge.builder("ftgo.couriers.available", couriersAvailable, AtomicInteger::get)
                .description("Current number of available couriers")
                .tag("service", "courier-service")
                .register(registry);
    }

    public void recordCourierCreated() {
        couriersCreated.increment();
    }

    public void recordDeliveryAssigned() {
        deliveriesAssigned.increment();
    }

    public void recordDeliveryCompleted() {
        deliveriesCompleted.increment();
    }

    public void setCouriersAvailable(int count) {
        couriersAvailable.set(count);
    }
}
