package com.ftgo.security.authorization;

/**
 * Fine-grained permissions for the FTGO platform.
 *
 * <p>Each permission represents a single operation within a bounded context.
 * Permissions are grouped into {@link FtgoRole}s and may also be assigned
 * individually via JWT {@code permissions} claims.
 *
 * <p>The permission naming convention is {@code CONTEXT_ACTION}
 * (e.g. {@code ORDER_CREATE}). The string authority form uses the colon-separated
 * lowercase equivalent (e.g. {@code order:create}).
 */
public enum FtgoPermission {

    // Order service
    ORDER_CREATE("order:create"),
    ORDER_READ("order:read"),
    ORDER_CANCEL("order:cancel"),
    ORDER_REVISE("order:revise"),
    ORDER_ACCEPT("order:accept"),
    ORDER_REJECT("order:reject"),
    ORDER_PREPARE("order:prepare"),
    ORDER_PICKUP("order:pickup"),
    ORDER_DELIVER("order:deliver"),

    // Consumer service
    CONSUMER_READ("consumer:read"),
    CONSUMER_CREATE("consumer:create"),
    CONSUMER_UPDATE("consumer:update"),

    // Restaurant service
    RESTAURANT_READ("restaurant:read"),
    RESTAURANT_CREATE("restaurant:create"),
    RESTAURANT_UPDATE("restaurant:update"),
    MENU_UPDATE("menu:update"),

    // Courier service
    COURIER_READ("courier:read"),
    COURIER_CREATE("courier:create"),
    COURIER_UPDATE_AVAILABILITY("courier:update-availability");

    private final String authority;

    FtgoPermission(String authority) {
        this.authority = authority;
    }

    public String authority() {
        return authority;
    }
}
