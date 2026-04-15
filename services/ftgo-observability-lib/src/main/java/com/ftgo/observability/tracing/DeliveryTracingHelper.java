package com.ftgo.observability.tracing;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Custom tracing spans for the Courier Service delivery operations.
 *
 * <p>Provides named spans for courier assignment, pickup, and delivery completion. Each span
 * captures relevant business context as tags.
 */
@Component
@ConditionalOnProperty(name = "spring.application.name", havingValue = "ftgo-courier-service")
public class DeliveryTracingHelper {

    private final Tracer tracer;

    public DeliveryTracingHelper(Tracer tracer) {
        this.tracer = tracer;
    }

    /**
     * Wraps courier assignment in a span named {@code delivery.assign}.
     *
     * @param courierId the courier being assigned
     * @param orderId the order for which the courier is assigned
     * @param operation the assignment logic
     */
    public void traceCourierAssignment(long courierId, long orderId, Runnable operation) {
        Span span =
                tracer.nextSpan()
                        .name("delivery.assign")
                        .tag("courier.id", String.valueOf(courierId))
                        .tag("order.id", String.valueOf(orderId))
                        .start();
        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            operation.run();
        } catch (Exception ex) {
            span.error(ex);
            throw ex;
        } finally {
            span.end();
        }
    }

    /**
     * Wraps courier availability update in a span named {@code delivery.availability}.
     *
     * @param courierId the courier whose availability is changing
     * @param available the new availability status
     * @param operation the update logic
     */
    public void traceCourierAvailability(long courierId, boolean available, Runnable operation) {
        Span span =
                tracer.nextSpan()
                        .name("delivery.availability")
                        .tag("courier.id", String.valueOf(courierId))
                        .tag("courier.available", String.valueOf(available))
                        .start();
        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            operation.run();
        } catch (Exception ex) {
            span.error(ex);
            throw ex;
        } finally {
            span.end();
        }
    }

    /**
     * Wraps delivery pickup in a span named {@code delivery.pickup}.
     *
     * @param orderId the order being picked up
     * @param operation the pickup logic
     */
    public void tracePickup(long orderId, Runnable operation) {
        Span span =
                tracer.nextSpan()
                        .name("delivery.pickup")
                        .tag("order.id", String.valueOf(orderId))
                        .start();
        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            operation.run();
        } catch (Exception ex) {
            span.error(ex);
            throw ex;
        } finally {
            span.end();
        }
    }

    /**
     * Wraps delivery completion in a span named {@code delivery.complete}.
     *
     * @param orderId the order being delivered
     * @param operation the delivery completion logic
     */
    public void traceDeliveryComplete(long orderId, Runnable operation) {
        Span span =
                tracer.nextSpan()
                        .name("delivery.complete")
                        .tag("order.id", String.valueOf(orderId))
                        .start();
        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            operation.run();
        } catch (Exception ex) {
            span.error(ex);
            throw ex;
        } finally {
            span.end();
        }
    }
}
