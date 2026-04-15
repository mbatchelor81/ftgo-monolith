package com.ftgo.testlib.containers;

import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Shared PostgreSQL Testcontainer configuration for integration tests.
 *
 * <p>Provides a singleton PostgreSQL container that is reused across all integration test classes
 * within a JVM. This avoids the overhead of starting a new container for every test class.
 *
 * <p>Usage in integration tests:
 *
 * <pre>{@code
 * @DynamicPropertySource
 * static void configureProperties(DynamicPropertyRegistry registry) {
 *     PostgresContainerConfig.registerProperties(registry);
 * }
 * }</pre>
 */
public final class PostgresContainerConfig {

    private static final String POSTGRES_IMAGE = "postgres:16-alpine";
    private static final String DATABASE_NAME = "ftgo_test";
    private static final String USERNAME = "ftgo_test";
    private static final String PASSWORD = "ftgo_test";

    @SuppressWarnings("resource")
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(POSTGRES_IMAGE)
                    .withDatabaseName(DATABASE_NAME)
                    .withUsername(USERNAME)
                    .withPassword(PASSWORD)
                    .withReuse(true);

    static {
        POSTGRES.start();
    }

    private PostgresContainerConfig() {}

    /** Returns the shared PostgreSQL container instance. */
    public static PostgreSQLContainer<?> getInstance() {
        return POSTGRES;
    }

    /**
     * Registers Spring datasource properties from the running container.
     *
     * @param registry the dynamic property registry from {@code @DynamicPropertySource}
     */
    public static void registerProperties(
            org.springframework.test.context.DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }
}
