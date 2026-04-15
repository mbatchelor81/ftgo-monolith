package com.ftgo.security.jwt;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

/**
 * Service for generating JWT access and refresh tokens.
 *
 * <p>Tokens include standard claims ({@code sub}, {@code iss}, {@code iat}, {@code exp})
 * plus FTGO-specific claims ({@code userId}, {@code roles}, {@code permissions}).
 */
public class JwtTokenService {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenService.class);

    private static final Set<String> RESERVED_CLAIMS = Set.of(
            "sub", "iss", "iat", "exp", "nbf", "jti",
            "userId", "roles", "permissions", "type");

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    public JwtTokenService(JwtEncoder jwtEncoder, JwtProperties jwtProperties) {
        this.jwtEncoder = jwtEncoder;
        this.jwtProperties = jwtProperties;
    }

    /**
     * Generates an access token for the given user.
     *
     * @param userId      unique user identifier stored in the {@code userId} claim
     * @param username    the subject ({@code sub}) claim
     * @param roles       roles to embed (stored without {@code ROLE_} prefix)
     * @param permissions fine-grained permissions
     * @return signed JWT access token string
     */
    public String generateAccessToken(String userId,
                                      String username,
                                      Collection<String> roles,
                                      Collection<String> permissions) {
        return generateAccessToken(userId, username, roles, permissions, Map.of());
    }

    /**
     * Generates an access token with additional custom claims.
     *
     * @param userId           unique user identifier
     * @param username         the subject claim
     * @param roles            roles to embed
     * @param permissions      fine-grained permissions
     * @param additionalClaims extra claims merged into the token
     * @return signed JWT access token string
     */
    public String generateAccessToken(String userId,
                                      String username,
                                      Collection<String> roles,
                                      Collection<String> permissions,
                                      Map<String, Object> additionalClaims) {
        Instant now = Instant.now();
        Instant expiry = now.plus(jwtProperties.getAccessTokenExpiration());

        JwtClaimsSet.Builder builder = JwtClaimsSet.builder()
                .id(UUID.randomUUID().toString())
                .issuer(jwtProperties.getIssuer())
                .subject(username)
                .issuedAt(now)
                .expiresAt(expiry)
                .claim("userId", userId)
                .claim("roles", roles)
                .claim("permissions", permissions)
                .claim("type", "access");

        additionalClaims.forEach((key, value) -> {
            if (RESERVED_CLAIMS.contains(key)) {
                log.warn("Ignoring reserved claim '{}' in additionalClaims — "
                        + "standard claims cannot be overridden", key);
            } else {
                builder.claim(key, value);
            }
        });

        return jwtEncoder.encode(JwtEncoderParameters.from(builder.build())).getTokenValue();
    }

    /**
     * Generates a refresh token for the given user.
     *
     * <p>Refresh tokens carry minimal claims — only enough to identify the user
     * and validate the token type during the refresh flow.
     *
     * @param userId   unique user identifier
     * @param username the subject claim
     * @return signed JWT refresh token string
     */
    public String generateRefreshToken(String userId, String username) {
        Instant now = Instant.now();
        Instant expiry = now.plus(jwtProperties.getRefreshTokenExpiration());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .id(UUID.randomUUID().toString())
                .issuer(jwtProperties.getIssuer())
                .subject(username)
                .issuedAt(now)
                .expiresAt(expiry)
                .claim("userId", userId)
                .claim("type", "refresh")
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    /**
     * Returns the configured access token expiration duration in seconds.
     */
    public long getAccessTokenExpirationSeconds() {
        return jwtProperties.getAccessTokenExpiration().getSeconds();
    }

    /**
     * Returns the configured refresh token expiration duration in seconds.
     */
    public long getRefreshTokenExpirationSeconds() {
        return jwtProperties.getRefreshTokenExpiration().getSeconds();
    }
}
