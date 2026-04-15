package com.ftgo.security.jwt;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/**
 * Central JWT configuration that provides encoder, decoder, token service, claims extractor, and
 * authentication converter beans.
 *
 * <p>RSA keys are resolved in order of precedence:
 *
 * <ol>
 *   <li>Externally configured via {@link RsaKeyProperties}
 *   <li>Auto-generated ephemeral key pair (development/testing only)
 * </ol>
 */
@Configuration
@EnableConfigurationProperties({JwtProperties.class, RsaKeyProperties.class})
public class JwtConfiguration {

    private static final Logger log = LoggerFactory.getLogger(JwtConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public RSAPublicKey jwtRsaPublicKey(RsaKeyProperties rsaKeyProperties) {
        validateKeyPairConfig(rsaKeyProperties);
        if (rsaKeyProperties.getPublicKey() != null) {
            return rsaKeyProperties.getPublicKey();
        }
        log.warn(
                "No RSA keys configured — generating ephemeral key pair. "
                        + "Configure ftgo.security.jwt.rsa.public-key and private-key for production use.");
        return generateKeyPair().rsaPublicKey;
    }

    @Bean
    @ConditionalOnMissingBean
    public RSAPrivateKey jwtRsaPrivateKey(RsaKeyProperties rsaKeyProperties) {
        validateKeyPairConfig(rsaKeyProperties);
        if (rsaKeyProperties.getPrivateKey() != null) {
            return rsaKeyProperties.getPrivateKey();
        }
        return generateKeyPair().rsaPrivateKey;
    }

    private void validateKeyPairConfig(RsaKeyProperties rsaKeyProperties) {
        boolean hasPublic = rsaKeyProperties.getPublicKey() != null;
        boolean hasPrivate = rsaKeyProperties.getPrivateKey() != null;
        if (hasPublic != hasPrivate) {
            throw new IllegalStateException(
                    "Both ftgo.security.jwt.rsa.public-key and ftgo.security.jwt.rsa.private-key "
                            + "must be configured together. Found only "
                            + (hasPublic ? "public" : "private")
                            + " key.");
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtDecoder jwtDecoder(RSAPublicKey rsaPublicKey) {
        return NimbusJwtDecoder.withPublicKey(rsaPublicKey).build();
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtEncoder jwtEncoder(RSAPublicKey rsaPublicKey, RSAPrivateKey rsaPrivateKey) {
        RSAKey rsaKey = new RSAKey.Builder(rsaPublicKey).privateKey(rsaPrivateKey).build();
        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(rsaKey));
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtTokenService jwtTokenService(JwtEncoder jwtEncoder, JwtProperties jwtProperties) {
        return new JwtTokenService(jwtEncoder, jwtProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtClaimsExtractor jwtClaimsExtractor() {
        return new JwtClaimsExtractor();
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtAuthenticationConverter jwtAuthenticationConverter(
            JwtClaimsExtractor claimsExtractor) {
        return new JwtAuthenticationConverter(claimsExtractor);
    }

    // ---------------------------------------------------------------
    // Ephemeral key-pair generation (development / testing fallback)
    // ---------------------------------------------------------------

    private volatile RsaKeyHolder cachedKeyPair;

    private RsaKeyHolder generateKeyPair() {
        if (cachedKeyPair == null) {
            synchronized (this) {
                if (cachedKeyPair == null) {
                    try {
                        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
                        generator.initialize(2048);
                        KeyPair keyPair = generator.generateKeyPair();
                        cachedKeyPair =
                                new RsaKeyHolder(
                                        (RSAPublicKey) keyPair.getPublic(),
                                        (RSAPrivateKey) keyPair.getPrivate());
                    } catch (Exception e) {
                        throw new IllegalStateException("Failed to generate RSA key pair", e);
                    }
                }
            }
        }
        return cachedKeyPair;
    }

    private static class RsaKeyHolder {
        final RSAPublicKey rsaPublicKey;
        final RSAPrivateKey rsaPrivateKey;

        RsaKeyHolder(RSAPublicKey rsaPublicKey, RSAPrivateKey rsaPrivateKey) {
            this.rsaPublicKey = rsaPublicKey;
            this.rsaPrivateKey = rsaPrivateKey;
        }
    }
}
