package com.ftgo.security.authorization;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-annotations that restrict access based on FTGO platform roles.
 *
 * <p>Each nested annotation maps to a {@code @PreAuthorize} expression
 * for one of the {@link FtgoRole} values. Apply at the method or type
 * level to enforce role-based access control.
 *
 * <p>Usage:
 * <pre>{@code
 * @RequireRole.Admin
 * @GetMapping("/api/admin/users")
 * public List<UserDto> listUsers() { ... }
 *
 * @RequireRole.Customer
 * @PostMapping("/api/orders")
 * public OrderDto createOrder(...) { ... }
 * }</pre>
 */
public final class RequireRole {

    private RequireRole() {
    }

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @PreAuthorize("hasRole('ADMIN')")
    public @interface Admin {
    }

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @PreAuthorize("hasRole('CUSTOMER')")
    public @interface Customer {
    }

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public @interface RestaurantOwner {
    }

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @PreAuthorize("hasRole('COURIER')")
    public @interface Courier {
    }

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @PreAuthorize("isAuthenticated()")
    public @interface Authenticated {
    }
}
