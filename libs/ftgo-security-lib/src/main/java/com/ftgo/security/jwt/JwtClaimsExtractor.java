package com.ftgo.security.jwt;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Extracts typed claim values from a JWT token.
 *
 * <p>Supports nested claim paths (e.g. {@code realm_access.roles})
 * and provides type-safe accessors for common FTGO claims.
 */
public class JwtClaimsExtractor {

    private final FtgoJwtProperties properties;

    public JwtClaimsExtractor(FtgoJwtProperties properties) {
        this.properties = properties;
    }

    public String extractUserId(Jwt jwt) {
        return jwt.getClaimAsString(properties.getUserIdClaimName());
    }

    public List<String> extractRoles(Jwt jwt) {
        return extractNestedStringList(jwt, properties.getRolesClaimName());
    }

    public List<String> extractPermissions(Jwt jwt) {
        return extractNestedStringList(jwt, properties.getPermissionsClaimName());
    }

    public Optional<String> extractEmail(Jwt jwt) {
        return Optional.ofNullable(jwt.getClaimAsString("email"));
    }

    public Optional<String> extractPreferredUsername(Jwt jwt) {
        return Optional.ofNullable(jwt.getClaimAsString("preferred_username"));
    }

    @SuppressWarnings("unchecked")
    private List<String> extractNestedStringList(Jwt jwt, String claimPath) {
        String[] parts = claimPath.split("\\.");
        Object current = jwt.getClaims();

        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(part);
            } else {
                return Collections.emptyList();
            }
        }

        if (current instanceof Collection<?>) {
            return ((Collection<?>) current).stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList();
        }
        return Collections.emptyList();
    }
}
