package com.ftgo.example.integration;

import com.ftgo.test.containers.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * FTGO integration-test template.
 *
 * <p>ArchitectureNotes
 * ------------------
 * An integration test in FTGO:
 *   - Boots a (narrow) Spring context via {@code @SpringBootTest} or a
 *     slice annotation (@WebMvcTest / @DataJpaTest).
 *   - Uses a real MySQL 8 via Testcontainers — never docker-compose.
 *   - Applies Flyway migrations automatically (the container is a clean
 *     MySQL instance, so Spring Boot's Flyway auto-config runs).
 *   - Runs in seconds, not milliseconds — budget accordingly.
 *
 * <p>The shared {@link AbstractIntegrationTest} base class starts a
 * single per-JVM {@code FtgoMySqlContainer} and exposes it as
 * {@code MYSQL}. Subclasses wire Spring's datasource properties via
 * {@link DynamicPropertySource}.
 *
 * <p>When to choose this tier
 * -------------------------
 * Anything that depends on Spring wiring or real SQL:
 *   - JPA queries
 *   - Flyway migrations
 *   - Security filters (401/403 paths)
 *   - Controller ↔ service integration through MockMvc
 *
 * <p>When NOT to use this tier
 * --------------------------
 *   - Pure domain logic — use {@code UnitTestTemplate.java}.
 *   - Cross-service behaviour — use {@code ContractTestTemplate.java}.
 *   - Full HTTP stack assertions — use {@code ApiTestTemplate.java}.
 */
@SpringBootTest
@AutoConfigureMockMvc
class IntegrationTestTemplate extends AbstractIntegrationTest {

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", MYSQL::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getOrder_whenPersisted_returnsJsonBody() throws Exception {
        // Arrange
        // TODO: seed a test order through the service's repository, e.g.
        //   orderRepository.save(OrderBuilder.anOrder().build());

        // Act + Assert
        mockMvc.perform(get("/orders/{id}", 99L).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(99))
                .andExpect(jsonPath("$.state").value("APPROVED"));
    }

    @Test
    void getOrder_whenMissing_returns404() throws Exception {
        mockMvc.perform(get("/orders/{id}", 999999L))
                .andExpect(status().isNotFound());
    }
}
