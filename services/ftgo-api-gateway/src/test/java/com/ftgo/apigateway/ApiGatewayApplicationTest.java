package com.ftgo.apigateway;

import static org.assertj.core.api.Assertions.assertThat;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.context.ActiveProfiles;

/** Verifies the API Gateway application context loads successfully. */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.cloud.gateway.default-filters=",
            "spring.data.redis.host=localhost",
            "spring.data.redis.port=6379",
            "server.ssl.enabled=false"
        })
@ActiveProfiles("test")
class ApiGatewayApplicationTest {

    private static final byte[] SECRET_KEY_BYTES = new byte[32];

    @TestConfiguration
    static class TestJwtConfig {
        @Bean
        ReactiveJwtDecoder reactiveJwtDecoder() {
            SecretKey key = new SecretKeySpec(SECRET_KEY_BYTES, "HmacSHA256");
            return NimbusReactiveJwtDecoder.withSecretKey(key)
                    .macAlgorithm(MacAlgorithm.HS256)
                    .build();
        }
    }

    @Autowired private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
    }
}
