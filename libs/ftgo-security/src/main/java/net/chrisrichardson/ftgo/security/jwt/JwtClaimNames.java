package net.chrisrichardson.ftgo.security.jwt;

/**
 * Names of the custom claims carried by FTGO JWTs.
 *
 * <p>Keeping them in one place prevents typos and lets services depend on a
 * single source of truth when reading the {@code Jwt} claims map.
 */
public final class JwtClaimNames {

    /** Stable identifier for the authenticated user (UUID or numeric string). */
    public static final String USER_ID = "userId";

    /** Human-readable username or email used for display/audit purposes. */
    public static final String USERNAME = "username";

    /**
     * Non-null list of role names assigned to the user (e.g. {@code ADMIN},
     * {@code CONSUMER}). Roles are mapped onto {@code ROLE_*} authorities by
     * {@link FtgoJwtAuthenticationConverter}.
     */
    public static final String ROLES = "roles";

    /**
     * Non-null list of fine-grained permissions (e.g. {@code order:read},
     * {@code order:write}). Permissions are mapped onto {@code PERM_*}
     * authorities by {@link FtgoJwtAuthenticationConverter}.
     */
    public static final String PERMISSIONS = "permissions";

    /**
     * Discriminator so a single decoder can distinguish access tokens from
     * refresh tokens — values are {@link JwtTokenType#ACCESS} or
     * {@link JwtTokenType#REFRESH}.
     */
    public static final String TOKEN_TYPE = "token_type";

    private JwtClaimNames() {
        // Constant holder — no instantiation.
    }
}
