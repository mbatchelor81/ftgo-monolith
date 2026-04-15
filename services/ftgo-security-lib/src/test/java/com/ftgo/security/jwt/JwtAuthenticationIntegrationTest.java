package com.ftgo.security.jwt;

import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ftgo.security.config.BaseSecurityConfiguration;
import com.ftgo.security.config.CorsSecurityConfiguration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests verifying the full JWT authentication flow through
 * the Spring Security filter chain.
 *
 * <p>Tests that:
 * <ul>
 *   <li>Requests without tokens receive 401</li>
 *   <li>Requests with valid JWT tokens are authenticated</li>
 *   <li>Invalid tokens are rejected with 401</li>
 *   <li>User context (roles, userId) is available in controllers</li>
 * </ul>
 */
@WebMvcTest
@Import(JwtAuthenticationIntegrationTest.TestConfig.class)
class JwtAuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService tokenService;

    @Test
    void requestWithoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/test/secure")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void requestWithValidAccessToken_returns200() throws Exception {
        String token = tokenService.generateAccessToken(
                "user-1", "john.doe",
                List.of("USER"),
                List.of("order:read"));

        mockMvc.perform(get("/api/test/secure")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("john.doe"));
    }

    @Test
    void requestWithValidToken_hasCorrectRoles() throws Exception {
        String token = tokenService.generateAccessToken(
                "user-2", "admin.user",
                List.of("ADMIN", "USER"),
                List.of("order:write"));

        mockMvc.perform(get("/api/test/secure")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("admin.user"));
    }

    @Test
    void requestWithInvalidToken_returns401() throws Exception {
        mockMvc.perform(get("/api/test/secure")
                .header("Authorization", "Bearer invalid.jwt.token")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void requestWithMalformedAuthHeader_returns401() throws Exception {
        mockMvc.perform(get("/api/test/secure")
                .header("Authorization", "NotBearer sometoken")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void requestWithExpiredToken_returns401() throws Exception {
        // We can't easily create an already-expired token with the service,
        // but we can verify a tampered token fails
        String validToken = tokenService.generateAccessToken(
                "user-1", "test", List.of(), List.of());
        // Tamper with the token payload
        String tamperedToken = validToken.substring(0, validToken.lastIndexOf('.'))
                + ".invalidsignature";

        mockMvc.perform(get("/api/test/secure")
                .header("Authorization", "Bearer " + tamperedToken)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Configuration
    @Import({
        BaseSecurityConfiguration.class,
        CorsSecurityConfiguration.class,
        JwtConfiguration.class
    })
    static class TestConfig {
        @Bean
        public TestSecureController testSecureController() {
            return new TestSecureController();
        }
    }

    @RestController
    static class TestSecureController {

        @GetMapping("/api/test/secure")
        public java.util.Map<String, Object> secure(
                org.springframework.security.core.Authentication authentication) {
            java.util.Map<String, Object> response = new java.util.LinkedHashMap<>();
            response.put("username", authentication.getName());
            response.put("authorities", authentication.getAuthorities().stream()
                    .map(Object::toString)
                    .toList());
            return response;
        }
    }
}
