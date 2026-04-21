package net.chrisrichardson.ftgo.security.authorization;

/**
 * Canonical FTGO role catalog shared by every microservice.
 *
 * <p>Each role maps 1:1 to a Spring Security {@code ROLE_*} authority via
 * {@link #authority()}. The role hierarchy is defined centrally in
 * {@link MethodSecurityConfiguration}:
 *
 * <pre>{@code
 *   ADMIN
 *    ├── RESTAURANT_OWNER ──┐
 *    └── COURIER           ──┼──▶ CUSTOMER
 * }</pre>
 *
 * <p>Role names are written into the JWT {@code roles} claim verbatim (see
 * {@link net.chrisrichardson.ftgo.security.jwt.JwtClaimNames#ROLES}) and
 * converted to authorities by
 * {@link net.chrisrichardson.ftgo.security.jwt.FtgoJwtAuthenticationConverter}.
 */
public enum Role {

    /** End-user that places orders. Alias for the "consumer" bounded context. */
    CUSTOMER,

    /** Operator that manages a restaurant's menu and accepts orders. */
    RESTAURANT_OWNER,

    /** Delivery driver that picks up and delivers orders. */
    COURIER,

    /** Platform operator. Inherits every other role via the role hierarchy. */
    ADMIN;

    /** Authority prefix Spring Security uses for role-based expressions. */
    public static final String ROLE_AUTHORITY_PREFIX = "ROLE_";

    /** The Spring Security authority name for this role (e.g. {@code ROLE_ADMIN}). */
    public String authority() {
        return ROLE_AUTHORITY_PREFIX + name();
    }

    /** The claim value written into the JWT {@code roles} array. */
    public String claimValue() {
        return name();
    }
}
