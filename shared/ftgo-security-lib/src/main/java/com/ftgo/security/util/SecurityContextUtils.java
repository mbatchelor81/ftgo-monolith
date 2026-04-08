package com.ftgo.security.util;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Utility class for accessing the current Spring Security context.
 *
 * <p>Provides convenience methods for extracting the current principal,
 * checking authentication status, and retrieving granted authorities.
 * All methods are null-safe and return {@link Optional} where appropriate.
 */
public final class SecurityContextUtils {

    private SecurityContextUtils() {
        // Utility class — prevent instantiation
    }

    /**
     * Returns the current {@link Authentication} if the user is authenticated
     * (and not anonymous).
     */
    public static Optional<Authentication> getCurrentAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        return Optional.of(auth);
    }

    /**
     * Returns the username (principal name) of the currently authenticated user.
     */
    public static Optional<String> getCurrentUsername() {
        return getCurrentAuthentication().map(Authentication::getName);
    }

    /**
     * Returns {@code true} if a non-anonymous user is currently authenticated.
     */
    public static boolean isAuthenticated() {
        return getCurrentAuthentication().isPresent();
    }

    /**
     * Returns the granted authorities of the current user, or an empty
     * collection if not authenticated.
     */
    public static Collection<? extends GrantedAuthority> getCurrentAuthorities() {
        return getCurrentAuthentication()
            .map(Authentication::getAuthorities)
            .orElse(Collections.emptyList());
    }

    /**
     * Returns {@code true} if the current user has the specified authority.
     *
     * @param authority the authority string (e.g., "ROLE_ADMIN")
     */
    public static boolean hasAuthority(String authority) {
        return getCurrentAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals(authority));
    }
}
