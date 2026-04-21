package net.chrisrichardson.ftgo.orderservice.security;

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
 * Verifies the order service wires the shared {@code libs/ftgo-security}
 * baseline correctly:
 * <ul>
 *   <li>{@code /actuator/health} stays public</li>
 *   <li>Business endpoints (e.g. {@code /orders}) require authentication</li>
 *   <li>401 responses carry the shared JSON error body</li>
 *   <li>CSRF is disabled so POSTs with Basic auth succeed without a token</li>
 * </ul>
 */
@SpringBootTest(classes = OrderServiceSecurityIntegrationTest.TestApp.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = "management.endpoints.web.exposure.include=health,info")
class OrderServiceSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void actuatorHealth_withoutAuth_returns200() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void ordersEndpoint_withoutAuth_returns401WithJsonBody() throws Exception {
        mockMvc.perform(get("/orders/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.path").value("/orders/1"));
    }

    @Test
    void ordersEndpoint_withBasicAuth_returns200() throws Exception {
        mockMvc.perform(get("/orders/1").with(httpBasic("user", "test-password")))
                .andExpect(status().isOk());
    }

    @Test
    void postOrders_withBasicAuthAndNoCsrfToken_succeeds() throws Exception {
        mockMvc.perform(post("/orders")
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
    @Import({FtgoSecurityAutoConfiguration.class, OrderServiceSecurityConfiguration.class})
    static class TestApp {

        @RestController
        @RequestMapping("/orders")
        static class OrdersController {

            @GetMapping("/{id}")
            public String get() {
                return "order";
            }

            @PostMapping
            public String create() {
                return "created";
            }
        }
    }
}
