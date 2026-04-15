package com.ftgo.security.jwt;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for JWT token management.
 *
 * <p>Bind to {@code ftgo.security.jwt.*} in application YAML/properties.
 *
 * <pre>
 * ftgo:
 *   security:
 *     jwt:
 *       issuer: ftgo-platform
 *       access-token-expiration: 30m
 *       refresh-token-expiration: 7d
 * </pre>
 */
@ConfigurationProperties(prefix = "ftgo.security.jwt")
public class JwtProperties {

    /**
     * Token issuer claim ({@code iss}).
     */
    private String issuer = "ftgo-platform";

    /**
     * Access token time-to-live. Defaults to 30 minutes.
     */
    private Duration accessTokenExpiration = Duration.ofMinutes(30);

    /**
     * Refresh token time-to-live. Defaults to 7 days.
     */
    private Duration refreshTokenExpiration = Duration.ofDays(7);

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public Duration getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public void setAccessTokenExpiration(Duration accessTokenExpiration) {
        this.accessTokenExpiration = accessTokenExpiration;
    }

    public Duration getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    public void setRefreshTokenExpiration(Duration refreshTokenExpiration) {
        this.refreshTokenExpiration = refreshTokenExpiration;
    }
}
