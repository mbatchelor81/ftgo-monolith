package com.ftgo.security.jwt;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("jwt-test")
class JwtAuthenticationIntegrationTest {

    private static RSAKey rsaKey;

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    static void generateKey() throws Exception {
        rsaKey = new RSAKeyGenerator(2048)
                .keyID("test-key-id")
                .generate();
    }

    @Test
    void protectedEndpointReturns401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/test")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    void protectedEndpointReturns200WithValidJwt() throws Exception {
        String token = createSignedJwt("user-abc", List.of("ftgo-consumer"), List.of());

        mockMvc.perform(get("/api/test")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("user-abc"));
    }

    @Test
    void healthEndpointIsPublicWithJwtEnabled() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpointReturns401WithExpiredToken() throws Exception {
        String token = createExpiredJwt("user-expired");

        mockMvc.perform(get("/api/test")
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rolesAreExtractedFromJwt() throws Exception {
        String token = createSignedJwt("admin-user", List.of("ftgo-admin"), List.of());

        mockMvc.perform(get("/api/role-check")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void permissionsAreExtractedFromJwt() throws Exception {
        String token = createSignedJwt("perm-user", List.of(), List.of("order:read"));

        mockMvc.perform(get("/api/permission-check")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    private String createSignedJwt(String subject, List<String> roles, List<String> permissions) throws Exception {
        JWSSigner signer = new RSASSASigner(rsaKey);

        JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                .subject(subject)
                .issueTime(new Date())
                .expirationTime(Date.from(Instant.now().plusSeconds(300)));

        if (!roles.isEmpty()) {
            claimsBuilder.claim("realm_access", Map.of("roles", roles));
        }
        if (!permissions.isEmpty()) {
            claimsBuilder.claim("permissions", permissions);
        }

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.getKeyID()).build(),
                claimsBuilder.build());
        signedJWT.sign(signer);
        return signedJWT.serialize();
    }

    private String createExpiredJwt(String subject) throws Exception {
        JWSSigner signer = new RSASSASigner(rsaKey);

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(subject)
                .issueTime(Date.from(Instant.now().minusSeconds(600)))
                .expirationTime(Date.from(Instant.now().minusSeconds(300)))
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.getKeyID()).build(),
                claims);
        signedJWT.sign(signer);
        return signedJWT.serialize();
    }

    @SpringBootApplication
    @RestController
    static class TestJwtApplication {

        @GetMapping("/api/test")
        public String test(Principal principal) {
            return principal.getName();
        }

        @GetMapping("/api/role-check")
        public String roleCheck() {
            return String.valueOf(
                    com.ftgo.security.util.SecurityUtils.hasRole("ftgo-admin"));
        }

        @GetMapping("/api/permission-check")
        public String permissionCheck() {
            return String.valueOf(
                    com.ftgo.security.util.SecurityUtils.hasAuthority("order:read"));
        }

        @Bean
        public JwtDecoder jwtDecoder() throws Exception {
            return NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build();
        }
    }
}
