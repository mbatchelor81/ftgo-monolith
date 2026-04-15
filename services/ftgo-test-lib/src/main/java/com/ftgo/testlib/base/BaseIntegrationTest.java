package com.ftgo.testlib.base;

import com.ftgo.testlib.containers.PostgresContainerConfig;
import org.junit.jupiter.api.Tag;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base class for integration tests that require a real PostgreSQL database.
 *
 * <p>Provides:
 *
 * <ul>
 *   <li>A shared PostgreSQL Testcontainer (started once per JVM)
 *   <li>Automatic Spring datasource configuration
 *   <li>Transaction rollback after each test for isolation
 *   <li>The {@code integration} JUnit tag for selective execution
 * </ul>
 *
 * <p>Usage:
 *
 * <pre>{@code
 * @SpringBootTest
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
 */
@Tag("integration")
@Transactional
public abstract class BaseIntegrationTest {

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        PostgresContainerConfig.registerProperties(registry);
    }
}
