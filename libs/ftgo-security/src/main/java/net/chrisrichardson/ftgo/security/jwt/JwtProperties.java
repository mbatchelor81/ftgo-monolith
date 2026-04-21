package net.chrisrichardson.ftgo.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Configuration properties for FTGO JWT authentication.
 *
 * <p>Bound under prefix {@code ftgo.security.jwt}. A deployment minimally
 * has to set {@code ftgo.security.jwt.secret} (HMAC-SHA256 key, at least
 * 32 bytes); every other property has a sensible default.
 *
 * <pre>
 * ftgo:
 *   security:
 *     jwt:
 *       secret: ${FTGO_JWT_SECRET}
 *       issuer: ftgo-auth
 *       audience: ftgo-services
 *       access-token-ttl: PT15M
 *       refresh-token-ttl: P7D
 *       clock-skew: PT30S
 * </pre>
 */
@ConfigurationProperties(prefix = "ftgo.security.jwt")
public class JwtProperties {

    /**
     * Shared HMAC secret used to sign and verify tokens. Must be at least
     * 32 bytes (256 bits) to satisfy JWA's HS256 requirements.
     */
    private String secret;

    /** Value written into the {@code iss} claim and enforced on validation. */
    private String issuer = "ftgo-auth";

    /** Value written into the {@code aud} claim and enforced on validation. */
    private String audience = "ftgo-services";

    /** Lifetime of an access token from issue to expiry. */
    private Duration accessTokenTtl = Duration.ofMinutes(15);

    /** Lifetime of a refresh token from issue to expiry. */
    private Duration refreshTokenTtl = Duration.ofDays(7);

    /** Maximum acceptable clock skew between issuer and validator. */
    private Duration clockSkew = Duration.ofSeconds(30);

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public Duration getAccessTokenTtl() {
        return accessTokenTtl;
    }

    public void setAccessTokenTtl(Duration accessTokenTtl) {
        this.accessTokenTtl = accessTokenTtl;
    }

    public Duration getRefreshTokenTtl() {
        return refreshTokenTtl;
    }

    public void setRefreshTokenTtl(Duration refreshTokenTtl) {
        this.refreshTokenTtl = refreshTokenTtl;
    }

    public Duration getClockSkew() {
        return clockSkew;
    }

    public void setClockSkew(Duration clockSkew) {
        this.clockSkew = clockSkew;
    }
}
