package com.ftgo.security.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Stateless helper methods for querying the current Spring Security context.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * Returns the current {@link Authentication} if present and authenticated.
     */
    public static Optional<Authentication> currentAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Optional.empty();
        }
        return Optional.of(auth);
    }

    /**
     * Returns the principal name of the currently authenticated user, or empty.
     */
    public static Optional<String> currentUsername() {
        return currentAuthentication().map(Authentication::getName);
    }

    /**
     * Returns the authorities granted to the currently authenticated user.
     */
    public static Collection<? extends GrantedAuthority> currentAuthorities() {
        return currentAuthentication()
                .map(Authentication::getAuthorities)
                .orElse(Collections.emptyList());
    }

    /**
     * Checks whether the current user has the given authority/role.
     */
    public static boolean hasAuthority(String authority) {
        return currentAuthorities().stream()
                .anyMatch(ga -> ga.getAuthority().equals(authority));
    }

    /**
     * Checks whether the current user has a role (prefixed with {@code ROLE_}).
     */
    public static boolean hasRole(String role) {
        String prefixed = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return hasAuthority(prefixed);
    }

    /**
     * Returns {@code true} if a user is currently authenticated (not anonymous).
     */
    public static boolean isAuthenticated() {
        return currentAuthentication()
                .map(auth -> auth.getAuthorities().stream()
                        .noneMatch(ga -> "ROLE_ANONYMOUS".equals(ga.getAuthority())))
                .orElse(false);
    }
}
