# Testcontainers for Integration Tests

Guide for using Testcontainers in FTGO integration tests to replace
shared MySQL service containers with isolated, disposable instances.

---

## Overview

[Testcontainers](https://testcontainers.com/) provides lightweight, throwaway
Docker containers for integration testing. Each test class (or test suite) gets
its own MySQL 5.7 container, ensuring:

- **Isolation:** No shared state between test classes
- **Reproducibility:** Tests run identically on any machine with Docker
- **No cleanup required:** Containers are destroyed after tests complete

---

## Prerequisites

| Requirement | Version | Notes |
|-------------|---------|-------|
| Docker | 20.10+ | Required for container lifecycle |
| Java | 17+ | New microservices target |
| Testcontainers | 1.19.7 | Managed in `libs.versions.toml` |

### CI Configuration

GitHub Actions workflows use Docker-in-Docker. The `ci-tests-integration.yml`
workflow already provisions Docker — Testcontainers works out of the box.

### Local Development

Ensure Docker Desktop (or Docker Engine) is running. Testcontainers auto-detects
the Docker environment via `DOCKER_HOST` or the default socket.

---

## Setup

### 1. Add Dependencies

The `ftgo-test-lib` module provides a pre-configured `FtgoMySQLContainer`.
Add it to your module:

```groovy
// build.gradle
dependencies {
    testImplementation project(":ftgo-test-lib")
}
```

This transitively provides:
- `org.testcontainers:testcontainers` (BOM)
- `org.testcontainers:junit-jupiter`
- `org.testcontainers:mysql`
- `com.mysql:mysql-connector-j`

### 2. Use `FtgoMySQLContainer` in Tests

```java
import net.chrisrichardson.ftgo.testlib.containers.FtgoMySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
@Testcontainers
class OrderRepositoryIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = FtgoMySQLContainer.getInstance();

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void findById_existingOrder_returnsOrder() {
        // Flyway runs automatically via Spring Boot auto-configuration
        // Container is fresh — no leftover data from other tests
    }
}
```

---

## Shared Container Pattern

For test suites where starting a new container per class is too slow,
use the singleton container pattern:

```java
@SpringBootTest
@Testcontainers
class BaseIntegrationTest {

    @Container
    static final MySQLContainer<?> MYSQL = FtgoMySQLContainer.getInstance();

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
    }
}

// Subclasses share the same container
class OrderRepositoryTest extends BaseIntegrationTest {
    @Test
    void findById_existingOrder_returnsOrder() { }
}

class ConsumerRepositoryTest extends BaseIntegrationTest {
    @Test
    void findById_existingConsumer_returnsConsumer() { }
}
```

`FtgoMySQLContainer.getInstance()` returns a singleton — the same container
instance is reused across all test classes within a JVM. The container starts
once and is destroyed when the JVM exits.

---

## Configuration Details

### FtgoMySQLContainer

The pre-configured container in `ftgo-test-lib`:

```java
public final class FtgoMySQLContainer {

    private static final String IMAGE = "mysql:5.7";
    private static final String DATABASE = "ftgo";
    private static final String USERNAME = "mysqluser";
    private static final String PASSWORD = "mysqlpw";

    private static MySQLContainer<?> container;

    public static MySQLContainer<?> getInstance() {
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
```

The credentials match the `docker-compose.yml` MySQL service and CI service
container configuration, ensuring consistency across environments.

### Flyway Integration

Flyway runs automatically when Spring Boot starts with the Testcontainers
datasource. The `ftgo-flyway` module's migration scripts populate the schema.
No manual migration step is needed in tests.

### application-test.yml

For integration tests, use a test profile to override configuration:

```yaml
spring:
  flyway:
    locations: classpath:db/migration
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
```

---

## Troubleshooting

### Container Fails to Start

1. Ensure Docker is running: `docker info`
2. Check available disk space: `docker system df`
3. Pull the image manually: `docker pull mysql:5.7`
4. Check Testcontainers logs: set `testcontainers.reuse.enable=true` in
   `~/.testcontainers.properties` for container reuse during local development

### Slow Container Startup

- Use the singleton pattern (shared container) to avoid per-class startup overhead
- Enable container reuse in local development:
  ```properties
  # ~/.testcontainers.properties
  testcontainers.reuse.enable=true
  ```
- Use `withReuse(true)` on the container (already configured in `FtgoMySQLContainer`)

### Port Conflicts

Testcontainers assigns random ports — conflicts with a local MySQL instance
are not possible. Access the mapped port via `mysql.getMappedPort(3306)`.

---

## Reference

| Resource | Link |
|----------|------|
| Testcontainers docs | https://testcontainers.com/ |
| MySQL module | https://java.testcontainers.org/modules/databases/mysql/ |
| Spring Boot integration | https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing.testcontainers |
| JUnit 5 integration | https://java.testcontainers.org/test_framework_integration/junit_5/ |
