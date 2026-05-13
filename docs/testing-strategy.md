# FTGO Platform — Testing Strategy

Comprehensive testing strategy for the FTGO monolith-to-microservices migration,
covering all tiers of the testing pyramid, tooling choices, and migration patterns.

---

## Testing Pyramid

```
          ╱   E2E    ╲          Few — full Docker Compose stack
         ╱─────────────╲        ci-tests-e2e.yml (~10 min)
        ╱  Integration  ╲      Some — MySQL service container
       ╱─────────────────╲     ci-tests-integration.yml (~5 min)
      ╱   Contract Tests  ╲    Pact / Spring Cloud Contract
     ╱─────────────────────╲   (planned — see §5)
    ╱     Unit Tests        ╲  Many — no external dependencies
   ╱─────────────────────────╲ ci-tests-unit.yml (~2 min)
```

| Tier | Scope | Speed | External Deps | Tooling |
|------|-------|-------|---------------|---------|
| **Unit** | Single class/method | ms | None | JUnit 5, Mockito, AssertJ |
| **Integration** | Service + DB, multi-component | seconds | MySQL 5.7 | Spring Boot Test, Testcontainers |
| **Contract** | API boundary between services | seconds | None | Spring Cloud Contract / Pact |
| **API / E2E** | Full application stack | 10s+ | Docker Compose | Rest-Assured, EndToEndTests |

**Guiding principle:** Push tests down the pyramid. If a unit test can verify
the behavior, prefer it over an integration test. Reserve E2E tests for
critical user journeys through the order lifecycle.

---

## 1. Unit Tests

### What to Test
- Domain logic: state transitions, validation, calculations (e.g., `Order`, `Money`, `Plan`)
- Service methods with mocked dependencies
- Value object behavior (equality, serialization)
- Exception and edge-case paths

### Patterns

```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrder_withValidDetails_returnsApprovedOrder() {
        // Arrange — use test data builders from ftgo-test-lib
        Restaurant restaurant = RestaurantBuilder.aRestaurant().build();
        // Act
        Order order = orderService.createOrder(/* ... */);
        // Assert — use AssertJ
        assertThat(order.getOrderState()).isEqualTo(OrderState.APPROVAL_PENDING);
    }
}
```

### Conventions
- **Naming:** `methodName_condition_expectedResult`
- **Structure:** Arrange-Act-Assert (AAA)
- **Assertions:** Use AssertJ (`assertThat`) for readability
- **Mocking:** Use `@ExtendWith(MockitoExtension.class)` — avoid `MockitoAnnotations.openMocks`
- **No Spring context:** Unit tests must not load `ApplicationContext`

### Coverage Targets
| Metric | Minimum | Enforced By |
|--------|---------|-------------|
| Line coverage | 70% | `ftgo.quality-conventions` / JaCoCo |
| Branch coverage | 50% | `ftgo.quality-conventions` / JaCoCo |

---

## 2. Integration Tests

### What to Test
- Repository queries against a real database
- Controller endpoints with full Spring context
- Service orchestration with real dependencies
- Flyway migrations on a clean schema
- JPA entity mapping and cascade behavior

### Patterns

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class OrderRepositoryIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = FtgoMySQLContainer.getInstance();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void findById_existingOrder_returnsOrder() {
        // test against real MySQL via Testcontainers
    }
}
```

### Source Set Layout
New microservices use the `integrationTest` source set defined by `ftgo.testing-conventions`:

```
services/order-service/order-service-app/
├── src/
│   ├── main/java/         # Production code
│   ├── test/java/          # Unit tests (JUnit 5)
│   └── integration-test/
│       ├── java/           # Integration tests
│       └── resources/      # Test-specific config (application-test.yml)
```

### Database
- **Testcontainers MySQL 5.7** for isolated, reproducible integration tests
- Flyway migrations run automatically via Spring Boot auto-configuration
- Each test class gets a fresh container (or shared via `@Container` static field)

---

## 3. API Tests

### What to Test
- REST endpoint request/response contracts
- HTTP status codes, content types, error responses
- Serialization/deserialization (especially `Money` via `MoneyModule`)
- Authentication/authorization enforcement

### Patterns

```java
@WebMvcTest(OrderController.class)
class OrderControllerApiTest {

    @MockBean
    private OrderService orderService;

    @MockBean
    private OrderRepository orderRepository;

