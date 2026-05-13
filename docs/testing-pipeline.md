# Testing Pipeline

Automated test execution across three tiers — unit, integration, and end-to-end — replacing manual test execution.

## Architecture

```
        ╱   E2E   ╲          ci-tests-e2e.yml — full Docker Compose stack
       ╱───────────╲
      ╱ Integration ╲        ci-tests-integration.yml — MySQL service container
     ╱───────────────╲
    ╱   Unit Tests    ╲      ci-tests-unit.yml — no external dependencies
   ╱───────────────────╲
```

| Tier | Workflow | Trigger | Duration | External Deps |
|------|----------|---------|----------|---------------|
| Unit | `ci-tests-unit.yml` | Push & PR | ~2 min | None |
| Integration | `ci-tests-integration.yml` | Push & PR | ~5 min | MySQL 5.7 |
| E2E | `ci-tests-e2e.yml` | Push & PR | ~10 min | Full Docker stack |

---

## Tier 1 — Unit Tests

Fast-feedback tests that run without external services. Split into three parallel jobs:

### Legacy Modules

Modules targeting Java 8 source with JUnit 4:

| Module | Tests | Description |
|--------|-------|-------------|
| `ftgo-common` | `MoneyTest`, `MoneySerializationTest` | Value object and serialization logic |
| `ftgo-order-service` | `OrderControllerTest` | Controller tests with Mockito mocks |

```bash
./gradlew :ftgo-common:test :ftgo-order-service:test
```

### Shared Libraries

Modules targeting Java 17 with JUnit 5 (via `ftgo.testing-conventions`):

| Module | Tests | Description |
|--------|-------|-------------|
| `ftgo-common-lib` | `MoneyTest`, `MoneySerializationTest` | Shared value object tests |
| `ftgo-security-lib` | `SecurityFilterChainIntegrationTest`, `SecurityUtilsTest` | Spring Security config |
| `ftgo-metrics-lib` | `FtgoMetricsAutoConfigurationTest` | Metrics auto-configuration |

```bash
./gradlew :ftgo-common-lib:test :ftgo-security-lib:test :ftgo-metrics-lib:test
```

### Microservices

New service modules targeting Java 17 with JUnit 5:

```bash
./gradlew \
  :consumer-service-api:test :consumer-service-app:test \
  :order-service-api:test :order-service-app:test \
  :restaurant-service-api:test :restaurant-service-app:test \
  :courier-service-api:test :courier-service-app:test
```

### Coverage

- Shared libraries and microservices produce JaCoCo reports via `ftgo.quality-conventions`.
- Reports: `**/build/reports/jacoco/test/jacocoTestReport.xml`
- Minimum thresholds: 70% line coverage, 50% branch coverage.

---

## Tier 2 — Integration Tests

Tests requiring a MySQL 5.7 database. Split into two parallel jobs:

### Monolith Integration

Runs `FtgoApplicationTest` — a `@SpringBootTest` that boots the full application context
against a live MySQL instance and exercises the REST API through the order lifecycle.

```bash
# Requires MySQL on localhost:3306
./gradlew :ftgo-flyway:flywayMigrate
./gradlew :ftgo-application:test
```

### Microservice Integration

Runs the `integrationTest` source set defined by `ftgo.testing-conventions` for each
microservice module. Sources live in `src/integration-test/java`.

```bash
# Requires MySQL on localhost:3306
./gradlew :ftgo-flyway:flywayMigrate
./gradlew \
  :consumer-service-app:integrationTest \
  :order-service-app:integrationTest \
  :restaurant-service-app:integrationTest \
  :courier-service-app:integrationTest
```

### MySQL Service Container

The CI workflow provisions MySQL 5.7 as a GitHub Actions service container:

| Parameter | Value |
|-----------|-------|
| Image | `mysql:5.7` |
| Root password | `rootpassword` |
| Database | `ftgo` |
| User | `mysqluser` |
| Password | `mysqlpw` |
| Port | `3306` |

Flyway migrations (`ftgo-flyway` module) are run before tests to ensure the schema is current.

---

## Tier 3 — E2E Tests

Full end-to-end tests against the complete Docker Compose stack.

### What It Tests

`EndToEndTests` (extending `AbstractEndToEndTests`) exercises the full order lifecycle:

1. Create consumer, restaurant, and order
2. Revise and cancel an order
3. Create courier, accept order, and complete delivery flow

### How It Runs

