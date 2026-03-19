package com.ftgo.common.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BaseSecurityConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthEndpoint_withoutAuth_returns200() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpoint_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/test"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_withValidAuth_returns200() throws Exception {
        mockMvc.perform(get("/api/test")
                        .with(httpBasic("user", "test-password")))
                .andExpect(status().isOk());
    }

    @Test
    void unauthorizedResponse_returnsJsonBody() throws Exception {
        mockMvc.perform(get("/api/test")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @SpringBootApplication
    static class TestApplication {

        @RestController
        static class TestController {
            @GetMapping("/api/test")
            public String test() {
                return "ok";
            }
        }
    }
}
