package net.chrisrichardson.ftgo.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
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

    private static Optional<Authentication> currentAuthentication() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated);
    }
}
