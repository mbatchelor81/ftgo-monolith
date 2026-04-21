package net.chrisrichardson.ftgo.security.jwt;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Immutable projection of the FTGO-specific claims carried by an access token.
 *
 * <p>Exposed to application code via {@code SecurityUtils.getCurrentPrincipal()},
 * which constructs the principal on demand from the current request's decoded
 * {@link Jwt}. The principal is <em>not</em> attached via
 * {@code Authentication.getDetails()} because Spring Security's
 * {@code JwtAuthenticationProvider} unconditionally overwrites that slot with
 * its own {@code WebAuthenticationDetails}.
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

    /**
     * Builds a principal from a decoded access-token {@link Jwt}, reading the
     * {@code user_id}, {@code username} (falling back to {@code sub}),
     * {@code roles}, and {@code permissions} claims. Called by
     * {@code SecurityUtils.getCurrentPrincipal()} and by
     * {@link FtgoJwtAuthenticationConverter} so both paths share one
     * canonical claim-extraction implementation.
     */
    public static FtgoJwtPrincipal fromJwt(Jwt jwt) {
        return new FtgoJwtPrincipal(
                jwt.getClaimAsString(JwtClaimNames.USER_ID),
                resolveUsername(jwt),
                toSet(jwt.getClaimAsStringList(JwtClaimNames.ROLES)),
                toSet(jwt.getClaimAsStringList(JwtClaimNames.PERMISSIONS))
        );
    }

    private static String resolveUsername(Jwt jwt) {
        String username = jwt.getClaimAsString(JwtClaimNames.USERNAME);
        return username != null ? username : jwt.getSubject();
    }

    private static Set<String> toSet(List<String> values) {
        return values == null ? Set.of() : new LinkedHashSet<>(values);
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
