package com.ftgo.observability.tracing;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import java.util.function.Supplier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Custom tracing spans for the Order Service business operations.
 *
 * <p>Provides named spans for order creation, acceptance, revision, cancellation, and the full
 * delivery lifecycle. Each span captures relevant business context as tags.
 */
@Component
@ConditionalOnProperty(name = "spring.application.name", havingValue = "ftgo-order-service")
public class OrderTracingHelper {

    private final Tracer tracer;

    public OrderTracingHelper(Tracer tracer) {
        this.tracer = tracer;
    }

    /**
     * Wraps the order creation flow in a span named {@code order.create}.
     *
     * @param consumerId the consumer placing the order
     * @param restaurantId the restaurant fulfilling the order
     * @param operation the creation logic to execute
     * @param <T> the return type (typically the created order or response DTO)
     * @return the result of the operation
     */
    public <T> T traceOrderCreation(long consumerId, long restaurantId, Supplier<T> operation) {
        Span span =
                tracer.nextSpan()
                        .name("order.create")
                        .tag("order.consumerId", String.valueOf(consumerId))
                        .tag("order.restaurantId", String.valueOf(restaurantId))
                        .start();
        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            return operation.get();
        } catch (Exception ex) {
            span.error(ex);
            throw ex;
        } finally {
            span.end();
        }
    }

    /**
     * Wraps order acceptance in a span named {@code order.accept}.
     *
     * @param orderId the order being accepted
     * @param operation the acceptance logic
     */
    public void traceOrderAcceptance(long orderId, Runnable operation) {
        Span span =
                tracer.nextSpan()
                        .name("order.accept")
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
     * Wraps order cancellation in a span named {@code order.cancel}.
     *
     * @param orderId the order being cancelled
     * @param operation the cancellation logic
     */
    public void traceOrderCancellation(long orderId, Runnable operation) {
        Span span =
                tracer.nextSpan()
                        .name("order.cancel")
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
     * Wraps order revision in a span named {@code order.revise}.
     *
     * @param orderId the order being revised
     * @param operation the revision logic
     * @param <T> the return type
     * @return the result of the operation
     */
    public <T> T traceOrderRevision(long orderId, Supplier<T> operation) {
        Span span =
                tracer.nextSpan()
                        .name("order.revise")
                        .tag("order.id", String.valueOf(orderId))
                        .start();
        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            return operation.get();
        } catch (Exception ex) {
            span.error(ex);
            throw ex;
        } finally {
            span.end();
        }
    }
}
