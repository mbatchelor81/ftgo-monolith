package com.ftgo.testlib.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Shared Testcontainers configuration for MySQL.
 * Replaces Docker Compose dependency for integration tests by providing
 * a self-contained MySQL container managed by Testcontainers.
 *
 * <p>Usage — import in your integration test:</p>
 * <pre>{@code
 * @SpringBootTest
 * @Import(MySqlTestcontainersConfiguration.class)
 * class MyIntegrationTest {
 *     // MySQL is automatically started and configured
 * }
 * }</pre>
 *
 * <p>Or use {@link com.ftgo.testlib.base.BaseIntegrationTest} which imports this automatically.</p>
 */
@TestConfiguration(proxyBeanMethods = false)
public class MySqlTestcontainersConfiguration {

    @Bean
    @ServiceConnection
    public MySQLContainer<?> mysqlContainer() {
        return new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
                .withDatabaseName("ftgo")
                .withUsername("ftgo_user")
                .withPassword("ftgo_password")
                .withCommand(
                        "--character-set-server=utf8mb4",
                        "--collation-server=utf8mb4_unicode_ci"
                );
    }
}
