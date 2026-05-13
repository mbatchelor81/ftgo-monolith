package com.ftgo.security.authorization;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-annotations that restrict access based on fine-grained permissions.
 *
 * <p>Usage:
 * <pre>{@code
 * @RequirePermission.OrderCreate
 * @PostMapping("/api/orders")
 * public OrderDto createOrder(@RequestBody CreateOrderRequest req) { ... }
 * }</pre>
 */
public final class RequirePermission {

    private RequirePermission() {
    }

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @PreAuthorize("hasAuthority('order:create')")
    public @interface OrderCreate {
    }

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @PreAuthorize("hasAuthority('order:read')")
    public @interface OrderRead {
    }

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @PreAuthorize("hasAuthority('order:cancel')")
    public @interface OrderCancel {
    }

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @PreAuthorize("hasAuthority('order:accept')")
    public @interface OrderAccept {
    }

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @PreAuthorize("hasAuthority('restaurant:read')")
    public @interface RestaurantRead {
    }

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @PreAuthorize("hasAuthority('restaurant:update')")
    public @interface RestaurantUpdate {
    }

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @PreAuthorize("hasAuthority('courier:read')")
    public @interface CourierRead {
    }

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @PreAuthorize("hasAuthority('courier:update-availability')")
    public @interface CourierUpdateAvailability {
    }
}
