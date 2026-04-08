package com.ftgo.security.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for {@link CorsConfig}.
 *
 * <p>Verifies that CORS headers are returned correctly for preflight requests.
 */
@SpringBootTest(
    classes = CorsConfigTest.TestApp.class,
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = {
        "ftgo.security.cors.allowed-origins=http://localhost:3000",
        "ftgo.security.cors.allowed-methods=GET,POST,PUT,DELETE",
        "ftgo.security.cors.allow-credentials=false"
    }
)
@AutoConfigureMockMvc
class CorsConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @SpringBootApplication
    @Import(FtgoSecurityAutoConfiguration.class)
    static class TestApp {

        @RestController
        static class CorsTestController {
            @GetMapping("/api/cors-test")
            public String test() {
                return "OK";
            }
        }
    }

    @Test
    @DisplayName("CORS preflight returns allowed origin header")
    void corsPreflight_returnsAllowedOrigin() throws Exception {
        mockMvc.perform(options("/api/cors-test")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "GET"))
            .andExpect(status().isOk())
            .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
    }

    @Test
    @DisplayName("CORS preflight returns allowed methods header")
    void corsPreflight_returnsAllowedMethods() throws Exception {
        mockMvc.perform(options("/api/cors-test")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST"))
            .andExpect(status().isOk())
            .andExpect(header().exists("Access-Control-Allow-Methods"));
    }
}
