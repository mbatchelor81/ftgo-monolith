package com.ftgo.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Externalized JWT configuration properties.
 *
 * <p>Bind these in {@code application.yml} under the {@code ftgo.security.jwt} prefix:
 *
 * <pre>
 * ftgo:
 *   security:
 *     jwt:
 *       enabled: true
 *       secret: ${FTGO_JWT_SECRET}
 *       expiration: 1800
 *       refresh-expiration: 86400
 *       issuer: ftgo-platform
 * </pre>
 *
 * <p><strong>Important:</strong> The signing secret must be provided via environment
 * variables in production. It must be at least 256 bits (32 bytes) for HMAC-SHA256.
 */
@ConfigurationProperties(prefix = "ftgo.security.jwt")
public class JwtProperties {

    /**
     * Whether JWT authentication is enabled. When {@code false}, the JWT filter
     * is not registered and services fall back to HTTP Basic authentication.
     */
    private boolean enabled = false;

    /**
     * The secret key used for HMAC-SHA256 token signing and verification.
     * Must be at least 32 characters (256 bits). Must be set via environment
     * variable in production (e.g., {@code ${FTGO_JWT_SECRET}}).
     */
    private String secret;

    /**
     * Access token expiration time in seconds. Default: 1800 (30 minutes).
     */
    private long expiration = 1800;

    /**
     * Refresh token expiration time in seconds. Default: 86400 (24 hours).
     */
    private long refreshExpiration = 86400;

    /**
     * The issuer claim ({@code iss}) embedded in generated tokens.
     */
    private String issuer = "ftgo-platform";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

    public long getRefreshExpiration() {
        return refreshExpiration;
    }

    public void setRefreshExpiration(long refreshExpiration) {
        this.refreshExpiration = refreshExpiration;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
}
