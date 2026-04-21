package com.ftgo.courier.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

/**
 * Business metrics published by the Courier microservice.
 *
 * <p>The {@code couriers.available} gauge is emitted as a counter of
 * availability transitions; absolute availability counts live in the
 * Courier domain and should be wired in as a {@link io.micrometer.core.instrument.Gauge}
 * when the repository is lifted out of the legacy module.
 */
@Component
public class CourierMetrics {

    public static final String COURIERS_REGISTERED = "couriers.registered";
    public static final String COURIERS_AVAILABILITY_CHANGED = "couriers.availability.changed";
    public static final String DELIVERIES_COMPLETED = "couriers.deliveries.completed";
    public static final String DELIVERY_DURATION = "couriers.delivery.duration";

    private final Counter couriersRegistered;
    private final Counter availabilityChanges;
    private final Counter deliveriesCompleted;
    private final Timer deliveryTimer;

    public CourierMetrics(MeterRegistry registry) {
        this.couriersRegistered = Counter.builder(COURIERS_REGISTERED)
                .description("Total number of couriers onboarded")
                .tag("service", "courier-service")
                .register(registry);

        this.availabilityChanges = Counter.builder(COURIERS_AVAILABILITY_CHANGED)
                .description("Total courier availability transitions (available <-> unavailable)")
                .tag("service", "courier-service")
                .register(registry);

        this.deliveriesCompleted = Counter.builder(DELIVERIES_COMPLETED)
                .description("Total number of successful courier deliveries")
                .tag("service", "courier-service")
                .register(registry);

        this.deliveryTimer = Timer.builder(DELIVERY_DURATION)
                .description("End-to-end delivery time from pickup to drop-off")
                .tag("service", "courier-service")
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram()
                .register(registry);
    }

    public void recordCourierRegistered() {
        couriersRegistered.increment();
    }

    public void recordAvailabilityChanged() {
        availabilityChanges.increment();
    }

    public void recordDeliveryCompleted() {
        deliveriesCompleted.increment();
    }

    public Timer deliveryTimer() {
        return deliveryTimer;
    }
}
