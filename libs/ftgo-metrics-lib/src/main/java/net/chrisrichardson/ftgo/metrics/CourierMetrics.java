package net.chrisrichardson.ftgo.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.MeterBinder;
/**
 * Business metrics for the Courier Service.
 * Tracks courier availability, pickup/delivery events, and delivery timing.
 */
public class CourierMetrics implements MeterBinder {

    private final MeterRegistry registry;

    private Counter couriersCreated;
    private Counter couriersAvailable;
    private Counter couriersUnavailable;
    private Counter pickupsScheduled;
    private Counter deliveriesCompleted;
    private Timer deliveryTime;

    public CourierMetrics(MeterRegistry registry) {
        this.registry = registry;
        bindTo(registry);
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        couriersCreated = Counter.builder("ftgo.couriers.created")
                .description("Total number of couriers created")
                .register(registry);

        couriersAvailable = Counter.builder("ftgo.couriers.available")
                .description("Total times couriers marked as available")
                .register(registry);

        couriersUnavailable = Counter.builder("ftgo.couriers.unavailable")
                .description("Total times couriers marked as unavailable")
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
}
