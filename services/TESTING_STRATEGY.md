# FTGO Microservices Testing Strategy

> Comprehensive testing strategy for the FTGO microservices migration.
> This document defines the testing pyramid, test types, tooling, and guidelines
> for writing effective tests across all bounded contexts.

---

## Table of Contents

1. [Testing Pyramid](#testing-pyramid)
2. [Test Types](#test-types)
3. [When to Write Which Test](#when-to-write-which-test)
4. [Tooling & Dependencies](#tooling--dependencies)
5. [Test Data Management](#test-data-management)
6. [Mocking vs Real Dependencies](#mocking-vs-real-dependencies)
7. [Test Naming Conventions](#test-naming-conventions)
8. [JUnit 5 Migration Guide](#junit-5-migration-guide)
9. [Testcontainers Usage](#testcontainers-usage)
10. [Coverage Requirements](#coverage-requirements)
11. [CI Integration](#ci-integration)
12. [Bounded Context Test Examples](#bounded-context-test-examples)

---

## Testing Pyramid

```
            /  E2E Tests  \              Few   — slow, high confidence
           /               \             Run on merge to main or nightly
          /  Contract Tests  \           Some  — verify service boundaries
         /                    \          Run on PR creation
        /  Integration Tests   \         More  — test with real dependencies
       /                        \        Run on PR creation
      /      Unit Tests          \       Many  — fast, isolated, focused
     /____________________________\      Run on every push
```

| Level           | Count   | Speed    | Scope                              | When to Run        |
|-----------------|---------|----------|------------------------------------|--------------------|
| **Unit**        | Many    | ms       | Single class/method                | Every push         |
| **Integration** | Some    | seconds  | Service + DB, multi-component      | PR creation        |
| **Contract**    | Some    | seconds  | API producer/consumer boundaries   | PR creation        |
| **E2E**         | Few     | 10s+     | Full service stack, user journeys  | Merge / nightly    |

**Rule of thumb:** If you can test it with a unit test, do. Reach for integration/E2E
only when unit tests cannot cover the interaction.

---

## Test Types

### Unit Tests

**Purpose:** Verify individual classes and methods in isolation.

**Characteristics:**
- No Spring context, no database, no external dependencies
- Use Mockito to mock collaborators
- Follow Arrange-Act-Assert (AAA) pattern
- Execute in milliseconds

**What to test:**
- Domain entity state transitions (e.g., `Order.cancel()`, `Order.acceptTicket()`)
- Value object behavior (e.g., `Money.add()`, `Money.multiply()`)
- Service-layer business logic with mocked repositories
- Input validation and edge cases
- Error handling paths

**Source set:** `src/test/java`
**JUnit tag:** None (excluded from `integration` and `e2e` tags)

### Integration Tests

**Purpose:** Verify that multiple components work together correctly with real
external dependencies (database, message broker, etc.).

**Characteristics:**
- Use `@SpringBootTest` or slice annotations (`@DataJpaTest`, `@WebMvcTest`)
- Use Testcontainers for PostgreSQL and other infrastructure
- Slower than unit tests but higher confidence
- Test real SQL queries, JPA mappings, and transaction behavior

**What to test:**
- Repository queries against a real database
- Controller endpoints with MockMvc or WebTestClient
- Service orchestration with real repositories
- Flyway migration compatibility
- Security filter chain behavior

**Source set:** `src/integration-test/java`
**JUnit tag:** `@Tag("integration")`

### Contract Tests

**Purpose:** Verify that API producers honor the contracts expected by consumers,
and that consumers can handle the producer's responses.

**Characteristics:**
- Use Spring Cloud Contract or REST-Assured with JSON schema validation
- Focus on request/response shapes, not business logic
- Prevent breaking changes when services evolve independently

**What to test:**
- REST API request/response schemas
- Required fields and data types
- Error response formats
- API versioning compatibility

**Source set:** `src/test/java` (contract-specific test classes)
**JUnit tag:** None (runs with unit tests)

### API Tests (REST-Assured)

**Purpose:** Validate REST endpoints end-to-end against a running Spring context.

**Characteristics:**
- Use REST-Assured with `@SpringBootTest(webEnvironment = RANDOM_PORT)`
- Test HTTP status codes, response bodies, headers
- Verify content negotiation and error handling

**What to test:**
- Full request/response cycle for each endpoint
- Authentication and authorization enforcement
- Input validation error responses
- Pagination and filtering behavior

**Source set:** `src/integration-test/java`
**JUnit tag:** `@Tag("integration")`

### End-to-End Tests

**Purpose:** Validate critical user journeys across the full service stack.

**Characteristics:**
- Require all services running (via Docker Compose)
- Test cross-service workflows
- Slow and potentially brittle; keep count low

**What to test:**
- Order lifecycle: create -> accept -> prepare -> pickup -> deliver
- Consumer registration and order placement
- Restaurant creation with menu management

**Source set:** `src/e2e-test/java`
**JUnit tag:** `@Tag("e2e")`

---

## When to Write Which Test

| Scenario                                          | Test Type        |
|---------------------------------------------------|------------------|
| Domain entity method (state transition, calc)     | Unit             |
| Value object behavior (Money, Address)            | Unit             |
| Service method with mocked dependencies           | Unit             |
| Repository custom query                           | Integration      |
| Controller endpoint status codes + response body  | Integration      |
| JPA entity mapping correctness                    | Integration      |
| Security filter chain (authenticated/anonymous)   | Integration      |
| API request/response contract shape               | Contract         |
| Full REST endpoint with real DB                   | API (REST-Assured)|
| Cross-service order lifecycle                     | E2E              |
| Database migration compatibility                  | Integration      |

### Decision Flowchart

```
Is it pure logic with no external dependencies?
  YES -> Unit Test
  NO  -> Does it need a database or Spring context?
           YES -> Does it test a single layer?
                    YES -> Slice test (@DataJpaTest, @WebMvcTest)
                    NO  -> Full integration test (@SpringBootTest)
           NO  -> Does it verify API contract shape?
                    YES -> Contract Test
                    NO  -> Does it span multiple services?
                             YES -> E2E Test
                             NO  -> Unit Test with mocks
```

---

## Tooling & Dependencies

All test dependencies are managed centrally in `gradle/libs.versions.toml`
and applied via the `ftgo.testing-conventions` build-logic plugin.

| Tool                | Purpose                        | Version  |
|---------------------|--------------------------------|----------|
| JUnit 5             | Test framework                 | 5.10.2   |
| Mockito             | Mocking framework              | 5.11.0   |
| AssertJ             | Fluent assertions              | 3.25.3   |
| Testcontainers      | Containerized test infra       | 1.19.7   |
| REST-Assured        | REST API testing               | 5.4.0    |
| Spring Boot Test    | Spring integration testing     | 3.2.5    |
| H2                  | In-memory DB for unit tests    | 2.2.224  |
| JaCoCo              | Code coverage                  | 0.8.12   |

### Test Library (`ftgo-test-lib`)

The `ftgo-test-lib` module provides shared test utilities:

- **Test Data Builders** — Fluent builders for domain entities (`OrderBuilder`,
  `ConsumerBuilder`, `RestaurantBuilder`, `CourierBuilder`)
- **Testcontainers Configuration** — Shared PostgreSQL container setup
- **Custom Assertions** — Domain-specific AssertJ assertions (`OrderAssert`,
  `MoneyAssert`)
- **Base Test Classes** — `BaseIntegrationTest`, `BaseContractTest`

Add it as a `testImplementation` dependency:

```groovy
testImplementation project(':ftgo-test-lib')
```

---

## Test Data Management

### Use Builders, Not Shared Fixtures

```java
// GOOD - clear what's being tested, minimal data
var order = OrderBuilder.anOrder()
    .withConsumerId(1L)
    .withRestaurant(restaurant)
    .build();

// BAD - opaque shared fixture
var order = TestFixtures.ORDER_1;
```

### Builder Guidelines

1. Every builder provides sensible defaults for all required fields
2. Use `with*` methods to override only what matters for the test
3. Builders live in `ftgo-test-lib` for cross-module reuse
4. Make test data minimal — include only fields relevant to the test

### Database Isolation

- Each integration test method gets a fresh transaction (rolled back after test)
- Use `@Transactional` on test classes or `@Sql` to reset state
- Testcontainers provides a clean PostgreSQL instance per test class

---

## Mocking vs Real Dependencies

### When to Mock

| Mock                                | Don't Mock                        |
|-------------------------------------|-----------------------------------|
| Repository interfaces (unit tests)  | The class under test              |
| External HTTP clients               | Simple value objects              |
| Clock / time providers              | Pure functions                    |
| Message publishers                  | Domain entities                   |
| Third-party API clients             | Internal logic of the SUT         |

### Mocking Guidelines

```java
// GOOD - mock only what you need
when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

// BAD - mocking everything
when(orderRepository.findAll()).thenReturn(List.of());
when(orderRepository.save(any())).thenReturn(order);
when(orderRepository.count()).thenReturn(1L);
```

- **Unit tests:** Mock repositories and external clients
- **Integration tests:** Use real repositories with Testcontainers DB
- Verify interactions (`verify()`) only when the side effect IS the point of the test
- Prefer fakes (in-memory implementations) over deep mocking chains

---

## Test Naming Conventions

### Method Naming

```
methodName_condition_expectedResult
```

Examples:
```java
cancel_whenApproved_setsStateToCancelled()
cancel_whenAlreadyCancelled_throwsUnsupportedStateTransition()
createOrder_withValidInput_returnsApprovedOrder()
findById_whenNotExists_returnsEmpty()
```

### Class Naming

| Test Type   | Convention                        | Example                          |
|-------------|-----------------------------------|----------------------------------|
| Unit        | `{ClassName}Test`                 | `OrderServiceTest`               |
| Integration | `{ClassName}IntegrationTest`      | `OrderRepositoryIntegrationTest` |
| Contract    | `{ServiceName}ContractTest`       | `OrderApiContractTest`           |
| API         | `{Endpoint}ApiTest`               | `OrderApiTest`                   |
| E2E         | `{Feature}E2eTest`                | `OrderLifecycleE2eTest`          |

### Display Names

Use `@DisplayName` for human-readable test output:

```java
@Nested
@DisplayName("cancel")
class Cancel {
    @Test
    @DisplayName("should set state to CANCELLED when order is APPROVED")
    void cancel_whenApproved_setsStateToCancelled() { ... }
}
```

---

## JUnit 5 Migration Guide

The codebase has been migrated from JUnit 4 to JUnit 5. Key differences:

| JUnit 4               | JUnit 5                          |
|-----------------------|----------------------------------|
| `@Test` (org.junit)   | `@Test` (org.junit.jupiter.api)  |
| `@Before`             | `@BeforeEach`                    |
| `@After`              | `@AfterEach`                     |
| `@BeforeClass`        | `@BeforeAll`                     |
| `@AfterClass`         | `@AfterAll`                      |
| `@Ignore`             | `@Disabled`                      |
| `@RunWith`            | `@ExtendWith`                    |
| `@Rule`               | `@ExtendWith` or `@RegisterExtension` |
| `@Category`           | `@Tag`                           |
| `Assert.assertEquals` | `Assertions.assertEquals` or AssertJ |

### Key JUnit 5 Features

```java
// Nested test classes for grouping
@Nested
@DisplayName("when order is approved")
class WhenApproved { ... }

// Parameterized tests
@ParameterizedTest
@EnumSource(value = OrderState.class, names = {"PREPARING", "PICKED_UP"})
void cancel_whenNotApproved_throwsException(OrderState state) { ... }

// Lifecycle per class (for expensive setup)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ExpensiveSetupTest { ... }

// Extensions replace runners and rules
@ExtendWith(MockitoExtension.class)
class ServiceTest { ... }
```

---

## Testcontainers Usage

### Shared PostgreSQL Container

The `ftgo-test-lib` provides a shared PostgreSQL container configuration:

```java
@SpringBootTest
@Tag("integration")
class OrderRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void save_withValidOrder_persistsToDatabase() {
        // Uses real PostgreSQL via Testcontainers
        var order = OrderBuilder.anOrder().build();
        var saved = orderRepository.save(order);
        assertThat(saved.getId()).isNotNull();
    }
}
```

### Container Lifecycle

- Containers are started once per test class via `@Testcontainers`
- Use `@Container` with `static` fields for class-level containers
- The `BaseIntegrationTest` base class handles PostgreSQL container setup
- Dynamic properties are injected via `@DynamicPropertySource`

---

## Coverage Requirements

| Metric         | Minimum | Target |
|----------------|---------|--------|
| Line coverage  | 70%     | 80%+   |
| Branch coverage| 70%     | 80%+   |

Coverage is enforced by JaCoCo in the `ftgo.testing-conventions` plugin.

### What Counts Toward Coverage

- Unit tests contribute to the primary coverage report
- Integration tests have a separate JaCoCo report
- A combined report merges both via `jacocoCombinedReport` task

### What NOT to Chase Coverage On

- Framework boilerplate (getters/setters, JPA-generated code)
- Auto-generated configuration classes
- Exception classes with no logic
- Trivial pass-through methods

---

## CI Integration

### Test Execution Order

```
1. ./gradlew test                  # Unit tests (every push)
2. ./gradlew integrationTest       # Integration tests (PR creation)
3. ./gradlew e2eTest               # E2E tests (merge to main / nightly)
```

### Gradle Tasks

| Task                           | Description                           |
|--------------------------------|---------------------------------------|
| `test`                         | Run unit tests (excludes integration/e2e tags) |
| `integrationTest`              | Run integration tests (tag: integration) |
| `e2eTest`                      | Run E2E tests (tag: e2e)              |
| `jacocoTestReport`             | Unit test coverage report             |
| `jacocoIntegrationTestReport`  | Integration test coverage report      |
| `jacocoCombinedReport`         | Merged coverage report                |
| `jacocoTestCoverageVerification` | Enforce coverage thresholds         |

### CI Pipeline Rules

- **Fail fast:** All test failures fail the build immediately
- **Parallel execution:** Unit tests run with `maxParallelForks`
- **Non-interactive:** Use `-B` flag for Maven compatibility, JUnit runs headless
- **Coverage gates:** JaCoCo verification runs as part of `check`

---

## Bounded Context Test Examples

Each bounded context should have tests at multiple levels. See the `ftgo-test-lib`
module for reusable builders and the individual service modules for example tests:

### Order Context
- **Unit:** `OrderTest` — state machine transitions, cancel/revise logic
- **Unit:** `OrderLineItemsTest` — order total calculations
- **Integration:** `OrderRepositoryIntegrationTest` — JPA persistence
- **Contract:** `OrderApiContractTest` — REST API response shapes

### Consumer Context
- **Unit:** `ConsumerTest` — order validation logic
- **Integration:** `ConsumerRepositoryIntegrationTest` — persistence

### Restaurant Context
- **Unit:** `RestaurantTest` — menu item lookup, creation
- **Integration:** `RestaurantRepositoryIntegrationTest` — persistence

### Courier Context
- **Unit:** `CourierTest` — availability, plan management
- **Integration:** `CourierRepositoryIntegrationTest` — persistence
