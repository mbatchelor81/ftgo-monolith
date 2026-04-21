package net.chrisrichardson.ftgo.security.jwt;

import java.util.Set;

/**
 * Immutable projection of the FTGO-specific claims carried by an access token.
 *
 * <p>Set as the {@code Authentication.principal} by
 * {@link FtgoJwtAuthenticationConverter}, allowing downstream services to
 * read the caller's identity without re-parsing raw claims:
 *
 * <pre>{@code
 * FtgoJwtPrincipal principal = SecurityUtils.getCurrentPrincipal().orElseThrow();
 * principal.userId();       // "user-42"
 * principal.roles();        // Set.of("CONSUMER")
 * principal.permissions();  // Set.of("order:read", "order:write")
 * }</pre>
 */
public record FtgoJwtPrincipal(
        String userId,
        String username,
        Set<String> roles,
        Set<String> permissions
) {

    public FtgoJwtPrincipal {
        roles = roles == null ? Set.of() : Set.copyOf(roles);
        permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
    }

    /** Convenience: does the principal carry the given role? */
    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    /** Convenience: does the principal carry the given permission? */
    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }
}
