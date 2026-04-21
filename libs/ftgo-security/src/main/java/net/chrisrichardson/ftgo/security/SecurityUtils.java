package net.chrisrichardson.ftgo.security;

import net.chrisrichardson.ftgo.security.jwt.FtgoJwtAuthenticationConverter;
import net.chrisrichardson.ftgo.security.jwt.FtgoJwtPrincipal;
import net.chrisrichardson.ftgo.security.jwt.JwtClaimNames;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility helpers for inspecting the current {@link SecurityContextHolder}.
 *
 * <p>Kept intentionally small — only the accessors the rest of the code base
 * actually needs. Any additional helpers should have a real caller before
 * being added.
 */
public final class SecurityUtils {

    private static final String ANONYMOUS_USER = "anonymousUser";

    private SecurityUtils() {
        // Utility class — no instantiation.
    }

    /**
     * Returns the username for the current request, or {@link Optional#empty()}
     * when the request is anonymous or unauthenticated.
     */
    public static Optional<String> getCurrentUsername() {
        return currentAuthentication()
                .map(Authentication::getName)
                .filter(name -> !ANONYMOUS_USER.equals(name));
    }

    /**
     * Returns true when the current request carries an authenticated,
     * non-anonymous principal.
     */
    public static boolean isAuthenticated() {
        return getCurrentUsername().isPresent();
    }

    /**
     * Returns the granted authorities attached to the current authentication.
     * Never {@code null} — an empty set is returned when the request is anonymous.
     */
    public static Collection<String> getCurrentAuthorities() {
        return currentAuthentication()
                .map(auth -> auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toUnmodifiableSet()))
                .orElse(Collections.emptySet());
    }

    /**
     * Returns the stable user identifier carried by the {@code userId} claim
     * of the current access token, or {@link Optional#empty()} for anonymous
     * requests and requests authenticated via HTTP Basic (no JWT).
     */
    public static Optional<String> getCurrentUserId() {
        return getCurrentPrincipal()
                .map(FtgoJwtPrincipal::userId)
                .or(() -> currentJwt().map(jwt -> jwt.getClaimAsString(JwtClaimNames.USER_ID)));
    }

    /**
     * Returns the role names (without the {@code ROLE_} prefix) carried by
     * the current access token's {@code roles} claim.
     */
    public static Set<String> getCurrentRoles() {
        return getCurrentPrincipal()
                .map(FtgoJwtPrincipal::roles)
                .or(() -> currentJwt().map(jwt -> toSet(jwt.getClaimAsStringList(JwtClaimNames.ROLES))))
                .orElse(Set.of());
    }

    /**
     * Returns the permission identifiers (without the {@code PERM_} prefix)
     * carried by the current access token's {@code permissions} claim.
     */
    public static Set<String> getCurrentPermissions() {
        return getCurrentPrincipal()
                .map(FtgoJwtPrincipal::permissions)
                .or(() -> currentJwt().map(jwt -> toSet(jwt.getClaimAsStringList(JwtClaimNames.PERMISSIONS))))
                .orElse(Set.of());
    }

    /**
     * Returns the {@link FtgoJwtPrincipal} attached to the current request
     * by {@link FtgoJwtAuthenticationConverter}, or {@link Optional#empty()}
     * when the request is not authenticated via JWT.
     */
    public static Optional<FtgoJwtPrincipal> getCurrentPrincipal() {
        return currentAuthentication()
                .filter(JwtAuthenticationToken.class::isInstance)
                .map(JwtAuthenticationToken.class::cast)
                .map(Authentication::getDetails)
                .filter(FtgoJwtPrincipal.class::isInstance)
                .map(FtgoJwtPrincipal.class::cast);
    }

    private static Optional<Jwt> currentJwt() {
        return currentAuthentication()
                .filter(JwtAuthenticationToken.class::isInstance)
                .map(JwtAuthenticationToken.class::cast)
                .map(JwtAuthenticationToken::getToken);
    }

    private static Set<String> toSet(List<String> values) {
        return values == null ? Set.of() : new LinkedHashSet<>(values);
    }

    private static Optional<Authentication> currentAuthentication() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated);
    }
}
