package com.ftgo.testlib.base;

import com.ftgo.testlib.config.MySqlTestcontainersConfiguration;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for integration tests with Testcontainers MySQL.
 *
 * <p>Provides:</p>
 * <ul>
 *   <li>Full Spring context with {@code @SpringBootTest}</li>
 *   <li>MySQL Testcontainer (auto-configured via {@link MySqlTestcontainersConfiguration})</li>
 *   <li>"integration" tag and "test" profile</li>
 * </ul>
 *
 * <p>Usage:</p>
 * <pre>{@code
 * class OrderRepositoryIntegrationTest extends BaseIntegrationTest {
 *
 *     @Autowired
 *     private OrderRepository orderRepository;
 *
 *     @Test
 *     void findAllByConsumerId_returnsOrdersForConsumer() {
 *         // Uses real MySQL via Testcontainers
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Guidelines:</strong></p>
 * <ul>
 *   <li>Use for testing database interactions, Spring context wiring, and component boundaries</li>
 *   <li>Prefer real dependencies (Testcontainers) over mocks for data layer tests</li>
 *   <li>Use {@code @Transactional} for automatic test isolation via rollback</li>
 *   <li>Place tests in {@code src/integration-test/java}</li>
 * </ul>
 */
@SpringBootTest
@Testcontainers
@Import(MySqlTestcontainersConfiguration.class)
@ActiveProfiles("test")
@Tag("integration")
public abstract class BaseIntegrationTest {
    // Intentionally empty — annotations are inherited by subclasses
}
