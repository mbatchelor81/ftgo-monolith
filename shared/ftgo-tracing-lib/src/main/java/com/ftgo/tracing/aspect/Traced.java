package com.ftgo.tracing.aspect;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method for custom span creation in distributed tracing.
 *
 * <p>When applied to a method, the {@link TracedAspect} creates a new span
 * that wraps the method execution. The span name defaults to the method name
 * but can be overridden via {@link #value()}.
 *
 * <p>Example usage:
 * <pre>
 * {@code @Traced("order.create")}
 * public Order createOrder(CreateOrderRequest request) { ... }
 *
 * {@code @Traced} // uses method name as span name
 * public void validateConsumer(long consumerId, Money orderTotal) { ... }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Traced {

    /**
     * Custom span name. Defaults to the annotated method name if empty.
     */
    String value() default "";

    /**
     * Additional tag key-value pairs for the span in {@code "key=value"} format.
     *
     * <p>Example: {@code @Traced(tags = {"operation=create", "domain=order"})}
     */
    String[] tags() default {};
}
