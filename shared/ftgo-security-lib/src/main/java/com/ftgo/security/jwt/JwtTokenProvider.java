package com.ftgo.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Service for creating and validating JWT tokens.
 *
 * <p>Handles token generation (access and refresh), parsing, claims extraction,
 * and validation. Uses HMAC-SHA256 for signing.
 *
 * <p>Thread-safe after construction.
 */
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    static final String CLAIMS_ROLES = "roles";
    static final String CLAIMS_PERMISSIONS = "permissions";
    static final String CLAIMS_TOKEN_TYPE = "type";
    static final String TOKEN_TYPE_ACCESS = "access";
    static final String TOKEN_TYPE_REFRESH = "refresh";

    private final SecretKey signingKey;
    private final long accessTokenExpirationSeconds;
    private final long refreshTokenExpirationSeconds;
    private final String issuer;

    public JwtTokenProvider(JwtProperties properties) {
        this.signingKey = Keys.hmacShaKeyFor(
            properties.getSecret().getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationSeconds = properties.getExpiration();
        this.refreshTokenExpirationSeconds = properties.getRefreshExpiration();
        this.issuer = properties.getIssuer();
    }

    /**
     * Generates an access token for the given user.
     *
     * @param userId      the user identifier (becomes the {@code sub} claim)
     * @param roles       the user's roles (e.g., {@code ["ROLE_ADMIN"]})
     * @param permissions the user's permissions (e.g., {@code ["order:read"]})
     * @return the signed JWT access token string
     */
    public String generateAccessToken(String userId, Set<String> roles, Set<String> permissions) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(accessTokenExpirationSeconds);

        return Jwts.builder()
            .subject(userId)
            .issuer(issuer)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .claim(CLAIMS_ROLES, roles)
            .claim(CLAIMS_PERMISSIONS, permissions)
            .claim(CLAIMS_TOKEN_TYPE, TOKEN_TYPE_ACCESS)
            .signWith(signingKey)
            .compact();
    }

    /**
     * Generates a refresh token for the given user.
     *
     * <p>Refresh tokens have a longer expiration and contain minimal claims.
     * They should be used to obtain new access tokens without re-authentication.
     *
     * @param userId the user identifier
     * @return the signed JWT refresh token string
     */
    public String generateRefreshToken(String userId) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(refreshTokenExpirationSeconds);

        return Jwts.builder()
            .subject(userId)
            .issuer(issuer)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .claim(CLAIMS_TOKEN_TYPE, TOKEN_TYPE_REFRESH)
            .signWith(signingKey)
            .compact();
    }

    /**
     * Refreshes an access token using a valid refresh token.
     *
     * <p>Validates the refresh token, extracts the user ID, and generates
     * a new access token with the provided roles and permissions.
     *
     * @param refreshToken the refresh token to validate
     * @param roles        the roles to include in the new access token
     * @param permissions  the permissions to include in the new access token
     * @return an Optional containing the new access token, or empty if the refresh token is invalid
     */
    public Optional<String> refreshAccessToken(String refreshToken, Set<String> roles, Set<String> permissions) {
        try {
            Claims claims = parseClaimsFromToken(refreshToken);
            String tokenType = claims.get(CLAIMS_TOKEN_TYPE, String.class);
            if (!TOKEN_TYPE_REFRESH.equals(tokenType)) {
                log.warn("Token refresh attempted with non-refresh token type: {}", tokenType);
                return Optional.empty();
            }
            String userId = claims.getSubject();
            return Optional.of(generateAccessToken(userId, roles, permissions));
        } catch (JwtException e) {
            log.warn("Failed to refresh token: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Validates a token and returns {@code true} if it is structurally valid,
     * correctly signed, and not expired.
     *
     * @param token the JWT token string
     * @return {@code true} if the token is valid
     */
    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        try {
            parseClaimsFromToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("JWT token expired: {}", e.getMessage());
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Extracts the user ID ({@code sub} claim) from a token.
     *
     * @param token the JWT token string
     * @return the user ID
     * @throws JwtException if the token is invalid
     */
    public String getUserId(String token) {
        return parseClaimsFromToken(token).getSubject();
    }

    /**
     * Extracts roles from the token claims.
     *
     * @param token the JWT token string
     * @return set of roles, or empty set if none present
     * @throws JwtException if the token is invalid
     */
    @SuppressWarnings("unchecked")
    public Set<String> getRoles(String token) {
        Claims claims = parseClaimsFromToken(token);
        Object rolesObj = claims.get(CLAIMS_ROLES);
        if (rolesObj instanceof List) {
            return new HashSet<>((List<String>) rolesObj);
        }
        return Collections.emptySet();
    }

    /**
     * Extracts permissions from the token claims.
     *
     * @param token the JWT token string
     * @return set of permissions, or empty set if none present
     * @throws JwtException if the token is invalid
     */
    @SuppressWarnings("unchecked")
    public Set<String> getPermissions(String token) {
        Claims claims = parseClaimsFromToken(token);
        Object permissionsObj = claims.get(CLAIMS_PERMISSIONS);
        if (permissionsObj instanceof List) {
            return new HashSet<>((List<String>) permissionsObj);
        }
        return Collections.emptySet();
    }

    /**
     * Extracts the token type claim ({@code access} or {@code refresh}).
     *
     * @param token the JWT token string
     * @return the token type string
     * @throws JwtException if the token is invalid
     */
    public String getTokenType(String token) {
        return parseClaimsFromToken(token).get(CLAIMS_TOKEN_TYPE, String.class);
    }

    /**
     * Parses and validates a JWT token, returning its claims.
     *
     * @param token the JWT token string
     * @return the parsed claims
     * @throws JwtException if the token is invalid, expired, or tampered
     */
    Claims parseClaimsFromToken(String token) {
        return Jwts.parser()
            .verifyWith(signingKey)
            .requireIssuer(issuer)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