    @Test
    void getOrder_existingOrder_returns200WithOrderBody() {
        Order order = OrderBuilder.anOrder()
                .withState(OrderState.APPROVED)
                .build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        given()
            .mockMvc(mockMvc)
        .when()
            .get("/orders/1")
        .then()
            .statusCode(200)
            .body("state", equalTo("APPROVED"));
    }
}
```

### Tooling
| Tool | Purpose |
|------|---------|
| `@WebMvcTest` | Loads only the web layer — fast |
| Rest-Assured `spring-mock-mvc` | Fluent API for request/response assertions |
| `@MockBean` | Isolates controller from service layer |

---

## 4. End-to-End Tests

### What to Test
- Full order lifecycle: create consumer → create restaurant → place order → accept → prepare → deliver
- Order revision and cancellation flows
- Cross-service data consistency

### How to Run
```bash
export DOCKER_HOST_IP=127.0.0.1
docker compose up -d
./gradlew :ftgo-end-to-end-tests:test
docker compose down -v
```

### Current Implementation
`EndToEndTests` extends `AbstractEndToEndTests` and exercises the complete
order lifecycle via REST API calls against the running application.

See `docs/testing-pipeline.md` for CI workflow details.

---

## 5. Contract Tests (Planned)

### Purpose
Verify API contracts between services without deploying the full stack.
Critical for the microservices migration where services communicate over HTTP/messaging.

### Approach Options

| Approach | Pros | Cons |
|----------|------|------|
| **Spring Cloud Contract** | Gradle plugin, auto-generated tests, Spring native | Tighter coupling to Spring |
| **Pact** | Language-agnostic, broker for contract sharing | Additional infrastructure (Pact Broker) |

### Recommended: Spring Cloud Contract

```java
// Consumer side — stub runner
@SpringBootTest
@AutoConfigureStubRunner(
    ids = "net.chrisrichardson.ftgo:order-service-api:+:stubs:8080",
    stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
class ConsumerServiceContractTest {

    @Test
    void getOrder_shouldMatchContract() {
        // calls against WireMock stub auto-generated from contract
    }
}
```

### Contract Location
```
services/order-service/order-service-api/
└── src/contractTest/resources/contracts/
    └── shouldReturnOrderById.groovy
```

---

## 6. Test Utilities — `ftgo-test-lib`

A shared library (`libs/ftgo-test-lib/`) providing reusable test infrastructure:

| Package | Contents |
|---------|----------|
| `testlib.builders` | Test data builders for all bounded contexts |
| `testlib.assertions` | Custom AssertJ assertions for domain objects |
| `testlib.config` | Shared Spring test configuration |
| `testlib.containers` | Testcontainers MySQL configuration |

### Usage
```groovy
// In any module's build.gradle
testImplementation project(":ftgo-test-lib")
```

See `libs/ftgo-test-lib/` for implementation details.

---

## 7. JUnit 4 → JUnit 5 Migration

See [docs/junit5-migration-guide.md](junit5-migration-guide.md) for the
complete migration guide covering annotation mappings, extension model,
parameterized tests, and assertion changes.

### Migration Status

| Module Category | JUnit Version | Migration Priority |
|-----------------|---------------|-------------------|
| Legacy monolith (`ftgo-*`) | JUnit 4.13.2 | Low — migrate when touching |
| Shared libraries (`libs/*`) | JUnit 5.10.2 | Done |
| New microservices (`services/*`) | JUnit 5.10.2 | Done |

### Strategy
- **New code** always uses JUnit 5 (enforced by `ftgo.testing-conventions`)
- **Legacy modules** migrate incrementally using `junit-vintage-engine` as a bridge
- **No big-bang migration** — convert tests when modifying existing modules

---

## 8. Testcontainers for Integration Tests

See [docs/testcontainers-guide.md](testcontainers-guide.md) for the complete guide
covering setup, shared containers, and CI configuration.

### Key Points
- Use `FtgoMySQLContainer` from `ftgo-test-lib` for consistent MySQL 5.7 configuration
- Containers start automatically via `@Testcontainers` + `@Container`
- Flyway migrations run via Spring Boot auto-configuration
- CI uses Docker-in-Docker; local dev uses Docker Desktop

---

## 9. CI Pipeline Integration

Three-tier pipeline defined in `.github/workflows/`:

| Workflow | Trigger | Tests Run |
|----------|---------|-----------|
| `ci-tests-unit.yml` | Push & PR | Unit tests (no external deps) |
| `ci-tests-integration.yml` | Push & PR | Integration tests (MySQL 5.7) |
| `ci-tests-e2e.yml` | Push & PR | Full stack E2E |

See [docs/testing-pipeline.md](testing-pipeline.md) for detailed pipeline documentation.

### Test Reporting
- JUnit XML reports uploaded as workflow artifacts
- JaCoCo coverage reports for shared libraries and microservices
- Coverage thresholds enforced via `ftgo.quality-conventions`

---

## 10. Best Practices

### Do
- Follow the testing pyramid — more unit tests, fewer E2E tests
- Use test data builders from `ftgo-test-lib` — avoid magic constants
- Test one behavior per test method
- Name tests descriptively: `methodName_condition_expectedResult`
- Use AssertJ for assertions — more readable than JUnit assertions
- Keep tests independent — no shared mutable state between tests
- Use `@Transactional` for integration tests to auto-rollback

### Don't
- Don't test framework behavior (Spring, Hibernate)
- Don't use `Thread.sleep()` in tests — use Awaitility for async assertions
- Don't share test data across test classes via static mutable fields
- Don't mock types you don't own — use integration tests instead
- Don't write tests that depend on execution order
- Don't chase 100% coverage — focus on business logic and branch coverage
