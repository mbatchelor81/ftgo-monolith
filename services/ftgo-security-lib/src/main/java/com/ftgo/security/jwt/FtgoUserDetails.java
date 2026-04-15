package com.ftgo.security.jwt;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Immutable representation of an authenticated FTGO user extracted from a JWT.
 *
 * <p>Available in the service layer via {@link
 * com.ftgo.security.util.SecurityUtils#getCurrentUserDetails()}.
 */
public class FtgoUserDetails {

    private final String userId;
    private final String username;
    private final Set<String> roles;
    private final Set<String> permissions;
    private final Map<String, Object> claims;

    public FtgoUserDetails(
            String userId,
            String username,
            Set<String> roles,
            Set<String> permissions,
            Map<String, Object> claims) {
        this.userId = userId;
        this.username = username;
        this.roles = roles != null ? Set.copyOf(roles) : Collections.emptySet();
        this.permissions = permissions != null ? Set.copyOf(permissions) : Collections.emptySet();
        this.claims = claims != null ? Map.copyOf(claims) : Collections.emptyMap();
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public Map<String, Object> getClaims() {
        return claims;
    }

    /**
     * Returns all granted authorities — both {@code ROLE_*} prefixed roles and raw permission
     * strings.
     */
    public Collection<String> getAuthorities() {
        var authorities = new java.util.ArrayList<String>();
        roles.forEach(r -> authorities.add(r.startsWith("ROLE_") ? r : "ROLE_" + r));
        authorities.addAll(permissions);
        return Collections.unmodifiableList(authorities);
    }
}
