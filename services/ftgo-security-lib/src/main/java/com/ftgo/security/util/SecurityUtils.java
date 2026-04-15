package com.ftgo.security.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class for common security operations.
 *
 * <p>Provides static helper methods to inspect the current security context,
 * retrieve the authenticated principal, and check authorities/roles.
 */
public final class SecurityUtils {

    private SecurityUtils() {
        // Utility class — no instantiation
    }

    /**
     * Returns the current {@link Authentication} if present.
     */
    public static Optional<Authentication> getCurrentAuthentication() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * Returns the username of the currently authenticated user, or empty if anonymous.
     */
    public static Optional<String> getCurrentUsername() {
        return getCurrentAuthentication()
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getName)
                .filter(name -> !"anonymousUser".equals(name));
    }

    /**
     * Returns the authorities granted to the current user.
     */
    public static Collection<String> getCurrentAuthorities() {
        return getCurrentAuthentication()
                .map(Authentication::getAuthorities)
                .orElse(Collections.emptyList())
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

    /**
     * Checks whether the current user holds a specific authority.
     */
    public static boolean hasAuthority(String authority) {
        return getCurrentAuthorities().contains(authority);
    }

    /**
     * Checks whether the current user holds a specific role.
     * Automatically prepends {@code ROLE_} if not already present.
     */
    public static boolean hasRole(String role) {
        String prefixedRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return hasAuthority(prefixedRole);
    }

    /**
     * Returns {@code true} if the current context has an authenticated (non-anonymous) user.
     */
    public static boolean isAuthenticated() {
        return getCurrentUsername().isPresent();
    }
}
