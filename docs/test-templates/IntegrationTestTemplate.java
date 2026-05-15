package com.ftgo.BOUNDED_CONTEXT;

import net.chrisrichardson.ftgo.testlib.containers.FtgoMySQLContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

// =============================================================================
// INTEGRATION TEST TEMPLATE
// =============================================================================
// Copy this template and replace:
//   - BOUNDED_CONTEXT → order, consumer, restaurant, courier
//   - Repository → the Spring Data JPA repository being tested
//   - Entity → the domain entity
//
// Conventions:
//   - File location: src/integration-test/java/com/ftgo/<context>/<Name>IntegrationTest.java
//   - Uses Testcontainers for MySQL 5.7 (matches production)
//   - Flyway migrations run automatically via Spring Boot
//   - @Transactional rolls back after each test for isolation
// =============================================================================

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@Transactional
class RepositoryIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = FtgoMySQLContainer.getInstance();

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
    }

    // @Autowired
    // private EntityRepository entityRepository;

    @Test
    void save_validEntity_persistsToDatabase() {
        // Arrange
        // Entity entity = EntityBuilder.anEntity().build();

        // Act
        // Entity saved = entityRepository.save(entity);

        // Assert
        // assertThat(saved.getId()).isNotNull();
        // assertThat(entityRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void findById_existingEntity_returnsWithCorrectFields() {
        // Arrange
        // Entity entity = EntityBuilder.anEntity()
        //     .withName("Test")
        //     .build();
        // entityRepository.save(entity);

        // Act
        // Optional<Entity> found = entityRepository.findById(entity.getId());

        // Assert
        // assertThat(found)
        //     .isPresent()
        //     .get()
        //     .extracting(Entity::getName)
        //     .isEqualTo("Test");
    }

    @Test
    void findById_nonExistentEntity_returnsEmpty() {
        // Act
        // Optional<Entity> found = entityRepository.findById(999L);

        // Assert
        // assertThat(found).isEmpty();
    }
}
