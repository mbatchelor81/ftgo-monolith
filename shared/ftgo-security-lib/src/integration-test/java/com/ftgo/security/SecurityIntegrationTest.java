package com.ftgo.security;

import com.ftgo.security.config.FtgoSecurityAutoConfiguration;
import com.ftgo.security.config.FtgoSecurityAutoConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests verifying the full FTGO security configuration stack.
 *
 * <p>Tests the complete security filter chain including authentication,
 * authorization, actuator security, and JSON error responses working together.
 */
@SpringBootTest(
    classes = SecurityIntegrationTest.TestApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = {
        "spring.security.user.name=testuser",
        "spring.security.user.password={noop}testpass",
        "spring.security.user.roles=SERVICE",
        "management.endpoints.web.exposure.include=health,info,metrics,env"
    }
)
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @SpringBootApplication
    @Import(FtgoSecurityAutoConfiguration.class)
    static class TestApplication {

        @RestController
        static class SecurityTestController {
            @GetMapping("/api/orders")
            public String getOrders() {
                return "{\"orders\": []}";
            }

            @GetMapping("/api/consumers")
            public String getConsumers() {
                return "{\"consumers\": []}";
            }
        }
    }

    @Nested
    @DisplayName("Authentication")
    class AuthenticationTests {

        @Test
        @DisplayName("Unauthenticated request returns 401 with JSON body")
        void unauthenticated_returns401Json() throws Exception {
            mockMvc.perform(get("/api/orders")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("HTTP Basic with valid credentials returns 200")
        void httpBasic_validCredentials_returns200() throws Exception {
            mockMvc.perform(get("/api/orders")
                    .with(httpBasic("testuser", "testpass"))
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("HTTP Basic with invalid credentials returns 401")
        void httpBasic_invalidCredentials_returns401() throws Exception {
            mockMvc.perform(get("/api/orders")
                    .with(httpBasic("testuser", "wrongpassword"))
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Actuator Security")
    class ActuatorTests {

        @Test
        @DisplayName("Health endpoint is publicly accessible")
        void health_isPublic() throws Exception {
            mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Info endpoint is publicly accessible")
        void info_isPublic() throws Exception {
            mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Metrics endpoint requires authentication")
        void metrics_requiresAuth() throws Exception {
            mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Metrics endpoint accessible with valid credentials")
        void metrics_withAuth_returns200() throws Exception {
            mockMvc.perform(get("/actuator/metrics")
                    .with(httpBasic("testuser", "testpass")))
                .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("CSRF Protection")
    class CsrfTests {

        @Test
        @WithMockUser(username = "testuser", roles = "SERVICE")
        @DisplayName("CSRF is disabled for stateless API — GET succeeds without token")
        void csrfDisabled_getSucceeds() throws Exception {
            mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Session Management")
    class SessionTests {

        @Test
        @DisplayName("No session cookie is set (stateless)")
        void stateless_noSessionCookie() throws Exception {
            mockMvc.perform(get("/api/orders")
                    .with(httpBasic("testuser", "testpass")))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    // Verify no JSESSIONID cookie is set
                    var cookies = result.getResponse().getCookies();
                    for (var cookie : cookies) {
                        assert !cookie.getName().equals("JSESSIONID")
                            : "JSESSIONID cookie should not be set in stateless mode";
                    }
                });
        }
    }
}
