package com.ftgo.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Utility class for accessing the current security context.
 */
public final class SecurityUtils {

    private SecurityUtils() {
        // Utility class — no instantiation
    }

    /**
     * Returns the current authenticated username, if available.
     */
    public static Optional<String> getCurrentUsername() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getName)
                .filter(name -> !"anonymousUser".equals(name));
    }

    /**
     * Returns true if the current request is authenticated (not anonymous).
     */
    public static boolean isAuthenticated() {
        return getCurrentUsername().isPresent();
    }
}
