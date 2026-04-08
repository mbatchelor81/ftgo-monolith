package com.ftgo.security.jwt;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;

/**
 * Immutable user details extracted from a JWT token.
 *
 * <p>Available in the service layer via
 * {@link com.ftgo.security.util.SecurityContextUtils#getCurrentUserDetails()}.
 *
 * <p>Contains the user identifier, granted roles, and fine-grained permissions
 * that were encoded as JWT claims during token issuance.
 */
public class FtgoUserDetails {

    private final String userId;
    private final Set<String> roles;
    private final Set<String> permissions;
    private final Collection<? extends GrantedAuthority> authorities;

    public FtgoUserDetails(String userId,
                           Set<String> roles,
                           Set<String> permissions,
                           Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.roles = roles != null ? Collections.unmodifiableSet(roles) : Collections.emptySet();
        this.permissions = permissions != null ? Collections.unmodifiableSet(permissions) : Collections.emptySet();
        this.authorities = authorities != null ? List.copyOf(authorities) : Collections.emptyList();
    }

    /**
     * Returns the user identifier from the JWT {@code sub} claim.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Returns the user's roles (e.g., {@code ROLE_ADMIN}, {@code ROLE_USER}).
     */
    public Set<String> getRoles() {
        return roles;
    }

    /**
     * Returns the user's fine-grained permissions
     * (e.g., {@code order:read}, {@code order:write}).
     */
    public Set<String> getPermissions() {
        return permissions;
    }

    /**
     * Returns the Spring Security granted authorities derived from roles and permissions.
     */
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * Returns {@code true} if the user has the specified role.
     *
     * @param role the role to check (e.g., "ROLE_ADMIN")
     */
    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    /**
     * Returns {@code true} if the user has the specified permission.
     *
     * @param permission the permission to check (e.g., "order:write")
     */
    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    @Override
    public String toString() {
        return "FtgoUserDetails{userId='" + userId + "', roles=" + roles + ", permissions=" + permissions + '}';
    }
}
