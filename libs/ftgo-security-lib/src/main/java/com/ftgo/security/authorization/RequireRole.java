package com.ftgo.security.authorization;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-annotation that restricts access to users holding the
 * {@code ROLE_ADMIN} authority.
 *
 * <p>Usage:
 * <pre>{@code
 * @RequireRole.Admin
 * @GetMapping("/api/admin/users")
 * public List<UserDto> listUsers() { ... }
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
    @PreAuthorize("hasAnyRole('CUSTOMER', 'RESTAURANT_OWNER', 'COURIER', 'ADMIN')")
    public @interface Authenticated {
    }
}
