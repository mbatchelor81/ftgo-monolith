package com.ftgo.observability.tracing;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

/**
 * Provides convenience methods for creating custom spans around business operations.
 *
 * <p>Wraps the Micrometer {@link Tracer} to simplify span creation with consistent naming and error
 * handling across FTGO services.
 */
@Component
public class TracingHelper {

    private final Tracer tracer;

    public TracingHelper(Tracer tracer) {
        this.tracer = tracer;
    }

    /**
     * Executes a {@link Runnable} within a new span.
     *
     * @param spanName the name for the new span
     * @param operation the operation to execute
     */
    public void executeInSpan(String spanName, Runnable operation) {
        Span span = tracer.nextSpan().name(spanName).start();
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
     * Executes a {@link Supplier} within a new span and returns the result.
     *
     * @param spanName the name for the new span
     * @param operation the operation to execute
     * @param <T> the return type
     * @return the result of the operation
     */
    public <T> T executeInSpan(String spanName, Supplier<T> operation) {
        Span span = tracer.nextSpan().name(spanName).start();
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
     * Executes a {@link Runnable} within a new span, adding a custom tag.
     *
     * @param spanName the name for the new span
     * @param tagKey the tag key
     * @param tagValue the tag value
     * @param operation the operation to execute
     */
    public void executeInSpan(String spanName, String tagKey, String tagValue, Runnable operation) {
        Span span = tracer.nextSpan().name(spanName).tag(tagKey, tagValue).start();
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
     * Executes a {@link Supplier} within a new span, adding a custom tag.
     *
     * @param spanName the name for the new span
     * @param tagKey the tag key
     * @param tagValue the tag value
     * @param operation the operation to execute
     * @param <T> the return type
     * @return the result of the operation
     */
    public <T> T executeInSpan(
            String spanName, String tagKey, String tagValue, Supplier<T> operation) {
        Span span = tracer.nextSpan().name(spanName).tag(tagKey, tagValue).start();
        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            return operation.get();
        } catch (Exception ex) {
            span.error(ex);
            throw ex;
        } finally {
            span.end();
        }
    }

    /** Returns the underlying {@link Tracer} for advanced use cases. */
    public Tracer getTracer() {
        return tracer;
    }
}
