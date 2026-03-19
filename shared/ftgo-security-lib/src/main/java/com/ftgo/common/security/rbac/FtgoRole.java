package com.ftgo.common.security.rbac;

/**
 * Defines the role model for FTGO microservices RBAC.
 *
 * <p>Role hierarchy: ADMIN > RESTAURANT_OWNER > COURIER > CUSTOMER
 */
public enum FtgoRole {

    CUSTOMER("ROLE_CUSTOMER", "End consumer who places orders"),
    COURIER("ROLE_COURIER", "Delivery driver who fulfills orders"),
    RESTAURANT_OWNER("ROLE_RESTAURANT_OWNER", "Restaurant operator who manages menus and accepts orders"),
    ADMIN("ROLE_ADMIN", "System administrator with full access");

    private final String authority;
    private final String description;

    FtgoRole(String authority, String description) {
        this.authority = authority;
        this.description = description;
    }

    public String getAuthority() {
        return authority;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Returns the role name without the ROLE_ prefix, suitable for use with
     * {@code hasRole()} SpEL expressions.
     */
    public String roleName() {
        return name();
    }
}
