package net.chrisrichardson.ftgo.consumerservice.security;

import net.chrisrichardson.ftgo.security.FtgoSecurityAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies the consumer service wires the shared {@code libs/ftgo-security}
 * baseline: health is public, {@code /consumers/**} requires auth, 401 JSON
 * body is returned, and CSRF is disabled for stateless API calls.
 */
@SpringBootTest(classes = ConsumerServiceSecurityIntegrationTest.TestApp.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = "management.endpoints.web.exposure.include=health,info")
class ConsumerServiceSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void actuatorHealth_withoutAuth_returns200() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void consumersEndpoint_withoutAuth_returns401WithJsonBody() throws Exception {
        mockMvc.perform(get("/consumers/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    void consumersEndpoint_withBasicAuth_returns200() throws Exception {
        mockMvc.perform(get("/consumers/1").with(httpBasic("user", "test-password")))
                .andExpect(status().isOk());
    }

    @Test
    void postConsumers_withBasicAuthAndNoCsrfToken_succeeds() throws Exception {
        mockMvc.perform(post("/consumers")
                        .with(httpBasic("user", "test-password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @SpringBootApplication(exclude = {
            DataSourceAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class,
            FlywayAutoConfiguration.class
    })
    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class,
            FlywayAutoConfiguration.class
    })
    @Import({FtgoSecurityAutoConfiguration.class, ConsumerServiceSecurityConfiguration.class})
    static class TestApp {

        @RestController
        @RequestMapping("/consumers")
        static class ConsumersController {

            @GetMapping("/{id}")
            public String get() {
                return "consumer";
            }

            @PostMapping
            public String create() {
                return "created";
            }
        }
    }
}
