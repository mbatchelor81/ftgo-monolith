package com.ftgo.security.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Tests for {@link BaseSecurityConfiguration}.
 *
 * <p>Verifies that:
 *
 * <ul>
 *   <li>Unauthenticated requests to API endpoints return 401
 *   <li>Authenticated requests are allowed through
 *   <li>CSRF is disabled (POST without token succeeds for authenticated users)
 * </ul>
 */
@WebMvcTest
@Import({BaseSecurityConfigurationTest.TestConfig.class})
class BaseSecurityConfigurationTest {

    @Autowired private MockMvc mockMvc;

    @Test
    void unauthenticatedRequest_returns401() throws Exception {
        mockMvc.perform(get("/api/test").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser")
    void authenticatedRequest_returns200() throws Exception {
        mockMvc.perform(get("/api/test").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser")
    void csrfDisabled_postWithoutTokenSucceeds() throws Exception {
        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(
                                        "/api/test")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                .andExpect(status().isOk());
    }

    @Configuration
    @Import({BaseSecurityConfiguration.class, CorsSecurityConfiguration.class})
    static class TestConfig {
        @Bean
        public TestController testController() {
            return new TestController();
        }
    }

    @RestController
    static class TestController {
        @GetMapping("/api/test")
        public String get() {
            return "ok";
        }

        @org.springframework.web.bind.annotation.PostMapping("/api/test")
        public String post() {
            return "ok";
        }
    }
}
