package com.ftgo.security.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link JwtTokenProvider}.
 */
class JwtTokenProviderTest {

    private static final String SECRET = "this-is-a-very-secure-secret-key-for-testing-purposes-only-at-least-32-chars";
    private static final String ISSUER = "ftgo-test";
    private static final long ACCESS_EXPIRATION = 1800;
    private static final long REFRESH_EXPIRATION = 86400;

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(SECRET);
        properties.setIssuer(ISSUER);
        properties.setExpiration(ACCESS_EXPIRATION);
        properties.setRefreshExpiration(REFRESH_EXPIRATION);
        tokenProvider = new JwtTokenProvider(properties);
    }

    @Nested
    @DisplayName("Access Token Generation")
    class AccessTokenGeneration {

        @Test
        @DisplayName("generates a valid access token with all claims")
        void generateAccessToken_withClaims_producesValidToken() {
            Set<String> roles = Set.of("ROLE_ADMIN", "ROLE_USER");
            Set<String> permissions = Set.of("order:read", "order:write");

            String token = tokenProvider.generateAccessToken("user-123", roles, permissions);

            assertThat(token).isNotBlank();
            assertThat(tokenProvider.validateToken(token)).isTrue();
            assertThat(tokenProvider.getUserId(token)).isEqualTo("user-123");
            assertThat(tokenProvider.getRoles(token)).containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER");
            assertThat(tokenProvider.getPermissions(token)).containsExactlyInAnyOrder("order:read", "order:write");
            assertThat(tokenProvider.getTokenType(token)).isEqualTo(JwtTokenProvider.TOKEN_TYPE_ACCESS);
        }

        @Test
        @DisplayName("generates token with empty roles and permissions")
        void generateAccessToken_emptyRolesAndPermissions_producesValidToken() {
            String token = tokenProvider.generateAccessToken("user-456", Set.of(), Set.of());

            assertThat(tokenProvider.validateToken(token)).isTrue();
            assertThat(tokenProvider.getUserId(token)).isEqualTo("user-456");
            assertThat(tokenProvider.getRoles(token)).isEmpty();
            assertThat(tokenProvider.getPermissions(token)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Refresh Token Generation")
    class RefreshTokenGeneration {

        @Test
        @DisplayName("generates a valid refresh token")
        void generateRefreshToken_producesValidToken() {
            String token = tokenProvider.generateRefreshToken("user-789");

            assertThat(token).isNotBlank();
            assertThat(tokenProvider.validateToken(token)).isTrue();
            assertThat(tokenProvider.getUserId(token)).isEqualTo("user-789");
            assertThat(tokenProvider.getTokenType(token)).isEqualTo(JwtTokenProvider.TOKEN_TYPE_REFRESH);
        }

        @Test
        @DisplayName("refresh token does not contain roles or permissions")
        void generateRefreshToken_noRolesOrPermissions() {
            String token = tokenProvider.generateRefreshToken("user-789");

            assertThat(tokenProvider.getRoles(token)).isEmpty();
            assertThat(tokenProvider.getPermissions(token)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Token Validation")
    class TokenValidation {

        @Test
        @DisplayName("valid token passes validation")
        void validateToken_validToken_returnsTrue() {
            String token = tokenProvider.generateAccessToken("user-1", Set.of(), Set.of());

            assertThat(tokenProvider.validateToken(token)).isTrue();
        }

        @Test
        @DisplayName("tampered token fails validation")
        void validateToken_tamperedToken_returnsFalse() {
            String token = tokenProvider.generateAccessToken("user-1", Set.of(), Set.of());
            String tampered = token + "tampered";

            assertThat(tokenProvider.validateToken(tampered)).isFalse();
        }

        @Test
        @DisplayName("random string fails validation")
        void validateToken_randomString_returnsFalse() {
            assertThat(tokenProvider.validateToken("not.a.jwt.token")).isFalse();
        }

        @Test
        @DisplayName("empty string fails validation")
        void validateToken_emptyString_returnsFalse() {
            assertThat(tokenProvider.validateToken("")).isFalse();
        }

        @Test
        @DisplayName("expired token fails validation")
        void validateToken_expiredToken_returnsFalse() {
            JwtProperties expiredProps = new JwtProperties();
            expiredProps.setSecret(SECRET);
            expiredProps.setIssuer(ISSUER);
            expiredProps.setExpiration(0); // expires immediately
            expiredProps.setRefreshExpiration(0);
            JwtTokenProvider expiredProvider = new JwtTokenProvider(expiredProps);

            String token = expiredProvider.generateAccessToken("user-1", Set.of(), Set.of());

            assertThat(tokenProvider.validateToken(token)).isFalse();
        }

        @Test
        @DisplayName("token signed with different key fails validation")
        void validateToken_differentKey_returnsFalse() {
            JwtProperties otherProps = new JwtProperties();
            otherProps.setSecret("a-completely-different-secret-key-that-is-long-enough");
            otherProps.setIssuer(ISSUER);
            otherProps.setExpiration(ACCESS_EXPIRATION);
            otherProps.setRefreshExpiration(REFRESH_EXPIRATION);
            JwtTokenProvider otherProvider = new JwtTokenProvider(otherProps);

            String token = otherProvider.generateAccessToken("user-1", Set.of(), Set.of());

            assertThat(tokenProvider.validateToken(token)).isFalse();
        }
    }

    @Nested
    @DisplayName("Token Refresh")
    class TokenRefresh {

        @Test
        @DisplayName("valid refresh token produces new access token")
        void refreshAccessToken_validRefreshToken_returnsNewAccessToken() {
            String refreshToken = tokenProvider.generateRefreshToken("user-100");
            Set<String> roles = Set.of("ROLE_USER");
            Set<String> permissions = Set.of("order:read");

            Optional<String> newAccessToken = tokenProvider.refreshAccessToken(refreshToken, roles, permissions);

            assertThat(newAccessToken).isPresent();
            assertThat(tokenProvider.validateToken(newAccessToken.get())).isTrue();
            assertThat(tokenProvider.getUserId(newAccessToken.get())).isEqualTo("user-100");
            assertThat(tokenProvider.getRoles(newAccessToken.get())).containsExactly("ROLE_USER");
            assertThat(tokenProvider.getTokenType(newAccessToken.get())).isEqualTo(JwtTokenProvider.TOKEN_TYPE_ACCESS);
        }

        @Test
        @DisplayName("access token cannot be used as refresh token")
        void refreshAccessToken_withAccessToken_returnsEmpty() {
            String accessToken = tokenProvider.generateAccessToken("user-100", Set.of(), Set.of());

            Optional<String> result = tokenProvider.refreshAccessToken(accessToken, Set.of(), Set.of());

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("invalid token returns empty")
        void refreshAccessToken_invalidToken_returnsEmpty() {
            Optional<String> result = tokenProvider.refreshAccessToken("invalid.token", Set.of(), Set.of());

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Claims Extraction")
    class ClaimsExtraction {

        @Test
        @DisplayName("extracts userId from token")
        void getUserId_validToken_returnsUserId() {
            String token = tokenProvider.generateAccessToken("user-abc", Set.of(), Set.of());

            assertThat(tokenProvider.getUserId(token)).isEqualTo("user-abc");
        }

        @Test
        @DisplayName("extracts roles from token")
        void getRoles_validToken_returnsRoles() {
            Set<String> roles = Set.of("ROLE_ADMIN", "ROLE_MANAGER");
            String token = tokenProvider.generateAccessToken("user-1", roles, Set.of());

            assertThat(tokenProvider.getRoles(token)).containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_MANAGER");
        }

        @Test
        @DisplayName("extracts permissions from token")
        void getPermissions_validToken_returnsPermissions() {
            Set<String> permissions = Set.of("order:read", "consumer:write");
            String token = tokenProvider.generateAccessToken("user-1", Set.of(), permissions);

            assertThat(tokenProvider.getPermissions(token)).containsExactlyInAnyOrder("order:read", "consumer:write");
        }

        @Test
        @DisplayName("invalid token throws JwtException on claims extraction")
        void getUserId_invalidToken_throwsException() {
            assertThatThrownBy(() -> tokenProvider.getUserId("invalid.token"))
                .isInstanceOf(JwtException.class);
        }
    }
}