1. Build the application JAR (`./gradlew :ftgo-application:assemble`)
2. Build Docker images (`docker compose build`)
3. Start the full stack (`docker compose up -d`)
4. Wait for the application health endpoint (`/actuator/health`)
5. Run E2E tests with `DOCKER_HOST_IP=127.0.0.1`
6. Tear down (`docker compose down -v`)

```bash
export DOCKER_HOST_IP=127.0.0.1
./gradlew :ftgo-end-to-end-tests:test
```

### Failure Debugging

On failure, the workflow uploads:
- JUnit XML test results
- HTML test reports
- Docker Compose logs for all services

---

## Test Result Reporting

All tiers produce JUnit XML reports and upload them as workflow artifacts.

| Artifact | Contents | Retention |
|----------|----------|-----------|
| `unit-test-results-*` | JUnit XML from unit tests | 14 days |
| `unit-test-reports-*` | HTML test reports | 7 days |
| `unit-coverage-reports-*` | JaCoCo HTML/XML coverage | 7 days |
| `integration-test-results-*` | JUnit XML from integration tests | 14 days |
| `integration-test-reports-*` | HTML test reports | 7 days |
| `integration-coverage-reports-*` | JaCoCo coverage | 7 days |
| `e2e-test-results` | JUnit XML from E2E tests | 14 days |
| `e2e-test-reports` | HTML test reports | 7 days |
| `e2e-docker-logs` | Docker logs (on failure only) | 7 days |

---

## Coverage Reporting

Coverage is generated by JaCoCo for modules that apply `ftgo.quality-conventions`:

- **Shared libraries** (`libs/`): Coverage enforced with quality gates.
- **Microservices** (`services/`): Coverage enforced with quality gates.
- **Legacy modules**: No JaCoCo (uses JUnit 4, not wired into quality conventions).

The `ci-quality-gate.yml` workflow runs the full static analysis + coverage pipeline
separately, including Checkstyle, PMD, SpotBugs, and SonarQube analysis.

### Adding Coverage to a New Module

1. Apply the quality conventions plugin in your module's `build.gradle`:
   ```groovy
   plugins {
       id 'ftgo.quality-conventions'
   }
   ```
2. JaCoCo is configured automatically with 70% line / 50% branch thresholds.
3. Coverage XML is generated at `build/reports/jacoco/test/jacocoTestReport.xml`.

---

## Local Development

### Running All Unit Tests

```bash
./gradlew :ftgo-common:test :ftgo-order-service:test \
  :ftgo-common-lib:test :ftgo-security-lib:test :ftgo-metrics-lib:test
```

### Running Integration Tests Locally

```bash
# Start MySQL (via Docker Compose from repo root)
docker compose up -d mysql

# Wait for MySQL and run migrations
./gradlew :ftgo-flyway:flywayMigrate

# Run integration tests
./gradlew :ftgo-application:test
```

### Running E2E Tests Locally

```bash
# Build and start the full stack
./gradlew :ftgo-application:assemble
docker compose up -d

# Wait for application to be healthy
curl -sf http://localhost:8081/actuator/health

# Run E2E tests
export DOCKER_HOST_IP=127.0.0.1
./gradlew :ftgo-end-to-end-tests:test

# Tear down
docker compose down -v
```

### Writing New Tests

| Test Type | Location | Framework | Needs DB? |
|-----------|----------|-----------|-----------|
| Unit | `src/test/java` | JUnit 5 + Mockito | No |
| Integration | `src/integration-test/java` | JUnit 5 + Spring Boot Test | Yes |
| E2E | `ftgo-end-to-end-tests` | JUnit 4 + REST-assured | Full stack |

For new microservice modules, apply `ftgo.testing-conventions` to get:
- JUnit 5 configured for both `test` and `integrationTest` source sets
- REST-assured, Mockito, AssertJ, and H2 test dependencies
- JUnit XML + HTML report generation
- `check` task wired to include integration tests

---

## Workflow Dependency Graph

```
ci-tests-unit.yml          ci-tests-integration.yml       ci-tests-e2e.yml
├─ legacy-unit-tests       ├─ monolith-integration        └─ e2e-tests
├─ library-unit-tests      ├─ microservice-integration
├─ microservice-unit-tests └─ integration-tests-passed
└─ unit-tests-passed

ci-monolith.yml            ci-quality-gate.yml
└─ build (full monolith)   ├─ quality (static analysis)
                           └─ sonar (SonarQube)
```

The testing workflows complement the existing `ci-monolith.yml` (full build) and
`ci-quality-gate.yml` (static analysis + coverage) workflows. They provide faster,
more granular feedback by isolating test tiers into separate workflow runs.
