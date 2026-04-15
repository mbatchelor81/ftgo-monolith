package com.ftgo.security.authorization;

/**
 * Fine-grained permission constants for method-level authorization.
 *
 * <p>Permissions follow the pattern {@code {domain}:{action}} or {@code {domain}:{action}:{scope}}.
 * The {@code :own} suffix indicates ownership-scoped permissions that require the authenticated
 * user to own the target resource.
 *
 * @see RolePermissionMapping
 * @see FtgoPermissionEvaluator
 */
public final class FtgoPermission {

    private FtgoPermission() {
        // Utility class — no instantiation
    }

    // ── Consumer Service ──────────────────────────────────────────────

    /** Create a new consumer profile. */
    public static final String CONSUMER_CREATE = "consumer:create";

    /** Read any consumer profile. */
    public static final String CONSUMER_READ = "consumer:read";

    /** Read only the authenticated user's own consumer profile. */
    public static final String CONSUMER_READ_OWN = "consumer:read:own";

    // ── Order Service ─────────────────────────────────────────────────

    /** Create a new order. */
    public static final String ORDER_CREATE = "order:create";

    /** Read any order. */
    public static final String ORDER_READ = "order:read";

    /** Read only orders owned by the authenticated user. */
    public static final String ORDER_READ_OWN = "order:read:own";

    /** Cancel any order. */
    public static final String ORDER_CANCEL = "order:cancel";

    /** Cancel only orders owned by the authenticated user. */
    public static final String ORDER_CANCEL_OWN = "order:cancel:own";

    /** Revise any order. */
    public static final String ORDER_REVISE = "order:revise";

    /** Accept a restaurant order (transition to ACCEPTED). */
    public static final String ORDER_ACCEPT = "order:accept";

    /** Update order status (preparing, ready for pickup, etc.). */
    public static final String ORDER_STATUS_UPDATE = "order:status:update";

    // ── Restaurant Service ────────────────────────────────────────────

    /** Create a new restaurant. */
    public static final String RESTAURANT_CREATE = "restaurant:create";

    /** Read any restaurant or menu. */
    public static final String RESTAURANT_READ = "restaurant:read";

    /** Update any restaurant. */
    public static final String RESTAURANT_UPDATE = "restaurant:update";

    /** Update only restaurants owned by the authenticated user. */
    public static final String RESTAURANT_UPDATE_OWN = "restaurant:update:own";

    /** Delete a restaurant. */
    public static final String RESTAURANT_DELETE = "restaurant:delete";

    // ── Courier Service ───────────────────────────────────────────────

    /** Create a new courier profile. */
    public static final String COURIER_CREATE = "courier:create";

    /** Read any courier profile. */
    public static final String COURIER_READ = "courier:read";

    /** Read only the authenticated user's own courier profile. */
    public static final String COURIER_READ_OWN = "courier:read:own";

    /** Update availability for any courier. */
    public static final String COURIER_UPDATE_AVAILABILITY = "courier:update:availability";

    /** Update only the authenticated courier's own availability. */
    public static final String COURIER_UPDATE_OWN_AVAILABILITY = "courier:update:availability:own";

    /** Update delivery status (picked up, delivered). */
    public static final String COURIER_DELIVERY_UPDATE = "courier:delivery:update";
}
