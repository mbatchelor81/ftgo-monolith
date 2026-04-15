package com.ftgo.security.jwt;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Extracts FTGO-specific claims from a validated {@link Jwt} token.
 *
 * <p>Converts the raw JWT into an {@link FtgoUserDetails} that the rest of the application can use
 * for authorization decisions.
 */
public class JwtClaimsExtractor {

    /**
     * Extracts an {@link FtgoUserDetails} from the given JWT.
     *
     * @param jwt a validated JWT token
     * @return user details populated from token claims
     */
    public FtgoUserDetails extractUserDetails(Jwt jwt) {
        String userId = jwt.getClaimAsString("userId");
        String username = jwt.getSubject();
        Set<String> roles = extractStringSet(jwt, "roles");
        Set<String> permissions = extractStringSet(jwt, "permissions");
        Map<String, Object> additionalClaims = extractAdditionalClaims(jwt);

        return new FtgoUserDetails(userId, username, roles, permissions, additionalClaims);
    }

    /** Extracts the token type claim ({@code "access"} or {@code "refresh"}). */
    public String extractTokenType(Jwt jwt) {
        String type = jwt.getClaimAsString("type");
        return type != null ? type : "access";
    }

    private Set<String> extractStringSet(Jwt jwt, String claimName) {
        Object claim = jwt.getClaim(claimName);
        if (claim instanceof List<?> list) {
            return list.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    private Map<String, Object> extractAdditionalClaims(Jwt jwt) {
        Set<String> standardClaims =
                Set.of(
                        "sub",
                        "iss",
                        "iat",
                        "exp",
                        "jti",
                        "nbf",
                        "userId",
                        "roles",
                        "permissions",
                        "type");

        Map<String, Object> additional = new LinkedHashMap<>();
        jwt.getClaims()
                .forEach(
                        (key, value) -> {
                            if (!standardClaims.contains(key)) {
                                additional.put(key, value);
                            }
                        });
        return additional;
    }
}
