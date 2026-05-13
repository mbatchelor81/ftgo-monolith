package net.chrisrichardson.ftgo.tracing;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method to be wrapped in a new tracing span.
 *
 * <p>Example usage:
 * <pre>
 * {@literal @}Traced("orderService.createOrder")
 * public Order createOrder(CreateOrderRequest request) { ... }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Traced {

    /**
     * The span name. Defaults to {@code className.methodName} when left empty.
     */
    String value() default "";
}
