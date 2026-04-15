package com.ftgo.security.authorization;

/**
 * Defines the roles available in the FTGO platform.
 *
 * <p>Roles are stored in JWT claims and mapped to Spring Security authorities with the {@code
 * ROLE_} prefix by {@link com.ftgo.security.jwt.JwtAuthenticationConverter}.
 *
 * <p>The role hierarchy is:
 *
 * <pre>
 *   ADMIN > RESTAURANT_OWNER
 *   ADMIN > COURIER
 *   RESTAURANT_OWNER > CUSTOMER
 * </pre>
 *
 * @see RolePermissionMapping
 * @see RoleHierarchyConfiguration
 */
public enum FtgoRole {

    /** End-user who places food orders. */
    CUSTOMER,

    /** Restaurant owner who manages restaurants and processes orders. */
    RESTAURANT_OWNER,

    /** Delivery courier who picks up and delivers orders. */
    COURIER,

    /** Platform administrator with full access to all resources. */
    ADMIN;

    /** Returns the Spring Security authority string (e.g., {@code ROLE_ADMIN}). */
    public String authority() {
        return "ROLE_" + name();
    }
}
