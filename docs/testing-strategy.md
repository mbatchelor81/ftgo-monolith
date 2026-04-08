# FTGO Microservices Testing Strategy

> **EM-48** — Comprehensive testing strategy for the FTGO monolith-to-microservices migration.

## Table of Contents

- [Testing Pyramid](#testing-pyramid)
- [Test Tiers](#test-tiers)
- [When to Write Which Test](#when-to-write-which-test)
- [Test Utility Library](#test-utility-library)
- [Testcontainers Configuration](#testcontainers-configuration)
- [Contract Testing](#contract-testing)
- [Mocking vs Real Dependencies](#mocking-vs-real-dependencies)
- [Coverage Targets](#coverage-targets)
- [CI Integration](#ci-integration)
- [Migration from Monolith Tests](#migration-from-monolith-tests)

---

## Testing Pyramid

```
            ╱ E2E Tests ╲           ~5%  — Full stack, critical paths only
           ╱──────────────╲
          ╱ Contract Tests  ╲       ~10% — Inter-service API contracts
         ╱────────────────────╲
        ╱  Integration Tests   ╲    ~15% — DB + Spring context + Testcontainers
       ╱────────────────────────╲
      ╱      Unit Tests          ╲  ~70% — Fast, isolated, mock dependencies
     ╱────────────────────────────╲
```

| Tier | Target Coverage | Execution Time | External Deps |
|------|----------------|----------------|---------------|
| **Unit** | 70% of tests | < 2 min total | None |
| **Integration** | 15% of tests | < 5 min total | MySQL (Testcontainers) |
| **Contract** | 10% of tests | < 3 min total | None (MockMvc) |
| **E2E** | 5% of tests | < 10 min total | Full stack |

---

## Test Tiers

### 1. Unit Tests

**Purpose:** Test individual classes and methods in isolation.

**Characteristics:**
- No Spring context loaded
- All dependencies mocked via Mockito
- Execute in milliseconds
- Located in `src/test/java`

**What to test:**
- Domain entity state transitions (e.g., `Order.cancel()`)
- Business logic and validation rules
- Value object behavior (e.g., `Money.add()`)
- Service-layer orchestration (with mocked repositories)
- Edge cases, null handling, boundary conditions

**Template:** Extend `BaseUnitTest`

```java
class OrderServiceTest extends BaseUnitTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrder_withValidInput_returnsOrder() {
        // Arrange
        var request = order().build();
        when(orderRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // Act
        var result = orderService.createOrder(request);

        // Assert
        assertThat(result).isApproved();
        verify(orderRepository).save(any(Order.class));
    }
}
```

### 2. Integration Tests

**Purpose:** Test component boundaries with real infrastructure.

**Characteristics:**
- Full Spring context with `@SpringBootTest`
- MySQL via Testcontainers (replaces Docker Compose)
- Located in `src/integration-test/java`
- Run via `./gradlew integrationTest`

**What to test:**
- Repository queries against a real database
- Spring context wiring and configuration
- JPA entity mapping and relationships
- Flyway migrations
- Transaction boundaries

**Template:** Extend `BaseIntegrationTest`

```java
class OrderRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @Transactional
    void findAllByConsumerId_returnsMatchingOrders() {
        // Arrange — data persisted to real MySQL
        // Act — query via repository
        // Assert — verify results
    }
}
```

### 3. Contract Tests

**Purpose:** Verify API contracts between services.

**Characteristics:**
- Uses `@WebMvcTest` for lightweight controller testing
- Service layer mocked with `@MockBean`
- Rest-Assured MockMvc for HTTP assertions
- Located in `src/test/java` alongside unit tests

**What to test:**
- REST endpoint request/response schemas
- HTTP status codes for success and error cases
- Content-Type and header contracts
- Request validation behavior

**Template:** Extend `BaseContractTest`

```java
@WebMvcTest(OrderController.class)
class OrderControllerContractTest extends BaseContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        initMockMvc(mockMvc);
    }

    @Test
    void getOrder_returns200WithCorrectSchema() {
        given()
            .contentType("application/json")
        .when()
            .get("/orders/{id}", 1L)
        .then()
            .statusCode(200)
            .body("orderState", equalTo("APPROVED"));
    }
}
```

### 4. API Tests (Rest-Assured)

**Purpose:** Full HTTP request/response testing including serialization.

**Characteristics:**
- Full Spring context on a random port
- Rest-Assured configured automatically
- Tests the entire HTTP pipeline
- Located in `src/test/java` or `src/integration-test/java`

**Template:** Extend `BaseApiTest`

```java
class OrderApiTest extends BaseApiTest {

    @Test
    void createOrder_returns201() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"consumerId": 1, "restaurantId": 1, "lineItems": []}
                """)
        .when()
            .post("/orders")
        .then()
            .statusCode(201)
            .body("id", notNullValue());
    }
}
```

### 5. E2E Tests

**Purpose:** Validate critical user journeys across the full stack.

**Characteristics:**
- Tests against a fully deployed environment
- Cover the complete order lifecycle
- Slowest tier — use sparingly
- Located in `ftgo-end-to-end-tests/`

**What to test:**
- Complete order flow: create → accept → prepare → pickup → deliver
- Cross-service interactions
- Data consistency across bounded contexts

---

## When to Write Which Test

| Scenario | Test Tier |
|----------|-----------|
| Domain entity method (e.g., `Order.cancel()`) | **Unit** |
| Service method with mocked dependencies | **Unit** |
| Value object logic (e.g., `Money.add()`) | **Unit** |
| Repository query correctness | **Integration** |
| JPA mapping and relationships | **Integration** |
| Flyway migration validation | **Integration** |
| REST endpoint request/response schema | **Contract** |
| API error handling and status codes | **Contract** |
| Inter-service communication contract | **Contract** |
| Full HTTP pipeline with serialization | **API** |
| Complete order lifecycle flow | **E2E** |
| Cross-service data consistency | **E2E** |

### Decision Flowchart

```
Is the behavior under test isolated to a single class?
├── YES → Can it run without Spring context?
│         ├── YES → UNIT TEST
│         └── NO  → Does it need a database?
│                   ├── YES → INTEGRATION TEST
│                   └── NO  → CONTRACT TEST (if API) / UNIT TEST (if service)
└── NO  → Does it cross service boundaries?
          ├── YES → CONTRACT TEST or E2E TEST
          └── NO  → INTEGRATION TEST
```

---

## Test Utility Library

The `shared/ftgo-test-lib` module provides reusable test infrastructure:

### Test Data Builders

```java
import static com.ftgo.testlib.builder.TestBuilders.*;

// Fluent builders for all domain entities
Order order = order().withConsumerId(42L).build();
Consumer consumer = consumer().withFirstName("Jane").build();
Restaurant restaurant = restaurant().withName("Ajanta").build();
Courier courier = courier().available().build();
MenuItem item = menuItem().withName("Pad Thai").withPrice("11.99").build();
Address addr = address().withCity("Oakland").build();
Money price = money().withAmount("12.99").build();
```

### Custom Assertions

```java
import static com.ftgo.testlib.assertion.FtgoAssertions.assertThat;

// Domain-specific assertions for readable tests
assertThat(order).isApproved().belongsToConsumer(1L).hasLineItemCount(2);
assertThat(money).hasAmount("25.98").isNotZero();
```

### Base Test Classes

| Class | Purpose | Annotations |
|-------|---------|-------------|
| `BaseUnitTest` | Unit tests with Mockito | `@ExtendWith(MockitoExtension.class)`, `@Tag("unit")` |
| `BaseIntegrationTest` | Integration tests with Testcontainers | `@SpringBootTest`, `@Testcontainers`, `@Tag("integration")` |
| `BaseContractTest` | Contract tests with MockMvc | `@ExtendWith(MockitoExtension.class)`, `@Tag("contract")` |
| `BaseApiTest` | API tests with Rest-Assured | `@SpringBootTest(RANDOM_PORT)`, `@Tag("api")` |

### Adding ftgo-test-lib to Your Service

```groovy
// In your service's build.gradle
dependencies {
    testImplementation project(":shared:ftgo-test-lib")
}
```

---

## Testcontainers Configuration

Testcontainers replaces Docker Compose for integration tests, providing:
- Self-contained MySQL instances per test suite
- No external Docker Compose dependency
- Automatic port allocation and cleanup
- Spring Boot auto-configuration via `@ServiceConnection`

### Shared Configuration

`MySqlTestcontainersConfiguration` provides a pre-configured MySQL 8.0 container:

```java
@SpringBootTest
@Import(MySqlTestcontainersConfiguration.class)
class MyIntegrationTest {
    // MySQL is automatically started and connected
}
```

Or simply extend `BaseIntegrationTest` which imports this configuration automatically.

### Custom Container Configuration

For service-specific needs, create a custom configuration:

```java
@TestConfiguration
public class CustomTestcontainersConfig {

    @Bean
    @ServiceConnection
    public MySQLContainer<?> mysql() {
        return new MySQLContainer<>("mysql:8.0")
                .withDatabaseName("my_service_db")
                .withInitScript("init.sql");
    }
}
```

---

## Contract Testing

### Approach: Spring Cloud Contract + Pact

We support two approaches for contract testing:

#### Provider-Side (Spring Cloud Contract)

1. Define contracts in the provider service under `src/test/resources/contracts/`
2. Auto-generated tests verify the provider implements the contract
3. Stubs are published for consumers to use

#### Consumer-Side (Pact)

1. Consumer defines expectations as Pact files
2. Provider verifies Pact files against its implementation
3. Pact Broker manages contract versions

### When to Use Which

| Scenario | Approach |
|----------|----------|
| Teams own both sides | Spring Cloud Contract |
| External consumers | Pact |
| API-first design | Spring Cloud Contract |
| Legacy integration | Pact |

### Contract Test Workflow

```
1. Consumer writes contract expectations
2. Contract test generates/verifies stubs
3. CI validates contracts on every build
4. Breaking changes caught before deployment
```

---

## Mocking vs Real Dependencies

### When to Mock

- **Unit tests:** Always mock external dependencies (repositories, services, HTTP clients)
- **Contract tests:** Mock service layer; test only controller + serialization
- **Time-dependent logic:** Use `TestClockConfiguration` for deterministic tests

### When to Use Real Dependencies

- **Integration tests:** Use Testcontainers for MySQL
- **Repository tests:** Test real SQL queries against a real database
- **JPA mapping tests:** Verify entity relationships with a real ORM
- **E2E tests:** Full stack with all services running

### Guidelines

```
Mock external boundaries, not internal logic:
✓ Mock: HTTP clients, message brokers, external APIs
✓ Mock: Repositories (in unit tests only)
✓ Mock: Clock, random number generators
✗ Don't mock: The class under test
✗ Don't mock: Simple value objects
✗ Don't mock: Pure functions
```

---

## Coverage Targets

| Module Type | Line Coverage | Branch Coverage |
|-------------|--------------|-----------------|
| Domain entities | ≥ 80% | ≥ 70% |
| Service layer | ≥ 70% | ≥ 60% |
| Controllers | ≥ 60% | ≥ 50% |
| Configuration | ≥ 40% | N/A |
| DTOs / Value objects | Skip (getter/setter) | Skip |

### JaCoCo Configuration

The `ftgo.testing-conventions` plugin configures JaCoCo automatically:
- `./gradlew jacocoTestReport` — Unit test coverage
- `./gradlew jacocoIntegrationTestReport` — Integration test coverage
- `./gradlew jacocoMergedReport` — Combined coverage report

Coverage thresholds are enforced via `jacocoTestCoverageVerification` (start at 0%, target 70%).

---

## CI Integration

The testing pipeline (EM-36) runs tests in this order:

```
1. Unit tests        → ./gradlew test                    (every push)
2. Integration tests → ./gradlew integrationTest         (every PR)
3. Contract tests    → ./gradlew contractTest            (every PR)
4. E2E tests         → ./gradlew :ftgo-end-to-end-tests:test  (merge to main)
5. Coverage report   → ./gradlew jacocoMergedReport      (every PR)
```

### Test Tags for Selective Execution

```bash
# Run only unit tests
./gradlew test -PincludeTags=unit

# Run only integration tests
./gradlew integrationTest -PincludeTags=integration

# Run only contract tests
./gradlew test -PincludeTags=contract
```

---

## Migration from Monolith Tests

### Step-by-Step Migration

1. **Identify test scope:** Map monolith tests to their bounded context
2. **Copy to microservice module:** Move tests to the appropriate `services/` directory
3. **Update imports:** Change package names from `net.chrisrichardson.ftgo` to `com.ftgo.<context>`
4. **Replace test data:** Use `TestBuilders` instead of inline object construction
5. **Replace assertions:** Use `FtgoAssertions` for domain-specific checks
6. **Add Testcontainers:** Replace Docker Compose with `BaseIntegrationTest`
7. **Verify:** Run `./gradlew test integrationTest` to confirm migration

### Existing Test Locations (Monolith)

| Module | Tests | Target Service |
|--------|-------|----------------|
| `shared/ftgo-domain/src/test/` | OrderTest, ConsumerTest, etc. | All bounded contexts |
| `shared/ftgo-common/src/test/` | MoneyTest, MoneySerializationTest | shared/ftgo-common |
| `ftgo-end-to-end-tests/` | EndToEndTests | E2E (keep as-is) |

See also: [JUnit 5 Migration Guide](junit5-migration-guide.md)
