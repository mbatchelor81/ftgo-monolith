package com.ftgo.security.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for {@link ActuatorSecurityConfig}.
 *
 * <p>Verifies that actuator endpoints are secured correctly:
 * <ul>
 *   <li>{@code /actuator/health} is publicly accessible</li>
 *   <li>{@code /actuator/info} is publicly accessible</li>
 *   <li>Other actuator endpoints require authentication</li>
 * </ul>
 */
@SpringBootTest(
    classes = ActuatorSecurityConfigTest.TestApp.class,
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = {
        "management.endpoints.web.exposure.include=health,info,metrics,env"
    }
)
@AutoConfigureMockMvc
class ActuatorSecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @SpringBootApplication
    @Import(FtgoSecurityAutoConfiguration.class)
    static class TestApp {
    }

    @Test
    @DisplayName("Health endpoint is publicly accessible")
    void healthEndpoint_isPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Info endpoint is publicly accessible")
    void infoEndpoint_isPublic() throws Exception {
        mockMvc.perform(get("/actuator/info"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Metrics endpoint requires authentication")
    void metricsEndpoint_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/actuator/metrics"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("Metrics endpoint accessible when authenticated")
    void metricsEndpoint_authenticated_returns200() throws Exception {
        mockMvc.perform(get("/actuator/metrics"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Env endpoint requires authentication")
    void envEndpoint_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/actuator/env"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("Env endpoint accessible when authenticated")
    void envEndpoint_authenticated_returns200() throws Exception {
        mockMvc.perform(get("/actuator/env"))
            .andExpect(status().isOk());
    }
}
