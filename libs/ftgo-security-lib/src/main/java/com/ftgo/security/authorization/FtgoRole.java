package com.ftgo.security.authorization;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Defines the roles available in the FTGO platform.
 *
 * <p>Each role maps to a set of {@link FtgoPermission}s that determine
 * what operations the role holder may perform. Roles are stored in JWT
 * claims and mapped to Spring Security {@code ROLE_} authorities by
 * {@link com.ftgo.security.jwt.FtgoJwtAuthenticationConverter}.
 */
public enum FtgoRole {

    CUSTOMER(EnumSet.of(
            FtgoPermission.ORDER_CREATE,
            FtgoPermission.ORDER_READ,
            FtgoPermission.ORDER_CANCEL,
            FtgoPermission.ORDER_REVISE,
            FtgoPermission.CONSUMER_READ
    )),

    RESTAURANT_OWNER(EnumSet.of(
            FtgoPermission.ORDER_READ,
            FtgoPermission.ORDER_ACCEPT,
            FtgoPermission.ORDER_REJECT,
            FtgoPermission.ORDER_PREPARE,
            FtgoPermission.RESTAURANT_READ,
            FtgoPermission.RESTAURANT_UPDATE,
            FtgoPermission.MENU_UPDATE
    )),

    COURIER(EnumSet.of(
            FtgoPermission.ORDER_READ,
            FtgoPermission.ORDER_PICKUP,
            FtgoPermission.ORDER_DELIVER,
            FtgoPermission.COURIER_READ,
            FtgoPermission.COURIER_UPDATE_AVAILABILITY
    )),

    ADMIN(EnumSet.allOf(FtgoPermission.class));

    private final Set<FtgoPermission> permissions;

    FtgoRole(Set<FtgoPermission> permissions) {
        this.permissions = Collections.unmodifiableSet(permissions);
    }

    public Set<FtgoPermission> getPermissions() {
        return permissions;
    }

    public boolean hasPermission(FtgoPermission permission) {
        return permissions.contains(permission);
    }

    public String authority() {
        return "ROLE_" + name();
    }
}
