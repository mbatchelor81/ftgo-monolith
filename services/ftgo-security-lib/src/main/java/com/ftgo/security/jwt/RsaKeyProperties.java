package com.ftgo.security.jwt;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * RSA key-pair properties for JWT signing and verification.
 *
 * <p>When external key files are provided via {@code ftgo.security.jwt.rsa.public-key}
 * and {@code ftgo.security.jwt.rsa.private-key}, Spring Boot automatically converts
 * {@code classpath:} or {@code file:} resource locations to RSA key objects.
 *
 * <p>If no keys are configured, {@link JwtConfiguration} generates an ephemeral
 * key pair suitable for development and testing.
 */
@ConfigurationProperties(prefix = "ftgo.security.jwt.rsa")
public class RsaKeyProperties {

    /**
     * RSA public key for JWT verification.
     */
    private RSAPublicKey publicKey;

    /**
     * RSA private key for JWT signing.
     */
    private RSAPrivateKey privateKey;

    public RSAPublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(RSAPublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public RSAPrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(RSAPrivateKey privateKey) {
        this.privateKey = privateKey;
    }
}
