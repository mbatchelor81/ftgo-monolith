package net.chrisrichardson.ftgo.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.MeterBinder;
/**
 * Business metrics for the Order Service.
 * Tracks order lifecycle events: creation, approval, rejection, cancellation, revision, and delivery.
 */
public class OrderMetrics implements MeterBinder {

    private final MeterRegistry registry;

    private Counter ordersCreated;
    private Counter ordersApproved;
    private Counter ordersRejected;
    private Counter ordersCancelled;
    private Counter ordersRevised;
    private Counter ordersDelivered;
    private Timer orderProcessingTime;
    private Timer orderFulfillmentTime;

    public OrderMetrics(MeterRegistry registry) {
        this.registry = registry;
        bindTo(registry);
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        ordersCreated = Counter.builder("ftgo.orders.created")
                .description("Total number of orders created")
                .register(registry);

        ordersApproved = Counter.builder("ftgo.orders.approved")
                .description("Total number of orders approved by restaurants")
                .register(registry);

        ordersRejected = Counter.builder("ftgo.orders.rejected")
                .description("Total number of orders rejected")
                .register(registry);

        ordersCancelled = Counter.builder("ftgo.orders.cancelled")
                .description("Total number of orders cancelled")
                .register(registry);

        ordersRevised = Counter.builder("ftgo.orders.revised")
                .description("Total number of orders revised")
                .register(registry);

        ordersDelivered = Counter.builder("ftgo.orders.delivered")
                .description("Total number of orders delivered")
                .register(registry);

        orderProcessingTime = Timer.builder("ftgo.orders.processing.time")
                .description("Time taken from order creation to restaurant acceptance")
                .register(registry);

        orderFulfillmentTime = Timer.builder("ftgo.orders.fulfillment.time")
                .description("Time taken from order creation to delivery")
                .register(registry);
    }

    public Counter getOrdersCreated() { return ordersCreated; }
    public Counter getOrdersApproved() { return ordersApproved; }
    public Counter getOrdersRejected() { return ordersRejected; }
    public Counter getOrdersCancelled() { return ordersCancelled; }
    public Counter getOrdersRevised() { return ordersRevised; }
    public Counter getOrdersDelivered() { return ordersDelivered; }
    public Timer getOrderProcessingTime() { return orderProcessingTime; }
    public Timer getOrderFulfillmentTime() { return orderFulfillmentTime; }
}
