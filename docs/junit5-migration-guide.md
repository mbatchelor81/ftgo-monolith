# JUnit 4 → JUnit 5 Migration Guide

Step-by-step guide for migrating FTGO legacy modules from JUnit 4 to JUnit 5.

---

## Overview

| Aspect | JUnit 4 | JUnit 5 |
|--------|---------|---------|
| Package | `org.junit` | `org.junit.jupiter.api` |
| Test annotation | `@Test` | `@Test` (different import) |
| Lifecycle | `@Before` / `@After` | `@BeforeEach` / `@AfterEach` |
| Class lifecycle | `@BeforeClass` / `@AfterClass` | `@BeforeAll` / `@AfterAll` |
| Runner | `@RunWith` | `@ExtendWith` |
| Rules | `@Rule` / `@ClassRule` | Extensions (`@ExtendWith`, `@RegisterExtension`) |
| Assumptions | `org.junit.Assume` | `org.junit.jupiter.api.Assumptions` |
| Assertions | `org.junit.Assert` | `org.junit.jupiter.api.Assertions` (or AssertJ) |

---

## Migration Strategy

### Phase 1: Add JUnit 5 Alongside JUnit 4 (Bridge)

Add the JUnit Vintage engine to run JUnit 4 tests on the JUnit 5 platform:

```groovy
// build.gradle for a legacy module
dependencies {
    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.2'
    testRuntimeOnly 'org.junit.vintage:junit-vintage-engine:5.10.2'
    testImplementation 'org.mockito:mockito-junit-jupiter:5.11.0'
}

test {
    useJUnitPlatform()
}
```

This allows JUnit 4 and JUnit 5 tests to coexist in the same module.

### Phase 2: Convert Tests Incrementally

Convert tests one at a time when modifying existing modules. No big-bang migration.

### Phase 3: Remove Vintage Engine

Once all tests in a module are converted, remove `junit-vintage-engine` and
the JUnit 4 dependency.

---

## Annotation Mapping

### Test Lifecycle

```java
// JUnit 4
import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;

public class OrderServiceTest {
    @BeforeClass
    public static void setUpClass() { }

    @Before
    public void setUp() { }

    @Test
    public void shouldCreateOrder() { }

    @After
    public void tearDown() { }

    @AfterClass
    public static void tearDownClass() { }
}

// JUnit 5
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

class OrderServiceTest {
    @BeforeAll
    static void setUpClass() { }

    @BeforeEach
    void setUp() { }

    @Test
    void shouldCreateOrder() { }

    @AfterEach
    void tearDown() { }

    @AfterAll
    static void tearDownClass() { }
}
```

**Key differences:**
- JUnit 5 test classes and methods can be package-private (no `public` required)
- `@BeforeClass` → `@BeforeAll`, `@Before` → `@BeforeEach`
- `@AfterClass` → `@AfterAll`, `@After` → `@AfterEach`

### Expected Exceptions

```java
// JUnit 4
@Test(expected = OrderMinimumNotMetException.class)
public void shouldThrowWhenMinimumNotMet() {
    order.noteApproved();
}

// JUnit 5
@Test
void shouldThrowWhenMinimumNotMet() {
    assertThrows(OrderMinimumNotMetException.class, () -> {
        order.noteApproved();
    });
}

// AssertJ (recommended)
@Test
void shouldThrowWhenMinimumNotMet() {
    assertThatThrownBy(() -> order.noteApproved())
        .isInstanceOf(OrderMinimumNotMetException.class);
}
```

### Test Timeout

```java
// JUnit 4
@Test(timeout = 5000)
public void shouldCompleteWithinTimeout() { }

// JUnit 5
@Test
@Timeout(value = 5, unit = TimeUnit.SECONDS)
void shouldCompleteWithinTimeout() { }
```

### Disabling Tests

```java
// JUnit 4
@Ignore("Not yet implemented")
@Test
public void shouldHandleRevision() { }

// JUnit 5
@Disabled("Not yet implemented")
@Test
void shouldHandleRevision() { }
```

---

## Runner → Extension Migration

### Mockito

```java
// JUnit 4
@RunWith(MockitoJUnitRunner.class)
public class OrderServiceTest {
    @Mock private OrderRepository orderRepository;
    // ...
}

// JUnit 5
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock private OrderRepository orderRepository;
    // ...
}
```

### Spring

```java
// JUnit 4
@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationIntegrationTest { }

// JUnit 5 — @SpringBootTest already includes @ExtendWith(SpringExtension.class)
@SpringBootTest
class ApplicationIntegrationTest { }
```

---

## Rules → Extensions

### TemporaryFolder

```java
// JUnit 4
@Rule
public TemporaryFolder tempFolder = new TemporaryFolder();

@Test
public void shouldWriteToFile() throws Exception {
    File file = tempFolder.newFile("test.txt");
}

// JUnit 5
@TempDir
Path tempDir;

@Test
void shouldWriteToFile() {
    Path file = tempDir.resolve("test.txt");
}
```

