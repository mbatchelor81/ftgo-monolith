package com.ftgo.security.jwt;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtClaimsExtractorTest {

    @Test
    void extractUserIdFromSubClaim() {
        FtgoJwtProperties properties = new FtgoJwtProperties();
        JwtClaimsExtractor extractor = new JwtClaimsExtractor(properties);

        Jwt jwt = buildJwt(Map.of("sub", "user-123"));

        assertThat(extractor.extractUserId(jwt)).isEqualTo("user-123");
    }

    @Test
    void extractRolesFromNestedClaim() {
        FtgoJwtProperties properties = new FtgoJwtProperties();
        JwtClaimsExtractor extractor = new JwtClaimsExtractor(properties);

        Jwt jwt = buildJwt(Map.of(
                "sub", "user-1",
                "realm_access", Map.of("roles", List.of("ftgo-consumer", "ftgo-admin"))
        ));

        assertThat(extractor.extractRoles(jwt))
                .containsExactly("ftgo-consumer", "ftgo-admin");
    }

    @Test
    void extractRolesReturnsEmptyWhenClaimMissing() {
        FtgoJwtProperties properties = new FtgoJwtProperties();
        JwtClaimsExtractor extractor = new JwtClaimsExtractor(properties);

        Jwt jwt = buildJwt(Map.of("sub", "user-1"));

        assertThat(extractor.extractRoles(jwt)).isEmpty();
    }

    @Test
    void extractPermissionsFromFlatClaim() {
        FtgoJwtProperties properties = new FtgoJwtProperties();
        JwtClaimsExtractor extractor = new JwtClaimsExtractor(properties);

        Jwt jwt = buildJwt(Map.of(
                "sub", "user-1",
                "permissions", List.of("order:read", "order:write")
        ));

        assertThat(extractor.extractPermissions(jwt))
                .containsExactly("order:read", "order:write");
    }

    @Test
    void extractEmailReturnsOptional() {
        FtgoJwtProperties properties = new FtgoJwtProperties();
        JwtClaimsExtractor extractor = new JwtClaimsExtractor(properties);

        Jwt jwtWithEmail = buildJwt(Map.of("sub", "user-1", "email", "test@ftgo.com"));
        Jwt jwtWithoutEmail = buildJwt(Map.of("sub", "user-1"));

        assertThat(extractor.extractEmail(jwtWithEmail)).contains("test@ftgo.com");
        assertThat(extractor.extractEmail(jwtWithoutEmail)).isEmpty();
    }

    @Test
    void extractPreferredUsername() {
        FtgoJwtProperties properties = new FtgoJwtProperties();
        JwtClaimsExtractor extractor = new JwtClaimsExtractor(properties);

        Jwt jwt = buildJwt(Map.of("sub", "user-1", "preferred_username", "alice"));

        assertThat(extractor.extractPreferredUsername(jwt)).contains("alice");
    }

    @Test
    void customClaimPathsAreRespected() {
        FtgoJwtProperties properties = new FtgoJwtProperties();
        properties.setRolesClaimName("custom.roles");
        properties.setUserIdClaimName("user_id");
        JwtClaimsExtractor extractor = new JwtClaimsExtractor(properties);

        Jwt jwt = buildJwt(Map.of(
                "user_id", "custom-user-42",
                "custom", Map.of("roles", List.of("ADMIN"))
        ));

        assertThat(extractor.extractUserId(jwt)).isEqualTo("custom-user-42");
        assertThat(extractor.extractRoles(jwt)).containsExactly("ADMIN");
    }

    private Jwt buildJwt(Map<String, Object> claims) {
        Jwt.Builder builder = Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300));
        claims.forEach(builder::claim);
        return builder.build();
    }
}
