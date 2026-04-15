package com.ftgo.apigateway.config;

import org.springframework.context.annotation.Configuration;

/**
 * SSL/TLS termination marker configuration for the FTGO API Gateway.
 *
 * <p>In a Kubernetes environment, TLS termination is handled at the Ingress or load-balancer level.
 * This configuration activates the embedded TLS when the {@code ssl} Spring profile is active, by
 * relying on the standard Spring Boot server SSL properties:
 *
 * <pre>
 * server:
 *   ssl:
 *     enabled: true
 *     key-store: classpath:keystore.p12
 *     key-store-password: ${SSL_KEYSTORE_PASSWORD}
 *     key-store-type: PKCS12
 *     key-alias: ftgo-gateway
 * </pre>
 *
 * <p>When TLS is terminated externally (the common production pattern), the gateway runs on plain
 * HTTP and trusts the {@code X-Forwarded-*} headers set by the upstream proxy. See {@code
 * server.forward-headers-strategy=framework} in {@code application.yml}.
 */
@Configuration
public class SslConfiguration {
    // SSL/TLS configuration is driven by Spring Boot's server.ssl.* properties.
    // This class serves as documentation and a hook for future programmatic TLS
    // customisation (e.g. mutual TLS, certificate rotation).
}
