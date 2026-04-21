package net.chrisrichardson.ftgo.security.authorization;

/**
 * Fine-grained permission identifiers used across FTGO services.
 *
 * <p>Permissions are issued as the JWT {@code permissions} claim and surface
 * on the {@link org.springframework.security.core.Authentication} as
 * {@code PERM_*} authorities (see
 * {@link net.chrisrichardson.ftgo.security.jwt.FtgoJwtAuthenticationConverter}).
 * Services reference them from {@code @PreAuthorize} expressions with
 * {@code hasAuthority('PERM_order:read')}.
 *
 * <p>Naming convention: {@code <bounded-context>:<verb>[:<sub-resource>]}.
 * Keeping the verbs small and consistent prevents the permission catalog
 * from sprawling as new services are added.
 *
 * <p>See {@code libs/ftgo-security/AUTHORIZATION.md} for the permission
 * matrix — which roles grant which permissions.
 */
public final class Permissions {

    private Permissions() {
        // Constant holder — no instantiation.
    }

    /** Authority prefix Spring Security sees after {@code FtgoJwtAuthenticationConverter}. */
    public static final String PERMISSION_AUTHORITY_PREFIX = "PERM_";

    // ------------------------------------------------------------------
    // Consumer bounded context
    // ------------------------------------------------------------------

    /** Read a consumer profile. Customers can read their own profile only. */
    public static final String CONSUMER_READ = "consumer:read";

    /** Create or update a consumer profile. */
    public static final String CONSUMER_WRITE = "consumer:write";

    /** List or manage any consumer (admin-only). */
    public static final String CONSUMER_ADMIN = "consumer:admin";

    // ------------------------------------------------------------------
    // Order bounded context
    // ------------------------------------------------------------------

    /** Read an order. Customers can read their own orders only. */
    public static final String ORDER_READ = "order:read";

    /** Create a new order. */
    public static final String ORDER_CREATE = "order:create";

    /** Cancel an order the caller owns. */
    public static final String ORDER_CANCEL = "order:cancel";

    /** Revise (add/change/remove line items) on an order the caller owns. */
    public static final String ORDER_REVISE = "order:revise";

    /** Accept / prepare / note-ready / deliver — state transitions driven by restaurants and couriers. */
    public static final String ORDER_FULFILL = "order:fulfill";

    /** Read or mutate any order regardless of owner (admin-only). */
    public static final String ORDER_ADMIN = "order:admin";

    // ------------------------------------------------------------------
    // Restaurant bounded context
    // ------------------------------------------------------------------

    /** Read a restaurant record — menu, opening hours, etc. */
    public static final String RESTAURANT_READ = "restaurant:read";

    /** Create or update a restaurant record. */
    public static final String RESTAURANT_WRITE = "restaurant:write";

    /** Manage a restaurant's menu items. */
    public static final String RESTAURANT_MENU_MANAGE = "restaurant:menu:manage";

    /** Mutate any restaurant (admin-only). */
    public static final String RESTAURANT_ADMIN = "restaurant:admin";

    // ------------------------------------------------------------------
    // Courier bounded context
    // ------------------------------------------------------------------

    /** Read a courier profile. Couriers can read their own profile only. */
    public static final String COURIER_READ = "courier:read";

    /** Create or update a courier profile. */
    public static final String COURIER_WRITE = "courier:write";

    /** Toggle a courier's own availability (on-shift / off-shift). */
    public static final String COURIER_AVAILABILITY_UPDATE = "courier:availability:update";

    /** Mutate any courier (admin-only). */
    public static final String COURIER_ADMIN = "courier:admin";
}
