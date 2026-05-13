package net.chrisrichardson.ftgo.metrics;

import java.util.concurrent.atomic.AtomicInteger;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/**
 * Business metrics for the Courier Service.
 * Tracks courier availability, pickup/delivery events, and delivery timing.
 */
public class CourierMetrics {

    private final Counter couriersCreated;
    private final Counter couriersAvailable;
    private final Counter couriersUnavailable;
    private final Counter pickupsScheduled;
    private final Counter deliveriesCompleted;
    private final Timer deliveryTime;
    private final AtomicInteger currentlyAvailable = new AtomicInteger(0);

    public CourierMetrics(MeterRegistry registry) {
        couriersCreated = Counter.builder("ftgo.couriers.created")
                .description("Total number of couriers created")
                .register(registry);

        couriersAvailable = Counter.builder("ftgo.couriers.available")
                .description("Total times couriers marked as available")
                .register(registry);

        couriersUnavailable = Counter.builder("ftgo.couriers.unavailable")
                .description("Total times couriers marked as unavailable")
                .register(registry);

        Gauge.builder("ftgo.couriers.currently.available", currentlyAvailable, AtomicInteger::get)
                .description("Current number of available couriers")
                .register(registry);

        pickupsScheduled = Counter.builder("ftgo.couriers.pickups.scheduled")
                .description("Total number of pickups scheduled")
                .register(registry);

        deliveriesCompleted = Counter.builder("ftgo.couriers.deliveries.completed")
                .description("Total number of deliveries completed")
                .register(registry);

        deliveryTime = Timer.builder("ftgo.couriers.delivery.time")
                .description("Time from pickup to delivery completion")
                .register(registry);
    }

    public Counter getCouriersCreated() { return couriersCreated; }
    public Counter getCouriersAvailable() { return couriersAvailable; }
    public Counter getCouriersUnavailable() { return couriersUnavailable; }
    public Counter getPickupsScheduled() { return pickupsScheduled; }
    public Counter getDeliveriesCompleted() { return deliveriesCompleted; }
    public Timer getDeliveryTime() { return deliveryTime; }
    public AtomicInteger getCurrentlyAvailable() { return currentlyAvailable; }
}
