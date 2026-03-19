package com.ftgo.common.security.jwt;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

/**
 * Provides JWT token generation and validation using Spring Security's
 * OAuth2 Resource Server JWT support.
 *
 * <p>Supports:
 * <ul>
 *   <li>Access token generation with configurable expiration</li>
 *   <li>Refresh token generation with longer expiration</li>
 *   <li>Token validation and claims extraction</li>
 *   <li>HMAC-SHA256 signing</li>
 * </ul>
 */
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final JwtProperties properties;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    public JwtTokenProvider(JwtProperties properties) {
        this.properties = properties;
        SecretKey secretKey = new SecretKeySpec(
                properties.getSecret().getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
        this.jwtEncoder = new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
        this.jwtDecoder = NimbusJwtDecoder.withSecretKey(secretKey).build();
    }

    /**
     * Generates an access token for the given username and roles.
     */
    public String generateAccessToken(String username, List<String> roles) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(properties.getExpirationMs());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.getIssuer())
                .subject(username)
                .issuedAt(now)
                .expiresAt(expiry)
                .claim("roles", roles)
                .claim("type", "access")
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    /**
     * Generates a refresh token for the given username.
     */
    public String generateRefreshToken(String username) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(properties.getRefreshExpirationMs());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.getIssuer())
                .subject(username)
                .issuedAt(now)
                .expiresAt(expiry)
                .claim("type", "refresh")
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    /**
     * Validates a token and returns the decoded JWT, or null if invalid.
     */
    public Jwt validateToken(String token) {
        try {
            return jwtDecoder.decode(token);
        } catch (JwtException e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extracts the username (subject) from a validated token.
     */
    public String getUsername(Jwt jwt) {
        return jwt.getSubject();
    }

    /**
     * Extracts roles from a validated token.
     */
    @SuppressWarnings("unchecked")
    public List<String> getRoles(Jwt jwt) {
        Object roles = jwt.getClaim("roles");
        if (roles instanceof List<?>) {
            return (List<String>) roles;
        }
        return List.of();
    }

    /**
     * Returns the token type (access or refresh).
     */
    public String getTokenType(Jwt jwt) {
        return jwt.getClaimAsString("type");
    }

    /**
     * Returns the configured JwtDecoder for use by Spring Security's
     * OAuth2 Resource Server auto-configuration.
     */
    public JwtDecoder getJwtDecoder() {
        return jwtDecoder;
    }
}
