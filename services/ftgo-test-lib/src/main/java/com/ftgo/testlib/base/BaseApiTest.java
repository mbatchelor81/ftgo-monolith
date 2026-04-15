package com.ftgo.testlib.base;

import com.ftgo.testlib.containers.PostgresContainerConfig;
import org.junit.jupiter.api.Tag;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Base class for API tests that use {@code RANDOM_PORT} or {@code DEFINED_PORT}.
 *
 * <p>Unlike {@link BaseIntegrationTest}, this class does <strong>not</strong> apply
 * {@code @Transactional}. When Spring Boot starts an embedded server on a real port the HTTP client
 * and server run in separate threads with separate transactions, so {@code @Transactional} on the
 * test class would silently fail to roll back server-side changes.
 *
 * <p>For test isolation, use one of the following strategies in your subclass:
 *
 * <ul>
 *   <li>Truncate or reset tables in a {@code @BeforeEach} method
 *   <li>Use unique test data that does not conflict across tests
 *   <li>Use Spring's {@code @Sql} annotation to load/clean data per test
 * </ul>
 *
 * <p>Provides:
 *
 * <ul>
 *   <li>A shared PostgreSQL Testcontainer (started once per JVM)
 *   <li>Automatic Spring datasource configuration via {@code @DynamicPropertySource}
 *   <li>The {@code integration} JUnit tag for selective execution
 * </ul>
 *
 * @see BaseIntegrationTest
 */
@Tag("integration")
public abstract class BaseApiTest {

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        PostgresContainerConfig.registerProperties(registry);
    }
}
