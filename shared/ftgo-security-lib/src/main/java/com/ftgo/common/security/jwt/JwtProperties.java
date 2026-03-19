package com.ftgo.common.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for JWT token management.
 *
 * <p>Configure in application.yml:
 * <pre>
 * ftgo:
 *   security:
 *     jwt:
 *       secret: ${JWT_SECRET:my-256-bit-secret-key-for-dev-only!!}
 *       expiration-ms: 3600000
 *       refresh-expiration-ms: 86400000
 *       issuer: ftgo-platform
 * </pre>
 */
@ConfigurationProperties(prefix = "ftgo.security.jwt")
public class JwtProperties {

    /**
     * HMAC-SHA256 secret key for signing tokens.
     * Must be at least 256 bits (32 bytes) for HS256.
     * In production, set via JWT_SECRET environment variable.
     */
    private String secret = "my-256-bit-secret-key-for-dev-only!!";

    /**
     * Access token expiration in milliseconds. Default: 1 hour.
     */
    private long expirationMs = 3_600_000;

    /**
     * Refresh token expiration in milliseconds. Default: 24 hours.
     */
    private long refreshExpirationMs = 86_400_000;

    /**
     * Token issuer claim. Default: ftgo-platform.
     */
    private String issuer = "ftgo-platform";

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }

    public long getExpirationMs() { return expirationMs; }
    public void setExpirationMs(long expirationMs) { this.expirationMs = expirationMs; }

    public long getRefreshExpirationMs() { return refreshExpirationMs; }
    public void setRefreshExpirationMs(long refreshExpirationMs) { this.refreshExpirationMs = refreshExpirationMs; }

    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }
}
