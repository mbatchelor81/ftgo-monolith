package com.ftgo.order.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

/**
 * Business metrics published by the Order microservice.
 *
 * <p>Every instrument is registered eagerly against the shared
 * {@link MeterRegistry} so it appears on {@code /actuator/prometheus} even
 * before the first business event is recorded. This keeps Grafana panels
 * from reporting "No data" during a cold start and lets alerting rules
 * reference the series name unconditionally.
 *
 * <p>Counter / timer naming follows the dot-delimited Micrometer convention
 * documented in {@code platform/observability/README.md} and is reused by
 * the Grafana dashboard under
 * {@code platform/observability/grafana/dashboards/}.
 */
@Component
public class OrderMetrics {

    public static final String ORDERS_CREATED = "orders.created";
    public static final String ORDERS_CANCELLED = "orders.cancelled";
    public static final String ORDERS_REVISED = "orders.revised";
    public static final String ORDERS_DELIVERED = "orders.delivered";
    public static final String ORDER_PROCESSING_TIME = "orders.processing.time";

    private final Counter ordersCreated;
    private final Counter ordersCancelled;
    private final Counter ordersRevised;
    private final Counter ordersDelivered;
    private final Timer orderProcessingTimer;

    public OrderMetrics(MeterRegistry registry) {
        this.ordersCreated = Counter.builder(ORDERS_CREATED)
                .description("Total number of orders created by consumers")
                .tag("service", "order-service")
                .register(registry);

        this.ordersCancelled = Counter.builder(ORDERS_CANCELLED)
                .description("Total number of orders cancelled before delivery")
                .tag("service", "order-service")
                .register(registry);

        this.ordersRevised = Counter.builder(ORDERS_REVISED)
                .description("Total number of in-flight orders whose line items were revised")
                .tag("service", "order-service")
                .register(registry);

        this.ordersDelivered = Counter.builder(ORDERS_DELIVERED)
                .description("Total number of orders marked as delivered to the consumer")
                .tag("service", "order-service")
                .register(registry);

        this.orderProcessingTimer = Timer.builder(ORDER_PROCESSING_TIME)
                .description("End-to-end processing time from order creation to delivery")
                .tag("service", "order-service")
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram()
                .register(registry);
    }

    public void recordOrderCreated() {
        ordersCreated.increment();
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

    public Timer processingTimer() {
        return orderProcessingTimer;
    }
}
