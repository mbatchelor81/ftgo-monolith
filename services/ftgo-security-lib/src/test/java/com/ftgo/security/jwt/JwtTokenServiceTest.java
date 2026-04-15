package com.ftgo.security.jwt;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link JwtTokenService}.
 *
 * <p>Verifies token generation, claims content, expiration, and refresh tokens.
 */
@SpringBootTest(classes = JwtTokenServiceTest.TestApp.class)
class JwtTokenServiceTest {

    @org.springframework.boot.autoconfigure.SpringBootApplication
    @Import(JwtConfiguration.class)
    static class TestApp {
    }

    @Autowired
    private JwtTokenService tokenService;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private JwtClaimsExtractor claimsExtractor;

    @Test
    void generateAccessToken_containsExpectedClaims() {
        String token = tokenService.generateAccessToken(
                "user-123", "john.doe",
                List.of("ADMIN", "USER"),
                List.of("order:read", "order:write"));

        Jwt jwt = jwtDecoder.decode(token);

        assertThat(jwt.getSubject()).isEqualTo("john.doe");
        assertThat(jwt.getClaimAsString("userId")).isEqualTo("user-123");
        assertThat(jwt.getClaimAsString("type")).isEqualTo("access");
        assertThat(jwt.getClaimAsString("iss")).isEqualTo("ftgo-platform");
        assertThat(jwt.getClaimAsStringList("roles")).containsExactlyInAnyOrder("ADMIN", "USER");
        assertThat(jwt.getClaimAsStringList("permissions"))
                .containsExactlyInAnyOrder("order:read", "order:write");
        assertThat(jwt.getExpiresAt()).isNotNull();
        assertThat(jwt.getIssuedAt()).isNotNull();
        assertThat(jwt.getId()).isNotBlank();
    }

    @Test
    void generateAccessToken_withAdditionalClaims_mergesClaims() {
        String token = tokenService.generateAccessToken(
                "user-456", "jane.doe",
                List.of("USER"),
                List.of("consumer:read"),
                Map.of("tenantId", "tenant-abc", "serviceId", "order-service"));

        Jwt jwt = jwtDecoder.decode(token);

        assertThat(jwt.getClaimAsString("tenantId")).isEqualTo("tenant-abc");
        assertThat(jwt.getClaimAsString("serviceId")).isEqualTo("order-service");
        assertThat(jwt.getClaimAsString("userId")).isEqualTo("user-456");
    }

    @Test
    void generateRefreshToken_containsMinimalClaims() {
        String token = tokenService.generateRefreshToken("user-789", "admin.user");

        Jwt jwt = jwtDecoder.decode(token);

        assertThat(jwt.getSubject()).isEqualTo("admin.user");
        assertThat(jwt.getClaimAsString("userId")).isEqualTo("user-789");
        assertThat(jwt.getClaimAsString("type")).isEqualTo("refresh");
        assertThat(jwt.getClaimAsString("iss")).isEqualTo("ftgo-platform");
        // Refresh tokens should not contain roles or permissions
        assertThat(jwt.getClaim("roles")).isNull();
        assertThat(jwt.getClaim("permissions")).isNull();
    }

    @Test
    void generateAccessToken_expirationMatchesConfig() {
        String token = tokenService.generateAccessToken(
                "user-1", "test", List.of(), List.of());

        Jwt jwt = jwtDecoder.decode(token);

        Duration tokenDuration = Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt());
        // Default is 30 minutes
        assertThat(tokenDuration).isEqualTo(Duration.ofMinutes(30));
    }

    @Test
    void generateRefreshToken_expirationMatchesConfig() {
        String token = tokenService.generateRefreshToken("user-1", "test");

        Jwt jwt = jwtDecoder.decode(token);

        Duration tokenDuration = Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt());
        // Default is 7 days
        assertThat(tokenDuration).isEqualTo(Duration.ofDays(7));
    }

    @Test
    void claimsExtractor_extractsUserDetails() {
        String token = tokenService.generateAccessToken(
                "user-100", "alice",
                List.of("MANAGER"),
                List.of("restaurant:manage"));

        Jwt jwt = jwtDecoder.decode(token);
        FtgoUserDetails details = claimsExtractor.extractUserDetails(jwt);

        assertThat(details.getUserId()).isEqualTo("user-100");
        assertThat(details.getUsername()).isEqualTo("alice");
        assertThat(details.getRoles()).containsExactly("MANAGER");
        assertThat(details.getPermissions()).containsExactly("restaurant:manage");
    }

    @Test
    void claimsExtractor_extractsTokenType() {
        String accessToken = tokenService.generateAccessToken(
                "u1", "user", List.of(), List.of());
        String refreshToken = tokenService.generateRefreshToken("u1", "user");

        assertThat(claimsExtractor.extractTokenType(jwtDecoder.decode(accessToken)))
                .isEqualTo("access");
        assertThat(claimsExtractor.extractTokenType(jwtDecoder.decode(refreshToken)))
                .isEqualTo("refresh");
    }

    @Test
    void getExpirationSeconds_returnsConfiguredValues() {
        assertThat(tokenService.getAccessTokenExpirationSeconds()).isEqualTo(1800L);  // 30min
        assertThat(tokenService.getRefreshTokenExpirationSeconds()).isEqualTo(604800L);  // 7d
    }

    @Test
    void generatedTokens_haveUniqueIds() {
        String token1 = tokenService.generateAccessToken("u1", "user", List.of(), List.of());
        String token2 = tokenService.generateAccessToken("u1", "user", List.of(), List.of());

        Jwt jwt1 = jwtDecoder.decode(token1);
        Jwt jwt2 = jwtDecoder.decode(token2);

        assertThat(jwt1.getId()).isNotEqualTo(jwt2.getId());
    }
}
