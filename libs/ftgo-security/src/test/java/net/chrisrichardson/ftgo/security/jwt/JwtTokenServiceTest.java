package net.chrisrichardson.ftgo.security.jwt;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenServiceTest {

    private static final String SECRET = "test-secret-key-for-ftgo-security-unit-tests-please-do-not-reuse";
    private static final SecretKeySpec KEY = new SecretKeySpec(
            SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

    private final JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(KEY));
    private final JwtDecoder decoder = NimbusJwtDecoder
            .withSecretKey(KEY).macAlgorithm(MacAlgorithm.HS256).build();
    private final JwtProperties properties = buildProperties();

    @Test
    void issueTokens_returnsAccessAndRefreshPair_withExpectedClaims() {
        JwtTokenService service = new JwtTokenService(encoder, decoder, properties);

        TokenPair tokens = service.issueTokens(
                "user-42",
                "alice",
                List.of("CONSUMER"),
                List.of("order:read", "order:write"));

        assertThat(tokens.accessToken().type()).isEqualTo(JwtTokenType.ACCESS);
        assertThat(tokens.refreshToken().type()).isEqualTo(JwtTokenType.REFRESH);

        Jwt access = decoder.decode(tokens.accessToken().value());
        assertThat(access.getClaimAsString(JwtClaimNames.USER_ID)).isEqualTo("user-42");
        assertThat(access.getClaimAsString(JwtClaimNames.USERNAME)).isEqualTo("alice");
        assertThat(access.getClaimAsString(JwtClaimNames.TOKEN_TYPE)).isEqualTo("access");
        assertThat(access.getClaimAsStringList(JwtClaimNames.ROLES)).containsExactly("CONSUMER");
        assertThat(access.getClaimAsStringList(JwtClaimNames.PERMISSIONS))
                .containsExactly("order:read", "order:write");
        assertThat(access.getClaimAsString("iss")).isEqualTo("ftgo-auth");
        assertThat(access.getAudience()).containsExactly("ftgo-services");
        assertThat(access.getSubject()).isEqualTo("user-42");

        Jwt refresh = decoder.decode(tokens.refreshToken().value());
        assertThat(refresh.getClaimAsString(JwtClaimNames.TOKEN_TYPE)).isEqualTo("refresh");
        assertThat(refresh.getClaimAsStringList(JwtClaimNames.ROLES)).isNull();
        assertThat(refresh.getClaimAsStringList(JwtClaimNames.PERMISSIONS)).isNull();
    }

    @Test
    void issueTokens_usesConfiguredTtls() {
        Instant fixedNow = Instant.parse("2025-01-01T00:00:00Z");
        Clock fixedClock = Clock.fixed(fixedNow, ZoneOffset.UTC);
        JwtTokenService service = new JwtTokenService(encoder, decoder, properties, fixedClock);

        TokenPair tokens = service.issueTokens("u", "u", Set.of(), Set.of());

        assertThat(tokens.accessToken().issuedAt()).isEqualTo(fixedNow);
        assertThat(tokens.accessToken().expiresAt())
                .isEqualTo(fixedNow.plus(properties.getAccessTokenTtl()));
        assertThat(tokens.refreshToken().expiresAt())
                .isEqualTo(fixedNow.plus(properties.getRefreshTokenTtl()));
    }

    @Test
    void refresh_withValidRefreshToken_issuesFreshTokenPair() {
        JwtTokenService service = new JwtTokenService(encoder, decoder, properties);
        TokenPair original = service.issueTokens("user-42", "alice", List.of("CONSUMER"), List.of("order:read"));

        TokenPair refreshed = service.refresh(original.refreshToken().value(),
                List.of("CONSUMER"), List.of("order:read", "order:write"));

        Jwt newAccess = decoder.decode(refreshed.accessToken().value());
        assertThat(newAccess.getClaimAsString(JwtClaimNames.USER_ID)).isEqualTo("user-42");
        assertThat(newAccess.getClaimAsStringList(JwtClaimNames.PERMISSIONS))
                .containsExactly("order:read", "order:write");
        assertThat(refreshed.accessToken().value()).isNotEqualTo(original.accessToken().value());
    }

    @Test
    void refresh_withAccessTokenValue_throwsIllegalArgument() {
        JwtTokenService service = new JwtTokenService(encoder, decoder, properties);
        TokenPair tokens = service.issueTokens("user-42", "alice", List.of(), List.of());

        assertThatThrownBy(() -> service.refresh(tokens.accessToken().value(), List.of(), List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not a refresh token");
    }

    @Test
    void refresh_withTamperedToken_throwsJwtException() {
        JwtTokenService service = new JwtTokenService(encoder, decoder, properties);
        TokenPair tokens = service.issueTokens("user-42", "alice", List.of(), List.of());

        String tampered = tokens.refreshToken().value().substring(0,
                tokens.refreshToken().value().length() - 2) + "xx";

        assertThatThrownBy(() -> service.refresh(tampered, List.of(), List.of()))
                .isInstanceOf(JwtException.class);
    }

    private static JwtProperties buildProperties() {
        JwtProperties p = new JwtProperties();
        p.setSecret(SECRET);
        p.setIssuer("ftgo-auth");
        p.setAudience("ftgo-services");
        p.setAccessTokenTtl(Duration.ofMinutes(5));
        p.setRefreshTokenTtl(Duration.ofMinutes(30));
        return p;
    }
}
