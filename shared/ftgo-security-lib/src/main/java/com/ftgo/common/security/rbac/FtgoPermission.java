package com.ftgo.common.security.rbac;

/**
 * Permission constants for fine-grained access control across FTGO services.
 *
 * <p>Used with {@code @PreAuthorize("hasAuthority('permission')")} annotations.
 */
public final class FtgoPermission {

    private FtgoPermission() {}

    // Consumer Service permissions
    public static final String CONSUMER_CREATE = "consumer:create";
    public static final String CONSUMER_READ = "consumer:read";
    public static final String CONSUMER_READ_OWN = "consumer:read:own";

    // Order Service permissions
    public static final String ORDER_CREATE = "order:create";
    public static final String ORDER_READ = "order:read";
    public static final String ORDER_READ_OWN = "order:read:own";
    public static final String ORDER_CANCEL = "order:cancel";
    public static final String ORDER_REVISE = "order:revise";
    public static final String ORDER_ACCEPT = "order:accept";
    public static final String ORDER_UPDATE_STATUS = "order:update-status";

    // Restaurant Service permissions
    public static final String RESTAURANT_CREATE = "restaurant:create";
    public static final String RESTAURANT_READ = "restaurant:read";
    public static final String RESTAURANT_UPDATE = "restaurant:update";
    public static final String RESTAURANT_UPDATE_OWN = "restaurant:update:own";

    // Courier Service permissions
    public static final String COURIER_CREATE = "courier:create";
    public static final String COURIER_READ = "courier:read";
    public static final String COURIER_UPDATE_AVAILABILITY = "courier:update-availability";
    public static final String COURIER_UPDATE_AVAILABILITY_OWN = "courier:update-availability:own";
}
