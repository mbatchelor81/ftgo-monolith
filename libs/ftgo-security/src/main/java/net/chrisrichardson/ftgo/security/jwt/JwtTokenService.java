package net.chrisrichardson.ftgo.security.jwt;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Mints and refreshes the FTGO access / refresh token pair.
 *
 * <p>The service does <em>not</em> authenticate users — it trusts the caller
 * (e.g. a login controller in a future auth service) to have already verified
 * the user's credentials. Responsibilities are narrow on purpose:
 * <ul>
 *   <li>{@link #issueTokens} — mint a fresh access + refresh pair for a user.</li>
 *   <li>{@link #refresh} — exchange a still-valid refresh token for a new pair
 *       using the identity claims inside the refresh token.</li>
 * </ul>
 *
 * <p>Token validation itself is delegated to Spring Security's OAuth2
 * Resource Server via the {@link JwtDecoder} bean — services never call
 * this class to validate inbound requests.
 */
public class JwtTokenService {

    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    private final JwtProperties properties;
    private final Clock clock;

    public JwtTokenService(JwtEncoder encoder, JwtDecoder decoder, JwtProperties properties) {
        this(encoder, decoder, properties, Clock.systemUTC());
    }

    public JwtTokenService(JwtEncoder encoder, JwtDecoder decoder, JwtProperties properties, Clock clock) {
        this.encoder = Objects.requireNonNull(encoder, "encoder");
        this.decoder = Objects.requireNonNull(decoder, "decoder");
        this.properties = Objects.requireNonNull(properties, "properties");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /**
     * Mint a new access + refresh token pair for the given user.
     *
     * @param userId      stable identifier written into both tokens' {@code userId} claim
     * @param username    human-readable username written into the {@code username} claim
     * @param roles       role names written into the access token (refresh token omits them)
     * @param permissions permission names written into the access token
     */
    public TokenPair issueTokens(String userId,
                                 String username,
                                 Collection<String> roles,
                                 Collection<String> permissions) {
        Instant now = clock.instant();

        IssuedToken accessToken = encodeAccessToken(userId, username, roles, permissions, now);
        IssuedToken refreshToken = encodeRefreshToken(userId, username, now);

        return new TokenPair(accessToken, refreshToken);
    }

    /**
     * Exchange a still-valid refresh token for a new access + refresh pair.
     *
     * <p>The caller is expected to apply any additional policy (e.g. checking
     * that the refresh token has not been revoked server-side) before
     * invoking this method.
     *
     * @throws JwtException              if the token fails signature, issuer,
     *                                   audience, or expiry validation
     * @throws IllegalArgumentException  if the token is not of type
     *                                   {@link JwtTokenType#REFRESH}
     */
    public TokenPair refresh(String refreshToken, Collection<String> roles, Collection<String> permissions) {
        Jwt decoded = decoder.decode(refreshToken);

        String tokenType = decoded.getClaimAsString(JwtClaimNames.TOKEN_TYPE);
        if (!JwtTokenType.REFRESH.claimValue().equals(tokenType)) {
            throw new IllegalArgumentException("Provided token is not a refresh token");
        }

        String userId = decoded.getClaimAsString(JwtClaimNames.USER_ID);
        String username = decoded.getClaimAsString(JwtClaimNames.USERNAME);
        return issueTokens(userId, username, roles, permissions);
    }

    private IssuedToken encodeAccessToken(String userId,
                                          String username,
                                          Collection<String> roles,
                                          Collection<String> permissions,
                                          Instant now) {
        Duration ttl = properties.getAccessTokenTtl();
        Instant expiresAt = now.plus(ttl);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.getIssuer())
                .audience(List.of(properties.getAudience()))
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(userId)
                .id(UUID.randomUUID().toString())
                .claim(JwtClaimNames.TOKEN_TYPE, JwtTokenType.ACCESS.claimValue())
                .claim(JwtClaimNames.USER_ID, userId)
                .claim(JwtClaimNames.USERNAME, username)
                .claim(JwtClaimNames.ROLES, copyOrEmpty(roles))
                .claim(JwtClaimNames.PERMISSIONS, copyOrEmpty(permissions))
                .build();

        return encode(claims, JwtTokenType.ACCESS, now, expiresAt);
    }

    private IssuedToken encodeRefreshToken(String userId, String username, Instant now) {
        Duration ttl = properties.getRefreshTokenTtl();
        Instant expiresAt = now.plus(ttl);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.getIssuer())
                .audience(List.of(properties.getAudience()))
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(userId)
                .id(UUID.randomUUID().toString())
                .claim(JwtClaimNames.TOKEN_TYPE, JwtTokenType.REFRESH.claimValue())
                .claim(JwtClaimNames.USER_ID, userId)
                .claim(JwtClaimNames.USERNAME, username)
                .build();

        return encode(claims, JwtTokenType.REFRESH, now, expiresAt);
    }

    private IssuedToken encode(JwtClaimsSet claims, JwtTokenType type, Instant issuedAt, Instant expiresAt) {
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String value = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new IssuedToken(value, type, issuedAt, expiresAt);
    }

    private static List<String> copyOrEmpty(Collection<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        Set<String> unique = new LinkedHashSet<>(values);
        return List.copyOf(unique);
    }
}
