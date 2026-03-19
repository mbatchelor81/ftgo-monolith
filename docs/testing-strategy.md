# FTGO Testing Strategy

## Testing Pyramid

```
        ╱  E2E  ╲          Few — full Docker Compose stack, critical paths only
       ╱─────────╲
      ╱Integration╲        Some — Spring context + Testcontainers (MySQL)
     ╱─────────────╲
    ╱  Unit Tests   ╲      Many — fast, isolated, JUnit 5 + Mockito
   ╱─────────────────╲
```

## Test Tiers

### Tier 1: Unit Tests

| Attribute | Value |
|-----------|-------|
| **Scope** | Single class/method in isolation |
| **Speed** | < 100ms per test |
| **Coverage Target** | 70% line coverage minimum, 80%+ recommended |
| **Dependencies** | Mocked via Mockito |
| **Location** | `src/test/java/` |
| **Run Command** | `./gradlew :module:test` |

**What to test:**
- Business logic in `@Service` classes
- Domain entity methods (state transitions, validations)
- DTO mapping/transformation logic
- Input validation and error handling
- Utility functions

**What NOT to test:**
- Framework boilerplate (getters/setters, auto-generated code)
- Spring configuration classes
- Trivial delegation methods

**Example:**
```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock private OrderRepository orderRepository;
    @Mock private ConsumerService consumerService;
    @InjectMocks private OrderService orderService;

    @Test
    void createOrder_withValidRequest_returnsApprovedOrder() {
        // Arrange
        var request = OrderBuilder.anOrder().build();
        when(orderRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // Act
        var result = orderService.createOrder(request);

        // Assert
        assertThat(result.getState()).isEqualTo(OrderState.APPROVED);
    }
}
```

### Tier 2: Integration Tests

| Attribute | Value |
|-----------|-------|
| **Scope** | Spring context + real database (Testcontainers) |
| **Speed** | 1-10 seconds per test |
| **Coverage Target** | All API endpoints, all repository queries |
| **Dependencies** | Testcontainers MySQL, Spring Boot test context |
| **Location** | `src/integrationTest/java/` |
| **Run Command** | `./gradlew :module:integrationTest` |

**What to test:**
- REST API endpoint contracts (request/response shapes, status codes)
- JPA repository queries (custom queries, pagination)
- Database migration compatibility
- Spring Security authorization rules
- Transaction boundaries

**Example:**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class OrderControllerIntegrationTest {
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("ftgo_test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Autowired private TestRestTemplate restTemplate;

    @Test
    void createOrder_returns201() {
        var request = new CreateOrderRequest(1L, 1L, List.of(...));
        var response = restTemplate.postForEntity("/orders", request, OrderResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }
}
```

### Tier 3: Contract Tests

| Attribute | Value |
|-----------|-------|
| **Scope** | Service-to-service API contract verification |
| **Speed** | 1-5 seconds per test |
| **Framework** | Spring Cloud Contract or Pact |
| **Location** | `src/contractTest/java/` |
| **Run Command** | `./gradlew :module:contractTest` |

**When to use:**
- Verifying that a consumer service's expectations match the provider's API
- Preventing breaking API changes in shared service contracts
- Testing API versioning and backward compatibility

### Tier 4: End-to-End Tests

| Attribute | Value |
|-----------|-------|
| **Scope** | Full application stack via Docker Compose |
| **Speed** | 10-60 seconds per test |
| **Coverage Target** | Critical user journeys only |
| **Dependencies** | Docker Compose, all services running |
| **Location** | `ftgo-end-to-end-tests/` |
| **Run Command** | `./run-end-to-end-tests.sh` |

**What to test:**
- Complete order lifecycle (create → accept → prepare → pickup → deliver)
- Cross-service workflows (consumer validation → order creation)
- Error scenarios spanning multiple services

## JUnit 5 Migration Guide

### From JUnit 4 to JUnit 5

| JUnit 4 | JUnit 5 | Notes |
|---------|---------|-------|
| `@Test` (org.junit) | `@Test` (org.junit.jupiter.api) | Different package |
| `@Before` | `@BeforeEach` | Runs before each test |
| `@After` | `@AfterEach` | Runs after each test |
| `@BeforeClass` | `@BeforeAll` | Must be static |
| `@Ignore` | `@Disabled` | Skip test |
| `@RunWith(MockitoJUnitRunner.class)` | `@ExtendWith(MockitoExtension.class)` | Mockito integration |
| `@RunWith(SpringRunner.class)` | `@ExtendWith(SpringExtension.class)` | Included in `@SpringBootTest` |
| `@Rule ExpectedException` | `assertThrows()` | Lambda-based |
| `Assert.assertEquals()` | `assertThat().isEqualTo()` | Use AssertJ |

### Common Patterns

```java
// Exception testing
assertThatThrownBy(() -> service.process(null))
    .isInstanceOf(IllegalArgumentException.class)
    .hasMessageContaining("must not be null");

// Parameterized tests
@ParameterizedTest
@ValueSource(strings = {"", " ", "  "})
void rejectsBlankInput(String input) {
    assertThatThrownBy(() -> service.validate(input))
        .isInstanceOf(ValidationException.class);
}

// Nested test classes for grouping
@Nested
class WhenOrderIsApproved {
    @Test void canBeCancelled() { ... }
    @Test void canBeAccepted() { ... }
    @Test void canBeRevised() { ... }
}
```

## Testcontainers Setup

### MySQL Configuration

```java
@Testcontainers
public abstract class AbstractIntegrationTest {
    @Container
    protected static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("ftgo_test")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("db/migration/V1__create_ftgo_db.sql");

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.flyway.url", MYSQL::getJdbcUrl);
        registry.add("spring.flyway.user", MYSQL::getUsername);
        registry.add("spring.flyway.password", MYSQL::getPassword);
    }
}
```

## CI Pipeline Integration

| Tier | Trigger | Timeout | Failure Policy |
|------|---------|---------|----------------|
| Unit Tests | Every push | 5 min | Block merge |
| Integration Tests | PR creation | 10 min | Block merge |
| Contract Tests | PR creation | 5 min | Block merge |
| E2E Tests | Merge to main | 15 min | Alert team |

## Test Data Management

- Use **builder pattern** for test data (see `ftgo-test-lib`)
- Each test creates its own data — no shared fixtures
- Use `@Transactional` + rollback for database test isolation
- Factories produce minimal objects — only set fields relevant to the test
