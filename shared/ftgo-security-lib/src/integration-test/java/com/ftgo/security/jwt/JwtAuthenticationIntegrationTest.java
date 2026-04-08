package com.ftgo.security.jwt;

import com.ftgo.security.config.FtgoSecurityAutoConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests verifying JWT authentication end-to-end.
 *
 * <p>Tests the complete flow: token generation → HTTP request with Bearer token
 * → JWT filter validation → SecurityContext population → endpoint access.
 */
@SpringBootTest(
    classes = JwtAuthenticationIntegrationTest.TestApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = {
        "ftgo.security.jwt.enabled=true",
        "ftgo.security.jwt.secret=integration-test-secret-key-that-is-at-least-32-characters-long",
        "ftgo.security.jwt.issuer=ftgo-integration-test",
        "ftgo.security.jwt.expiration=1800",
        "ftgo.security.jwt.refresh-expiration=86400",
        "spring.security.user.name=testuser",
        "spring.security.user.password={noop}testpass",
        "spring.security.user.roles=SERVICE",
        "management.endpoints.web.exposure.include=health,info,metrics"
    }
)
@AutoConfigureMockMvc
class JwtAuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @SpringBootApplication
    @Import(FtgoSecurityAutoConfiguration.class)
    static class TestApplication {

        @RestController
        static class JwtTestController {

            @GetMapping("/api/protected")
            public String protectedEndpoint() {
                return "{\"message\": \"success\"}";
            }

            @GetMapping("/api/user-context")
            public String userContext() {
                var userDetails = com.ftgo.security.util.SecurityContextUtils.getCurrentUserDetails();
                if (userDetails.isPresent()) {
                    var details = userDetails.get();
                    return "{\"userId\": \"" + details.getUserId()
                        + "\", \"roles\": " + details.getRoles()
                        + ", \"permissions\": " + details.getPermissions() + "}";
                }
                return "{\"userId\": null}";
            }
        }
    }

    @Nested
    @DisplayName("JWT Token Authentication")
    class JwtTokenAuthentication {

        @Test
        @DisplayName("Valid access token grants access to protected endpoint")
        void validAccessToken_grantsAccess() throws Exception {
            String token = tokenProvider.generateAccessToken("user-123",
                Set.of("ROLE_USER"), Set.of("order:read"));

            mockMvc.perform(get("/api/protected")
                    .header("Authorization", "Bearer " + token)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("No token returns 401 Unauthorized")
        void noToken_returns401() throws Exception {
            mockMvc.perform(get("/api/protected")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Invalid token returns 401 Unauthorized")
        void invalidToken_returns401() throws Exception {
            mockMvc.perform(get("/api/protected")
                    .header("Authorization", "Bearer invalid.jwt.token")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Expired token returns 401 Unauthorized")
        void expiredToken_returns401() throws Exception {
            // Create a provider with 0-second expiration
            JwtProperties expiredProps = new JwtProperties();
            expiredProps.setSecret("integration-test-secret-key-that-is-at-least-32-characters-long");
            expiredProps.setIssuer("ftgo-integration-test");
            expiredProps.setExpiration(0);
            expiredProps.setRefreshExpiration(0);
            JwtTokenProvider expiredProvider = new JwtTokenProvider(expiredProps);

            String expiredToken = expiredProvider.generateAccessToken("user-1", Set.of(), Set.of());

            mockMvc.perform(get("/api/protected")
                    .header("Authorization", "Bearer " + expiredToken)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Refresh token cannot be used to access protected endpoints")
        void refreshToken_cannotAccessEndpoints() throws Exception {
            String refreshToken = tokenProvider.generateRefreshToken("user-123");

            mockMvc.perform(get("/api/protected")
                    .header("Authorization", "Bearer " + refreshToken)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Token signed with wrong key returns 401")
        void wrongKeyToken_returns401() throws Exception {
            JwtProperties otherProps = new JwtProperties();
            otherProps.setSecret("a-completely-different-secret-key-that-is-long-enough");
            otherProps.setIssuer("ftgo-integration-test");
            otherProps.setExpiration(1800);
            otherProps.setRefreshExpiration(86400);
            JwtTokenProvider otherProvider = new JwtTokenProvider(otherProps);

            String token = otherProvider.generateAccessToken("user-1", Set.of(), Set.of());

            mockMvc.perform(get("/api/protected")
                    .header("Authorization", "Bearer " + token)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("User Context Propagation")
    class UserContextPropagation {

        @Test
        @DisplayName("JWT claims are available as user context in service layer")
        void jwtClaims_availableAsUserContext() throws Exception {
            String token = tokenProvider.generateAccessToken("user-456",
                Set.of("ROLE_ADMIN"), Set.of("order:write"));

            mockMvc.perform(get("/api/user-context")
                    .header("Authorization", "Bearer " + token)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(
                    org.hamcrest.Matchers.containsString("\"userId\": \"user-456\"")));
        }
    }

    @Nested
    @DisplayName("Public Endpoints")
    class PublicEndpoints {

        @Test
        @DisplayName("Health endpoint remains accessible without token")
        void healthEndpoint_noToken_isAccessible() throws Exception {
            mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Info endpoint remains accessible without token")
        void infoEndpoint_noToken_isAccessible() throws Exception {
            mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Token Refresh Flow")
    class TokenRefreshFlow {

        @Test
        @DisplayName("Refresh token can generate new valid access token")
        void refreshToken_generatesNewAccessToken() throws Exception {
            // Generate a refresh token
            String refreshToken = tokenProvider.generateRefreshToken("user-789");

            // Use it to get a new access token
            var newAccessToken = tokenProvider.refreshAccessToken(
                refreshToken, Set.of("ROLE_USER"), Set.of("order:read"));

            // The new access token should work
            mockMvc.perform(get("/api/protected")
                    .header("Authorization", "Bearer " + newAccessToken.orElseThrow())
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        }
    }
}
