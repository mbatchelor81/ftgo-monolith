package net.chrisrichardson.ftgo.security.jwt;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static net.chrisrichardson.ftgo.security.jwt.JwtResourceServerIntegrationTest.JWT_SECRET;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies that a service importing {@link net.chrisrichardson.ftgo.security.FtgoSecurityAutoConfiguration}
 * behaves as an OAuth2 Resource Server when {@code ftgo.security.jwt.secret}
 * is configured: valid bearer tokens authenticate the request, while missing,
 * malformed, expired, tampered, or wrong-issuer tokens are rejected with
 * the FTGO standard 401 JSON body.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "ftgo.security.jwt.secret=" + JWT_SECRET,
        "ftgo.security.jwt.issuer=ftgo-auth",
        "ftgo.security.jwt.audience=ftgo-services",
        "ftgo.security.jwt.access-token-ttl=PT5M",
        "ftgo.security.jwt.refresh-token-ttl=PT30M"
})
class JwtResourceServerIntegrationTest {

    static final String JWT_SECRET = "test-secret-key-for-ftgo-security-library-integration-test";

    private static final SecretKeySpec KEY = new SecretKeySpec(
            JWT_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtEncoder encoder;

    @Test
    void apiEndpoint_withoutToken_returns401Json() throws Exception {
        mockMvc.perform(get("/api/test").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    void apiEndpoint_withValidAccessToken_returns200AndExposesClaims() throws Exception {
        String token = issue(JwtTokenType.ACCESS, "user-42", "alice",
                List.of("CONSUMER"), List.of("order:read"));

        mockMvc.perform(get("/api/test").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user-42"))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.roles").value("CONSUMER"))
                .andExpect(jsonPath("$.permissions").value("order:read"));
    }

    @Test
    void apiEndpoint_withRefreshToken_returns401() throws Exception {
        String token = issue(JwtTokenType.REFRESH, "user-42", "alice", List.of(), List.of());

        mockMvc.perform(get("/api/test")
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void apiEndpoint_withMalformedToken_returns401() throws Exception {
        mockMvc.perform(get("/api/test")
                        .header("Authorization", "Bearer not-a-real-jwt")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void apiEndpoint_withExpiredToken_returns401() throws Exception {
        Instant past = Instant.now().minusSeconds(3600);
        JwtClaimsSet claims = baseClaims(past, past.plusSeconds(60), "user-42", "alice")
                .claim(JwtClaimNames.TOKEN_TYPE, JwtTokenType.ACCESS.claimValue())
                .claim(JwtClaimNames.ROLES, List.of("CONSUMER"))
                .claim(JwtClaimNames.PERMISSIONS, List.of("order:read"))
                .build();
        String expired = encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();

        mockMvc.perform(get("/api/test")
                        .header("Authorization", "Bearer " + expired)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void apiEndpoint_withWrongIssuer_returns401() throws Exception {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("someone-else")
                .audience(List.of("ftgo-services"))
                .issuedAt(now)
                .expiresAt(now.plusSeconds(300))
                .subject("user-42")
                .id(UUID.randomUUID().toString())
                .claim(JwtClaimNames.TOKEN_TYPE, JwtTokenType.ACCESS.claimValue())
                .claim(JwtClaimNames.USER_ID, "user-42")
                .claim(JwtClaimNames.USERNAME, "alice")
                .build();
        String badIssuer = encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();

        mockMvc.perform(get("/api/test")
                        .header("Authorization", "Bearer " + badIssuer)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void apiEndpoint_withWrongAudience_returns401() throws Exception {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("ftgo-auth")
                .audience(List.of("some-other-system"))
                .issuedAt(now)
                .expiresAt(now.plusSeconds(300))
                .subject("user-42")
                .id(UUID.randomUUID().toString())
                .claim(JwtClaimNames.TOKEN_TYPE, JwtTokenType.ACCESS.claimValue())
                .claim(JwtClaimNames.USER_ID, "user-42")
                .claim(JwtClaimNames.USERNAME, "alice")
                .build();
        String badAudience = encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();

        mockMvc.perform(get("/api/test")
                        .header("Authorization", "Bearer " + badAudience)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void apiEndpoint_withTokenSignedByDifferentKey_returns401() throws Exception {
        SecretKeySpec otherKey = new SecretKeySpec(
                "some-other-completely-different-32-byte-key-xx".getBytes(StandardCharsets.UTF_8),
                "HmacSHA256");
        JwtEncoder otherEncoder = new NimbusJwtEncoder(new ImmutableSecret<>(otherKey));
        Instant now = Instant.now();
        JwtClaimsSet claims = baseClaims(now, now.plusSeconds(300), "user-42", "alice")
                .claim(JwtClaimNames.TOKEN_TYPE, JwtTokenType.ACCESS.claimValue())
                .build();
        String tampered = otherEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();

        mockMvc.perform(get("/api/test")
                        .header("Authorization", "Bearer " + tampered)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    private String issue(JwtTokenType type,
                         String userId,
                         String username,
                         List<String> roles,
                         List<String> permissions) {
        Instant now = Instant.now();
        JwtClaimsSet.Builder builder = baseClaims(now, now.plusSeconds(300), userId, username)
                .claim(JwtClaimNames.TOKEN_TYPE, type.claimValue());
        if (type == JwtTokenType.ACCESS) {
            builder.claim(JwtClaimNames.ROLES, roles)
                    .claim(JwtClaimNames.PERMISSIONS, permissions);
        }
        return encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), builder.build())).getTokenValue();
    }

    private static JwtClaimsSet.Builder baseClaims(Instant issuedAt, Instant expiresAt,
                                                   String userId, String username) {
        return JwtClaimsSet.builder()
                .issuer("ftgo-auth")
                .audience(List.of("ftgo-services"))
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(userId)
                .id(UUID.randomUUID().toString())
                .claim(JwtClaimNames.USER_ID, userId)
                .claim(JwtClaimNames.USERNAME, username);
    }

    @SpringBootApplication
    static class TestApplication {

        @RestController
        static class ClaimsController {
            @GetMapping("/api/test")
            public java.util.Map<String, String> claims() {
                return java.util.Map.of(
                        "userId", net.chrisrichardson.ftgo.security.SecurityUtils.getCurrentUserId().orElse(""),
                        "username", net.chrisrichardson.ftgo.security.SecurityUtils.getCurrentUsername().orElse(""),
                        "roles", String.join(",", net.chrisrichardson.ftgo.security.SecurityUtils.getCurrentRoles()),
                        "permissions", String.join(",", net.chrisrichardson.ftgo.security.SecurityUtils.getCurrentPermissions())
                );
            }
        }
    }
}
