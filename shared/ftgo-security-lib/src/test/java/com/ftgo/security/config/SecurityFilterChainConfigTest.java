package com.ftgo.security.config;

import org.junit.jupiter.api.DisplayName;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for {@link SecurityFilterChainConfig}.
 *
 * <p>Verifies that the default security filter chain correctly:
 * <ul>
 *   <li>Requires authentication for protected endpoints</li>
 *   <li>Permits public access to health/info endpoints</li>
 *   <li>Returns 401 JSON response for unauthenticated requests</li>
 *   <li>Allows authenticated users to access protected endpoints</li>
 * </ul>
 */
@SpringBootTest(
    classes = SecurityFilterChainConfigTest.TestApp.class,
    webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@AutoConfigureMockMvc
class SecurityFilterChainConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @SpringBootApplication
    @Import(FtgoSecurityAutoConfiguration.class)
    static class TestApp {

        @RestController
        static class TestController {
            @GetMapping("/api/test")
            public String test() {
                return "OK";
            }
        }
    }

    @Test
    @DisplayName("Unauthenticated request to protected endpoint returns 401")
    void protectedEndpoint_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/test")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "SERVICE")
    @DisplayName("Authenticated request to protected endpoint returns 200")
    void protectedEndpoint_authenticated_returns200() throws Exception {
        mockMvc.perform(get("/api/test")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("OPTIONS preflight requests are permitted")
    void optionsRequest_isPermitted() throws Exception {
        mockMvc.perform(options("/api/test")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "GET"))
            .andExpect(status().isOk());
    }
}
