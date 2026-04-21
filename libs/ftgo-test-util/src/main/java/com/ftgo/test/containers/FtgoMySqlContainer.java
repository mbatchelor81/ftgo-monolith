package com.ftgo.test.containers;

import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Shared Testcontainers MySQL 8 container used by FTGO service integration
 * tests (EM-48).
 *
 * <p>Replaces the legacy {@code docker-compose up mysql} + {@code
 * wait-for-mysql.sh} orchestration. Each {@code @SpringBootTest} that
 * extends {@code AbstractIntegrationTest} (or declares this container as a
 * {@code @Container}) gets an isolated MySQL instance with the same
 * credentials and charset as production — no host-port collisions, no
 * cross-test data bleed.
 *
 * <p>Recommended usage inside a JUnit 5 test:
 *
 * <pre>{@code
 * @Testcontainers
 * class OrderServiceIntegrationTest {
 *     @Container
 *     static final FtgoMySqlContainer MYSQL = FtgoMySqlContainer.shared();
 *
 *     @DynamicPropertySource
 *     static void mysqlProperties(DynamicPropertyRegistry registry) {
 *         registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
 *         registry.add("spring.datasource.username", MYSQL::getUsername);
 *         registry.add("spring.datasource.password", MYSQL::getPassword);
 *     }
 * }
 * }</pre>
 *
 * <p>The container is marked reusable with {@link #withReuse(boolean)} so
 * developer laptops can share a single MySQL instance across test runs
 * when {@code ~/.testcontainers.properties} has {@code
 * testcontainers.reuse.enable=true}. CI runs always get a fresh
 * instance per job.
 */
public final class FtgoMySqlContainer extends MySQLContainer<FtgoMySqlContainer> {

    /**
     * Docker image used for every FTGO integration test. Pinned to a
     * specific patch so CI is reproducible and so local Docker caches
     * don't drift between developer machines.
     */
    public static final DockerImageName IMAGE = DockerImageName.parse("mysql:8.0.36");

    public static final String DEFAULT_DATABASE = "ftgo";
    public static final String DEFAULT_USERNAME = "ftgo";
    public static final String DEFAULT_PASSWORD = "ftgo";

    public FtgoMySqlContainer() {
        super(IMAGE);
        withDatabaseName(DEFAULT_DATABASE);
        withUsername(DEFAULT_USERNAME);
        withPassword(DEFAULT_PASSWORD);
        withUrlParam("useSSL", "false");
        withUrlParam("allowPublicKeyRetrieval", "true");
        withUrlParam("serverTimezone", "UTC");
        withUrlParam("characterEncoding", "UTF-8");
        withReuse(true);
    }

    /**
     * Convenience factory so tests can declare the container as a
     * {@code static final} field without repeating the constructor.
     */
    public static FtgoMySqlContainer shared() {
        return new FtgoMySqlContainer();
    }
}
