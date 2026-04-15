package com.ftgo.security.config;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for {@link ActuatorSecurityConfiguration}.
 *
 * <p>Uses a full {@link SpringBootTest} so that actuator endpoints are registered.
 * Verifies that:
 * <ul>
 *   <li>Actuator /health endpoint is publicly accessible</li>
 *   <li>Other actuator endpoints require authentication</li>
 * </ul>
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = {
        "management.endpoints.web.exposure.include=health,info,metrics,env",
        "spring.main.allow-bean-definition-overriding=true"
    }
)
@AutoConfigureMockMvc
class ActuatorSecurityConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void actuatorHealth_isPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/actuator/health")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void actuatorInfo_isPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/actuator/info")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void actuatorMetrics_requiresAuthentication() throws Exception {
        mockMvc.perform(get("/actuator/metrics")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void actuatorEnv_requiresAuthentication() throws Exception {
        mockMvc.perform(get("/actuator/env")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin")
    void actuatorMetrics_withAuth_returns200() throws Exception {
        mockMvc.perform(get("/actuator/metrics")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @SpringBootApplication
    @Import({
        ActuatorSecurityConfiguration.class,
        BaseSecurityConfiguration.class,
        CorsSecurityConfiguration.class
    })
    static class TestApp {
    }
}
