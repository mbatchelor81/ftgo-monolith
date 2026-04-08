package com.ftgo.security.authorization;

/**
 * Fine-grained permissions for the FTGO platform, organized by bounded context.
 *
 * <p>Permissions are stored in JWT claims and can be checked via
 * {@code @PreAuthorize("hasAuthority('...')")} or
 * {@link com.ftgo.security.jwt.FtgoUserDetails#hasPermission(String)}.
 *
 * <p>Naming convention: {@code <context>:<action>}
 */
public final class FtgoPermission {

    private FtgoPermission() {
        // Utility class — prevent instantiation
    }

    // ---- Consumer Service ----
    public static final String CONSUMER_CREATE = "consumer:create";
    public static final String CONSUMER_READ = "consumer:read";
    public static final String CONSUMER_UPDATE = "consumer:update";
    public static final String CONSUMER_DELETE = "consumer:delete";

    // ---- Order Service ----
    public static final String ORDER_CREATE = "order:create";
    public static final String ORDER_READ = "order:read";
    public static final String ORDER_CANCEL = "order:cancel";
    public static final String ORDER_REVISE = "order:revise";
    public static final String ORDER_ACCEPT = "order:accept";

    // ---- Restaurant Service ----
    public static final String RESTAURANT_CREATE = "restaurant:create";
    public static final String RESTAURANT_READ = "restaurant:read";
    public static final String RESTAURANT_UPDATE = "restaurant:update";
    public static final String RESTAURANT_DELETE = "restaurant:delete";

    // ---- Courier Service ----
    public static final String COURIER_CREATE = "courier:create";
    public static final String COURIER_READ = "courier:read";
    public static final String COURIER_UPDATE_AVAILABILITY = "courier:update_availability";
}
