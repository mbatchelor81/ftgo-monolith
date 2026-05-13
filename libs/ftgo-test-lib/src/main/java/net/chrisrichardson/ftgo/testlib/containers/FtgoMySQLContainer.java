package net.chrisrichardson.ftgo.testlib.containers;

import org.testcontainers.containers.MySQLContainer;

/**
 * Pre-configured MySQL 5.7 Testcontainer for FTGO integration tests.
 *
 * <p>Provides a singleton container instance that is reused across all test classes
 * within a JVM. Credentials match the {@code docker-compose.yml} and CI service
 * container configuration.
 *
 * <p>Usage:
 * <pre>
 * {@literal @}Testcontainers
 * {@literal @}SpringBootTest
 * class MyIntegrationTest {
 *
 *     {@literal @}Container
 *     static MySQLContainer{@literal <?>} mysql = FtgoMySQLContainer.getInstance();
 *
 *     {@literal @}DynamicPropertySource
 *     static void configureDataSource(DynamicPropertyRegistry registry) {
 *         registry.add("spring.datasource.url", mysql::getJdbcUrl);
 *         registry.add("spring.datasource.username", mysql::getUsername);
 *         registry.add("spring.datasource.password", mysql::getPassword);
 *     }
 * }
 * </pre>
 */
public final class FtgoMySQLContainer {

    private static final String IMAGE = "mysql:5.7";
    private static final String DATABASE = "ftgo";
    private static final String USERNAME = "mysqluser";
    private static final String PASSWORD = "mysqlpw";

    private static MySQLContainer<?> container;

    private FtgoMySQLContainer() {
    }

    /**
     * Returns a singleton MySQL container instance configured for FTGO.
     * The container is created on first access and reused for the JVM lifetime.
     */
    public static synchronized MySQLContainer<?> getInstance() {
        if (container == null) {
            container = new MySQLContainer<>(IMAGE)
                    .withDatabaseName(DATABASE)
                    .withUsername(USERNAME)
                    .withPassword(PASSWORD)
                    .withReuse(true);
        }
        return container;
    }
}
