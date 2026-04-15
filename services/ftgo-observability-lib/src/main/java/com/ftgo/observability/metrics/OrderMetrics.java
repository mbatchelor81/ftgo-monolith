package com.ftgo.observability.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Custom business metrics for the Order Service.
 *
 * <p>Tracks order lifecycle events: creation, acceptance, cancellation, revision, and delivery.
 * Each counter is tagged with relevant dimensions for Grafana drill-down.
 */
@Component
@ConditionalOnProperty(name = "spring.application.name", havingValue = "ftgo-order-service")
public class OrderMetrics {

    private final Counter ordersCreated;
    private final Counter ordersAccepted;
    private final Counter ordersCancelled;
    private final Counter ordersRevised;
    private final Counter ordersDelivered;
    private final Timer orderProcessingTime;

    public OrderMetrics(MeterRegistry registry) {
        this.ordersCreated =
                Counter.builder("ftgo.orders.created")
                        .description("Total number of orders created")
                        .tag("service", "order-service")
                        .register(registry);

        this.ordersAccepted =
                Counter.builder("ftgo.orders.accepted")
                        .description("Total number of orders accepted by restaurants")
                        .tag("service", "order-service")
                        .register(registry);

        this.ordersCancelled =
                Counter.builder("ftgo.orders.cancelled")
                        .description("Total number of orders cancelled")
                        .tag("service", "order-service")
                        .register(registry);

        this.ordersRevised =
                Counter.builder("ftgo.orders.revised")
                        .description("Total number of orders revised")
                        .tag("service", "order-service")
                        .register(registry);

        this.ordersDelivered =
                Counter.builder("ftgo.orders.delivered")
                        .description("Total number of orders delivered")
                        .tag("service", "order-service")
                        .register(registry);

        this.orderProcessingTime =
                Timer.builder("ftgo.orders.processing.time")
                        .description("Time taken to process an order from creation to delivery")
                        .tag("service", "order-service")
                        .register(registry);
    }

    public void recordOrderCreated() {
        ordersCreated.increment();
    }

    public void recordOrderAccepted() {
        ordersAccepted.increment();
    }

    public void recordOrderCancelled() {
        ordersCancelled.increment();
    }

    public void recordOrderRevised() {
        ordersRevised.increment();
    }

    public void recordOrderDelivered() {
        ordersDelivered.increment();
    }

    public Timer getOrderProcessingTime() {
        return orderProcessingTime;
    }
}
