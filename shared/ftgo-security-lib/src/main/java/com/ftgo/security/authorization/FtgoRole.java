package com.ftgo.security.authorization;

/**
 * Defines the roles available in the FTGO platform.
 *
 * <p>Roles are stored in JWT claims and used for method-level security
 * via {@code @PreAuthorize} annotations. The Spring Security authority
 * string is {@code "ROLE_" + name()} (e.g., {@code ROLE_ADMIN}).
 *
 * <h3>Role Hierarchy</h3>
 * <pre>
 *   ADMIN &gt; RESTAURANT_OWNER &gt; CUSTOMER
 *   ADMIN &gt; COURIER
 * </pre>
 */
public enum FtgoRole {

    /** End-user who places food orders. */
    CUSTOMER,

    /** Owner/manager of a restaurant registered on the platform. */
    RESTAURANT_OWNER,

    /** Delivery driver who picks up and delivers orders. */
    COURIER,

    /** Platform administrator with full access to all services. */
    ADMIN;

    /** Spring Security authority string (e.g., {@code ROLE_ADMIN}). */
    public String authority() {
        return "ROLE_" + name();
    }
}
