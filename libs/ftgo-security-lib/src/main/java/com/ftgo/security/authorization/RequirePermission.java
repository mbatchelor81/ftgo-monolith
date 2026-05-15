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
 * <p>Each nested annotation corresponds to a {@link FtgoPermission} value.
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

    // -- Order service --

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
    @PreAuthorize("hasAuthority('order:revise')")
    public @interface OrderRevise {
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
    @PreAuthorize("hasAuthority('order:reject')")
    public @interface OrderReject {
    }

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @PreAuthorize("hasAuthority('order:prepare')")
    public @interface OrderPrepare {
    }

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @PreAuthorize("hasAuthority('order:pickup')")
    public @interface OrderPickup {
    }

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @PreAuthorize("hasAuthority('order:deliver')")
    public @interface OrderDeliver {
    }

    // -- Consumer service --

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @PreAuthorize("hasAuthority('consumer:read')")
    public @interface ConsumerRead {
    }

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @PreAuthorize("hasAuthority('consumer:create')")
    public @interface ConsumerCreate {
    }

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @PreAuthorize("hasAuthority('consumer:update')")
    public @interface ConsumerUpdate {
    }

    // -- Restaurant service --

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @PreAuthorize("hasAuthority('restaurant:read')")
    public @interface RestaurantRead {
    }

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @PreAuthorize("hasAuthority('restaurant:create')")
    public @interface RestaurantCreate {
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
    @PreAuthorize("hasAuthority('menu:update')")
    public @interface MenuUpdate {
    }

    // -- Courier service --

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @PreAuthorize("hasAuthority('courier:read')")
    public @interface CourierRead {
    }

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @PreAuthorize("hasAuthority('courier:create')")
    public @interface CourierCreate {
    }

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @PreAuthorize("hasAuthority('courier:update-availability')")
    public @interface CourierUpdateAvailability {
    }
}
