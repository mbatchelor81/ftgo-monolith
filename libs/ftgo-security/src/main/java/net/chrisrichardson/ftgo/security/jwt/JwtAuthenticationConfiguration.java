package net.chrisrichardson.ftgo.security.jwt;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.springframework.security.oauth2.jwt.JwtClaimNames.AUD;
import static org.springframework.security.oauth2.jwt.JwtClaimNames.ISS;

/**
 * Wires up the JWT encoder, decoder, and authentication converter used by
 * every FTGO microservice. Activated when {@code ftgo.security.jwt.secret}
 * is set; services deployed without a secret (e.g. during early migration
 * phases) fall back to the HTTP Basic configuration in
 * {@link net.chrisrichardson.ftgo.security.BaseSecurityConfiguration}.
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
@ConditionalOnProperty(prefix = "ftgo.security.jwt", name = "secret")
public class JwtAuthenticationConfiguration {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    @Bean
    @ConditionalOnMissingBean
    public JwtEncoder jwtEncoder(JwtProperties properties) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(buildSecretKey(properties)));
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtDecoder jwtDecoder(JwtProperties properties) {
        NimbusJwtDecoder decoder = buildRawDecoder(properties);
        // Resource-server validation: signature (HS256), expiry, issuer,
        // audience, and access-token-only. Refresh tokens fail here and
        // surface as 401 at the resource-server boundary.
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                timestampValidator(properties),
                issuerValidator(properties),
                audienceValidator(properties),
                accessTokenOnlyValidator()
        ));
        return decoder;
    }

    @Bean
    @ConditionalOnMissingBean
    public FtgoJwtAuthenticationConverter ftgoJwtAuthenticationConverter() {
        return new FtgoJwtAuthenticationConverter();
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtTokenService jwtTokenService(JwtEncoder encoder, JwtProperties properties) {
        // JwtTokenService decodes refresh tokens in refresh(); it therefore
        // needs a decoder that validates signature/expiry/issuer/audience but
        // accepts both access and refresh token_type values.
        NimbusJwtDecoder refreshAwareDecoder = buildRawDecoder(properties);
        refreshAwareDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                timestampValidator(properties),
                issuerValidator(properties),
                audienceValidator(properties)
        ));
        return new JwtTokenService(encoder, refreshAwareDecoder, properties);
    }

    private static NimbusJwtDecoder buildRawDecoder(JwtProperties properties) {
        return NimbusJwtDecoder
                .withSecretKey(buildSecretKey(properties))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    private static SecretKeySpec buildSecretKey(JwtProperties properties) {
        String secret = properties.getSecret();
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("ftgo.security.jwt.secret must be configured");
        }
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "ftgo.security.jwt.secret must be at least 32 bytes to satisfy HS256");
        }
        return new SecretKeySpec(keyBytes, HMAC_ALGORITHM);
    }

    private static OAuth2TokenValidator<Jwt> timestampValidator(JwtProperties properties) {
        return new JwtTimestampValidator(properties.getClockSkew());
    }

    private static OAuth2TokenValidator<Jwt> issuerValidator(JwtProperties properties) {
        return new JwtClaimValidator<String>(ISS, properties.getIssuer()::equals);
    }

    private static OAuth2TokenValidator<Jwt> audienceValidator(JwtProperties properties) {
        return new JwtClaimValidator<List<String>>(
                AUD, aud -> aud != null && aud.contains(properties.getAudience()));
    }

    /**
     * Refresh tokens are rejected at the resource-server boundary so only
     * access tokens can authenticate API calls. The refresh endpoint path
     * inside {@link JwtTokenService#refresh(String, java.util.Collection, java.util.Collection)}
     * uses a decoder that does <em>not</em> apply this validator.
     */
    private static OAuth2TokenValidator<Jwt> accessTokenOnlyValidator() {
        return new JwtClaimValidator<String>(
                JwtClaimNames.TOKEN_TYPE,
                typ -> typ == null || JwtTokenType.ACCESS.claimValue().equals(typ));
    }
}
