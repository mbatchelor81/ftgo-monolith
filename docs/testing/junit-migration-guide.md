# JUnit 4 → JUnit 5 Migration Guide

The FTGO microservices have standardized on **JUnit Jupiter 5** (see
[`docs/testing-strategy.md`](../testing-strategy.md) §3). This guide
describes the mechanical transformations required to migrate a legacy
JUnit 4 test suite (as used in the monolith's `ftgo-*` modules) to the
JUnit 5 patterns expected in the new `services/*` layout.

## Why migrate?

- JUnit 5 is the version shipped by the `junit-jupiter` bundle in
  [`gradle/libs.versions.toml`](../../gradle/libs.versions.toml) and
  wired in by `ftgo.testing-conventions`.
- Native support for parameterized tests, nested grouping, dependency
  injection into test methods, parallel execution, and Testcontainers'
  `@Testcontainers` + `@Container` extension model.
- Spring Boot 3.x drops JUnit 4 from the test starter by default; the
  `vintage` engine is available but discouraged.

## 1. Annotation mapping

| JUnit 4                              | JUnit 5                                 | Import                                              |
|--------------------------------------|-----------------------------------------|-----------------------------------------------------|
| `org.junit.Test`                     | `org.junit.jupiter.api.Test`            | `import org.junit.jupiter.api.Test;`                |
| `org.junit.Before`                   | `org.junit.jupiter.api.BeforeEach`      | `import org.junit.jupiter.api.BeforeEach;`          |
| `org.junit.After`                    | `org.junit.jupiter.api.AfterEach`       | `import org.junit.jupiter.api.AfterEach;`           |
| `org.junit.BeforeClass`              | `org.junit.jupiter.api.BeforeAll` (static)| `import org.junit.jupiter.api.BeforeAll;`         |
| `org.junit.AfterClass`               | `org.junit.jupiter.api.AfterAll` (static) | `import org.junit.jupiter.api.AfterAll;`          |
| `@Ignore`                            | `@Disabled`                             | `import org.junit.jupiter.api.Disabled;`            |
| `@Test(expected = X.class)`          | `assertThrows(X.class, () -> …)`        | `org.junit.jupiter.api.Assertions.assertThrows`     |
| `@Test(timeout = 1000L)`             | `@Timeout(1)` on method or `assertTimeout(…)` | `import org.junit.jupiter.api.Timeout;`       |
| `@RunWith(MockitoJUnitRunner.class)` | `@ExtendWith(MockitoExtension.class)`   | `import org.mockito.junit.jupiter.MockitoExtension;`|
| `@RunWith(SpringJUnit4ClassRunner)`  | `@ExtendWith(SpringExtension.class)` (usually implicit via `@SpringBootTest`) | — |
| `@RunWith(Parameterized.class)`      | `@ParameterizedTest` + `@MethodSource`  | `import org.junit.jupiter.params.ParameterizedTest;`|

## 2. Visibility

JUnit 5 no longer requires test classes or methods to be `public`.
Prefer **package-private** visibility throughout — it removes
boilerplate and signals intent ("these classes aren't API").

```java
// JUnit 4
public class OrderServiceTest {
    @Test public void shouldCreateOrder() { ... }
}

// JUnit 5
class OrderServiceTest {
    @Test
    void createOrder_withValidRequest_returnsCreatedOrder() { ... }
}
```

## 3. Assertions

Prefer **AssertJ** over the built-in `org.junit.jupiter.api.Assertions`
— AssertJ is already on the classpath through
`ftgo.testing-conventions` and gives far richer failure messages.

```java
// Legacy JUnit 4 + Hamcrest
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;

assertThat(order.getTotal(), equalTo(new Money("12.50")));

// JUnit 5 + AssertJ
import static org.assertj.core.api.Assertions.assertThat;

assertThat(order.getTotal()).isEqualTo(new Money("12.50"));
```

For `Money`, use the custom AssertJ extension shipped in
`libs/ftgo-test-util`:

```java
import static com.ftgo.test.assertions.MoneyAssert.assertThat;

assertThat(order.getOrderTotal()).isEqualToAmount("62.50");
```

## 4. Exception assertions

```java
// JUnit 4
@Test(expected = OrderMinimumNotMetException.class)
public void shouldRejectSmallOrder() { orderService.create(smallOrder); }

// JUnit 5 — AssertJ
@Test
void create_belowMinimum_throws() {
    assertThatThrownBy(() -> orderService.create(smallOrder))
        .isInstanceOf(OrderMinimumNotMetException.class)
        .hasMessageContaining("minimum");
}
```

AssertJ's `assertThatThrownBy` also supports chaining `.extracting(…)`
for field-level assertions on the exception without unwrapping.

## 5. Parameterized tests

```java
// JUnit 4
@RunWith(Parameterized.class)
public class QuantityValidationTest {
    @Parameterized.Parameters
    public static Collection<Object[]> data() { ... }
    public QuantityValidationTest(int qty, boolean valid) { ... }
    @Test public void validate() { ... }
}

// JUnit 5
class QuantityValidationTest {
    @ParameterizedTest(name = "quantity {0} → {1}")
    @CsvSource({
        "1, true",
        "0, false",
        "-1, false",
        "1000000, false"
    })
    void validate(int quantity, boolean valid) { ... }
}
```

## 6. Mockito

```java
// JUnit 4 style
@RunWith(MockitoJUnitRunner.class)
public class OrderServiceTest {
    @Mock OrderRepository orderRepository;
    @InjectMocks OrderService orderService;
}

// JUnit 5
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock OrderRepository orderRepository;
    @InjectMocks OrderService orderService;
}
```

Notes:
- `MockitoExtension` enforces strict stubbing by default — unused
  stubs become test failures. Migrate any `when().thenReturn()` that
  is conditionally unused behind `Mockito.lenient()` rather than
  switching the strictness back to `LENIENT`.
- Constructor injection works out of the box: declare
  `@InjectMocks` on the **field**, not the constructor.

## 7. Spring Boot tests

```java
// JUnit 4
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class OrderServiceIntegrationTest { ... }

// JUnit 5
@SpringBootTest(webEnvironment = RANDOM_PORT)
class OrderServiceIntegrationTest extends AbstractIntegrationTest { ... }
```

Spring Boot's test starter already bundles `SpringExtension` — an
explicit `@ExtendWith(SpringExtension.class)` is redundant.

## 8. Rules → Extensions

JUnit 4 `@Rule` / `@ClassRule` fields don't exist in Jupiter. The
most common replacements:

| JUnit 4 rule                      | JUnit 5 equivalent                                                   |
|-----------------------------------|----------------------------------------------------------------------|
| `TemporaryFolder`                 | `@TempDir Path path` on test method/field                            |
| `ExpectedException`               | `assertThrows` / `assertThatThrownBy`                                |
| `TestName`                        | `TestInfo testInfo` parameter                                        |
| `org.testcontainers.containers.*` | `@Testcontainers` class-level + `@Container` field-level extensions  |

## 9. Assumptions

```java
// JUnit 4
import static org.junit.Assume.assumeTrue;

// JUnit 5
import static org.junit.jupiter.api.Assumptions.assumeTrue;
```

Same semantics, different package.

## 10. Migration workflow

1. **Change dependencies.** Confirm the module applies
   `ftgo.testing-conventions` (the version catalog's `junit-jupiter`
   bundle replaces JUnit 4 entirely).
2. **Update imports.** The annotation mapping in §1 is 90 % of the
   migration.
3. **Remove `@RunWith(...)`.** Replace with `@ExtendWith(...)` if
   non-Spring, or leave empty for Spring Boot tests.
4. **Rename tests.** `shouldXYZ()` → `action_condition_expectedResult`.
5. **Drop `public` modifiers** on classes and test methods.
6. **Switch assertions to AssertJ.**
7. **Port `@Test(expected = …)`** to `assertThatThrownBy`.
8. **Run the tests.** Gradle 8 + JUnit 5 shows failing stack traces
   with clear markers when a vintage JUnit 4 class is still on the
   classpath.
9. **Delete the vintage dependency.** If the module had
   `junit:junit:4.x`, remove it — the convention plugin no longer
   pulls it in and leaving it risks mixed engines.

## 11. Example: full before / after

### Before (JUnit 4 + Hamcrest + Mockito runner)

```java
@RunWith(MockitoJUnitRunner.class)
public class OrderControllerTest {

    @Mock private OrderService orderService;
    @Mock private OrderRepository orderRepository;

    private OrderController controller;

    @Before
    public void setUp() {
        controller = new OrderController(orderService, orderRepository);
    }

    @Test
    public void shouldReturn404WhenOrderMissing() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        given().standaloneSetup(controller)
            .when().get("/orders/1")
            .then().statusCode(404);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectNegativeId() {
        controller.find(-1L);
    }
}
```

### After (JUnit 5 + AssertJ + MockitoExtension)

```java
@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock private OrderService orderService;
    @Mock private OrderRepository orderRepository;

    private OrderController controller;

    @BeforeEach
    void setUp() {
        controller = new OrderController(orderService, orderRepository);
    }

    @Test
    void findOrder_whenMissing_returns404() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        given().standaloneSetup(controller)
            .when().get("/orders/1")
            .then().statusCode(404);
    }

    @Test
    void findOrder_withNegativeId_rejects() {
        assertThatThrownBy(() -> controller.find(-1L))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
```
