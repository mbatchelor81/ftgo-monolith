package com.ftgo.SERVICENAME.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test template using Testcontainers + PostgreSQL.
 *
 * <p>Integration tests should:
 * <ul>
 *   <li>Use the @Tag("integration") annotation so the Gradle integrationTest task picks them up</li>
 *   <li>Use Testcontainers for a real PostgreSQL instance</li>
 *   <li>Use @DynamicPropertySource to inject container connection info</li>
 *   <li>Test repository queries, Flyway migrations, and JPA mappings</li>
 * </ul>
 *
 * <p>Replace SERVICENAME with the actual service name when copying this template.
 */
@Tag("integration")
@Testcontainers
// @DataJpaTest  // Uncomment once real entities/repositories exist
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("ExampleRepository Integration")
class ExampleRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("ftgo_test")
            .withUsername("ftgo")
            .withPassword("ftgo");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
    }

    // @Autowired
    // private ExampleRepository exampleRepository;

    @Test
    @DisplayName("should connect to PostgreSQL test container")
    void shouldConnectToPostgres() {
        assertThat(postgres.isRunning()).isTrue();
    }

    @Test
    @DisplayName("should save and retrieve entity")
    void save_andFindById_returnsEntity() {
        // var entity = new ExampleEntity("test");
        // exampleRepository.save(entity);
        //
        // var found = exampleRepository.findById(entity.getId());
        // assertThat(found).isPresent();
        // assertThat(found.get().getName()).isEqualTo("test");
        assertThat(true).isTrue(); // Placeholder — replace with real test
    }
}
