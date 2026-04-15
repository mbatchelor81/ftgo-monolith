package com.ftgo.testlib.templates;

import static org.assertj.core.api.Assertions.assertThat;

import com.ftgo.testlib.base.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Template for integration tests using {@code @SpringBootTest} + Testcontainers.
 *
 * <h2>How to use this template</h2>
 *
 * <ol>
 *   <li>Copy this file into your service's {@code src/integration-test/java} directory
 *   <li>Rename the class (e.g., {@code OrderRepositoryIntegrationTest})
 *   <li>Add {@code @SpringBootTest} with your service's main class
 *   <li>Inject the real beans you need to test
 *   <li>Replace placeholder assertions with real test logic
 * </ol>
 *
 * <h2>Conventions</h2>
 *
 * <ul>
 *   <li>Extend {@link BaseIntegrationTest} for automatic Testcontainers PostgreSQL setup
 *   <li>Tests run in a transaction that is rolled back automatically
 *   <li>Tagged with {@code @Tag("integration")} via the base class
 *   <li>Use real repositories — do NOT mock the database layer
 * </ul>
 *
 * <h2>Example</h2>
 *
 * <pre>{@code
 * @SpringBootTest(classes = OrderServiceApplication.class)
 * class OrderRepositoryIntegrationTest extends BaseIntegrationTest {
 *
 *     @Autowired
 *     private OrderRepository orderRepository;
 *
 *     @Test
 *     void save_withValidOrder_persistsToDatabase() {
 *         var order = OrderBuilder.anOrder().build();
 *         var saved = orderRepository.save(order);
 *         assertThat(saved.getId()).isNotNull();
 *     }
 * }
 * }</pre>
 *
 * @see BaseIntegrationTest
 */
// @SpringBootTest(classes = YourServiceApplication.class) // Uncomment with your app class
@DisplayName("IntegrationTestTemplate — copy and rename")
@SuppressWarnings(
        "checkstyle:MethodName") // Template uses test naming convention: method_condition_result
public class IntegrationTestTemplate extends BaseIntegrationTest {

    // @Autowired
    // private YourRepository yourRepository;

    @Test
    @DisplayName("should persist entity to database")
    void save_withValidEntity_persistsToDatabase() {
        // Arrange
        // var entity = YourBuilder.anEntity().build();

        // Act
        // var saved = yourRepository.save(entity);

        // Assert
        // assertThat(saved.getId()).isNotNull();
        assertThat(true).isTrue(); // Placeholder
    }

    @Test
    @DisplayName("should find entity by ID")
    void findById_whenExists_returnsEntity() {
        // Arrange
        // var entity = yourRepository.save(YourBuilder.anEntity().build());

        // Act
        // var found = yourRepository.findById(entity.getId());

        // Assert
        // assertThat(found).isPresent();
        assertThat(true).isTrue(); // Placeholder
    }

    @Test
    @DisplayName("should return empty when entity does not exist")
    void findById_whenNotExists_returnsEmpty() {
        // var found = yourRepository.findById(999L);
        // assertThat(found).isEmpty();
        assertThat(true).isTrue(); // Placeholder
    }
}