### ExpectedException

```java
// JUnit 4
@Rule
public ExpectedException thrown = ExpectedException.none();

@Test
public void shouldThrow() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("invalid");
    service.process(null);
}

// JUnit 5
@Test
void shouldThrow() {
    IllegalArgumentException ex = assertThrows(
        IllegalArgumentException.class,
        () -> service.process(null)
    );
    assertThat(ex.getMessage()).contains("invalid");
}
```

---

## Assertions Migration

### JUnit Assert → AssertJ (Recommended)

```java
// JUnit 4 Assert
import static org.junit.Assert.*;

assertEquals(expected, actual);
assertTrue(condition);
assertNotNull(value);
assertNull(value);

// AssertJ (recommended for JUnit 5)
import static org.assertj.core.api.Assertions.*;

assertThat(actual).isEqualTo(expected);
assertThat(condition).isTrue();
assertThat(value).isNotNull();
assertThat(value).isNull();

// AssertJ collection assertions
assertThat(orders)
    .hasSize(3)
    .extracting(Order::getOrderState)
    .containsExactly(APPROVED, PREPARING, DELIVERED);
```

### Hamcrest → AssertJ

```java
// Hamcrest (JUnit 4 style)
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

assertThat(order.getState(), equalTo(OrderState.APPROVED));
assertThat(items, hasSize(3));

// AssertJ (JUnit 5 style)
assertThat(order.getOrderState()).isEqualTo(OrderState.APPROVED);
assertThat(items).hasSize(3);
```

---

## Parameterized Tests

```java
// JUnit 4
@RunWith(Parameterized.class)
public class MoneyTest {
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { "10.00", "5.00", "15.00" },
            { "0.00", "0.00", "0.00" }
        });
    }

    private final String a, b, expected;

    public MoneyTest(String a, String b, String expected) {
        this.a = a; this.b = b; this.expected = expected;
    }

    @Test
    public void shouldAdd() {
        assertEquals(new Money(expected), new Money(a).add(new Money(b)));
    }
}

// JUnit 5
class MoneyTest {
    @ParameterizedTest
    @CsvSource({
        "10.00, 5.00, 15.00",
        "0.00, 0.00, 0.00"
    })
    void shouldAdd(String a, String b, String expected) {
        assertThat(new Money(a).add(new Money(b))).isEqualTo(new Money(expected));
    }
}
```

---

## FTGO-Specific Migration Examples

### OrderControllerTest (Current JUnit 4)

```java
// Before (JUnit 4) — ftgo-order-service
public class OrderControllerTest {
    @Before
    public void setUp() throws Exception {
        orderService = mock(OrderService.class);
        // ...
    }

    @Test
    public void shouldFindOrder() {
        when(orderRepository.findById(1L))
            .thenReturn(Optional.of(CHICKEN_VINDALOO_ORDER));
        // ...
    }
}

// After (JUnit 5) — using ftgo-test-lib builders
@ExtendWith(MockitoExtension.class)
class OrderControllerTest {
    @Mock private OrderService orderService;
    @Mock private OrderRepository orderRepository;
    @InjectMocks private OrderController orderController;

    @Test
    void getOrder_existingOrder_returns200() {
        Order order = OrderBuilder.anOrder()
            .withId(99L)
            .withState(OrderState.APPROVED)
            .build();
        when(orderRepository.findById(99L)).thenReturn(Optional.of(order));
        // ...
    }
}
```

### RestaurantMother → RestaurantBuilder

```java
// Before (Mother object pattern)
public class RestaurantMother {
    public static final String AJANTA_RESTAURANT_NAME = "Ajanta";
    public static final long AJANTA_ID = 1L;
    public static final Restaurant AJANTA_RESTAURANT = new Restaurant(/* ... */);
}

// After (Builder pattern from ftgo-test-lib)
Restaurant restaurant = RestaurantBuilder.aRestaurant()
    .withName("Ajanta")
    .withMenuItem("1", "Chicken Vindaloo", new Money("12.34"))
    .build();
```

---

## Migration Checklist

For each module being migrated:

- [ ] Add JUnit 5 + Vintage engine dependencies
- [ ] Add `test { useJUnitPlatform() }` to `build.gradle`
- [ ] Convert `@RunWith` → `@ExtendWith`
- [ ] Convert `@Before`/`@After` → `@BeforeEach`/`@AfterEach`
- [ ] Convert `@BeforeClass`/`@AfterClass` → `@BeforeAll`/`@AfterAll`
- [ ] Replace `@Test(expected=...)` → `assertThrows` or `assertThatThrownBy`
- [ ] Replace `@Ignore` → `@Disabled`
- [ ] Migrate `@Rule`/`@ClassRule` → extensions
- [ ] Replace JUnit Assert/Hamcrest → AssertJ
- [ ] Replace Mother objects → builders from `ftgo-test-lib`
- [ ] Remove JUnit 4 and Vintage engine dependencies
- [ ] Verify all tests pass: `./gradlew :module:test`
