package com.ftgo.test.containers;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for FTGO service integration tests that need a real MySQL.
 *
 * <p>Starts a single {@link FtgoMySqlContainer} per test JVM (static
 * field = container lifecycle is bound to the class) and exposes it as
 * {@link #MYSQL} so subclasses can wire Spring properties via
 * {@code @DynamicPropertySource}:
 *
 * <pre>{@code
 * @SpringBootTest
 * class OrderRepositoryIntegrationTest extends AbstractIntegrationTest {
 *
 *     @DynamicPropertySource
 *     static void mysqlProperties(DynamicPropertyRegistry registry) {
 *         registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
 *         registry.add("spring.datasource.username", MYSQL::getUsername);
 *         registry.add("spring.datasource.password", MYSQL::getPassword);
 *         registry.add("spring.datasource.driver-class-name", MYSQL::getDriverClassName);
 *     }
 *
 *     @Test
 *     void repositoryPersistsOrder() { ... }
 * }
 * }</pre>
 *
 * <p>Subclasses do <strong>not</strong> have to re-declare
 * {@link Testcontainers @Testcontainers}; it is inherited. The container
 * uses {@code withReuse(true)} so developer laptops that opt in via
 * {@code ~/.testcontainers.properties} skip the cold-start cost on every
 * run. CI always gets a fresh container per workflow job.
 */
@Testcontainers
public abstract class AbstractIntegrationTest {

    @Container
    protected static final FtgoMySqlContainer MYSQL = FtgoMySqlContainer.shared();
}
