# JUnit 5 Migration Guide

> **EM-48** — Guide for migrating FTGO tests from JUnit 4 to JUnit 5 (Jupiter).

## Table of Contents

- [Overview](#overview)
- [Key Differences](#key-differences)
- [Annotation Mapping](#annotation-mapping)
- [Assertion Migration](#assertion-migration)
- [Extension Model](#extension-model)
- [Test Lifecycle](#test-lifecycle)
- [Parameterized Tests](#parameterized-tests)
- [Nested Tests](#nested-tests)
- [Display Names](#display-names)
- [Assumptions](#assumptions)
- [Timeout Handling](#timeout-handling)
- [Migration Checklist](#migration-checklist)

---

## Overview

The FTGO microservices use **JUnit 5 (Jupiter)** as the test framework. The legacy monolith
modules use JUnit 4. This guide covers the key changes needed when migrating tests.

### Dependencies

JUnit 5 is configured automatically by the `ftgo.testing-conventions` Gradle plugin:

```groovy
// No manual dependency needed — already provided by the convention plugin
plugins {
    id 'ftgo.testing-conventions'
}
```

The version catalog (`gradle/libs.versions.toml`) pins JUnit Jupiter at `5.10.2`.

---

## Key Differences

| Feature | JUnit 4 | JUnit 5 |
|---------|---------|---------|
| Package | `org.junit` | `org.junit.jupiter.api` |
| Test annotation | `@Test` | `@Test` (different import) |
| Lifecycle | `@Before` / `@After` | `@BeforeEach` / `@AfterEach` |
| Class lifecycle | `@BeforeClass` / `@AfterClass` | `@BeforeAll` / `@AfterAll` |
| Ignore test | `@Ignore` | `@Disabled` |
| Expected exception | `@Test(expected=...)` | `assertThrows()` |
| Timeout | `@Test(timeout=...)` | `@Timeout` annotation |
| Runner | `@RunWith` | `@ExtendWith` |
| Rules | `@Rule` / `@ClassRule` | Extensions |
| Assertions class | `org.junit.Assert` | `org.junit.jupiter.api.Assertions` |
| Visibility | `public` required | Package-private allowed |

---

## Annotation Mapping

### Before (JUnit 4)

```java
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.runner.RunWith;
```

### After (JUnit 5)

```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;
```

### Side-by-side Example

```java
// JUnit 4
@RunWith(MockitoJUnitRunner.class)
public class OrderServiceTest {

    @Before
    public void setUp() { /* ... */ }

    @Test
    public void testCreateOrder() { /* ... */ }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidOrder() { /* ... */ }

    @Ignore("Not yet implemented")
    @Test
    public void testReviseOrder() { /* ... */ }
}

// JUnit 5
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @BeforeEach
    void setUp() { /* ... */ }

    @Test
    void createOrder_withValidInput_returnsOrder() { /* ... */ }

    @Test
    void createOrder_withInvalidInput_throwsException() {
        assertThrows(IllegalArgumentException.class,
            () -> orderService.createOrder(invalidRequest));
    }

    @Disabled("Not yet implemented")
    @Test
    void reviseOrder_updatesLineItems() { /* ... */ }
}
```

---

## Assertion Migration

### JUnit 4 → JUnit 5 Assertions

```java
// JUnit 4
import static org.junit.Assert.*;

assertEquals("expected", actual);
assertTrue(condition);
assertNotNull(object);
assertArrayEquals(expected, actual);

// JUnit 5
import static org.junit.jupiter.api.Assertions.*;

assertEquals("expected", actual);         // same
assertTrue(condition);                     // same
assertNotNull(object);                     // same
assertArrayEquals(expected, actual);       // same

// NEW: message is now the LAST parameter (not first)
assertEquals("expected", actual, "Order should be approved");
// JUnit 4: assertEquals("Order should be approved", "expected", actual);
```

### Recommended: Use AssertJ Instead

We recommend **AssertJ** over JUnit 5 assertions for better readability:

```java
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Fluent, readable assertions
assertThat(order.getOrderState()).isEqualTo(OrderState.APPROVED);
assertThat(orders).hasSize(3).extracting("id").containsExactly(1L, 2L, 3L);
assertThatThrownBy(() -> order.cancel())
    .isInstanceOf(UnsupportedStateTransitionException.class);
```

### FTGO Custom Assertions

Use the custom assertions from `ftgo-test-lib`:

```java
import static com.ftgo.testlib.assertion.FtgoAssertions.assertThat;

assertThat(order).isApproved().belongsToConsumer(1L);
assertThat(money).hasAmount("25.98").isNotZero();
```

---

## Extension Model

JUnit 5 replaces `@RunWith` and `@Rule` with a unified **Extension** model.

### Common Extensions

| JUnit 4 | JUnit 5 |
|---------|---------|
| `@RunWith(MockitoJUnitRunner.class)` | `@ExtendWith(MockitoExtension.class)` |
| `@RunWith(SpringRunner.class)` | `@ExtendWith(SpringExtension.class)` |
| `@Rule TemporaryFolder` | `@TempDir` annotation |
| `@Rule ExpectedException` | `assertThrows()` |
| `@Rule Timeout` | `@Timeout` annotation |

### Mockito Extension

```java
// JUnit 4
@RunWith(MockitoJUnitRunner.class)
public class OrderServiceTest {
    @Mock private OrderRepository repo;
    @InjectMocks private OrderService service;
}

// JUnit 5
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock private OrderRepository repo;
    @InjectMocks private OrderService service;
}
```

### Spring Extension

```java
// JUnit 4
@RunWith(SpringRunner.class)
@SpringBootTest
public class IntegrationTest { }

// JUnit 5 — @SpringBootTest already includes @ExtendWith(SpringExtension.class)
@SpringBootTest
class IntegrationTest { }
```

### Temporary Directory

```java
// JUnit 4
@Rule
public TemporaryFolder tempFolder = new TemporaryFolder();

@Test
public void testFileWrite() throws IOException {
    File file = tempFolder.newFile("test.txt");
}

// JUnit 5
@Test
void testFileWrite(@TempDir Path tempDir) throws IOException {
    Path file = tempDir.resolve("test.txt");
    Files.writeString(file, "content");
}
```

---

## Test Lifecycle

### Default Lifecycle (Per-Method)

By default, JUnit 5 creates a new test instance for each test method (same as JUnit 4):

```java
class OrderTest {
    private Order order = order().build(); // fresh instance per test

    @Test
    void cancel_transitionsToCancelled() {
        order.cancel();
        assertThat(order).isCancelled();
    }
}
```

### Per-Class Lifecycle

For expensive setup, use `@TestInstance(Lifecycle.PER_CLASS)`:

```java
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ExpensiveSetupTest {

    @BeforeAll
    void setUp() {
        // Runs once; can be non-static
    }
}
```

---

## Parameterized Tests

### JUnit 4 Parameterized Runner → JUnit 5 @ParameterizedTest

```java
// JUnit 4
@RunWith(Parameterized.class)
public class MoneyTest {
    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { {"10", "20", "30"}, {"0", "0", "0"} });
    }
    // constructor, fields, test method...
}

// JUnit 5
class MoneyTest {

    @ParameterizedTest
    @CsvSource({
        "10, 20, 30",
        "0,  0,  0",
        "5,  -3, 2"
    })
    void add_returnsSumOfAmounts(String a, String b, String expected) {
        Money result = new Money(a).add(new Money(b));
        assertThat(result).isEqualTo(new Money(expected));
    }

    @ParameterizedTest
    @EnumSource(OrderState.class)
    void orderState_hasValidName(OrderState state) {
        assertThat(state.name()).isNotBlank();
    }

    @ParameterizedTest
    @MethodSource("invalidOrderInputs")
    void createOrder_withInvalidInput_throwsException(long consumerId, String reason) {
        assertThatThrownBy(() -> order().withConsumerId(consumerId).build())
            .isInstanceOf(IllegalArgumentException.class);
    }

    static Stream<Arguments> invalidOrderInputs() {
        return Stream.of(
            Arguments.of(-1L, "negative consumer ID"),
            Arguments.of(0L, "zero consumer ID")
        );
    }
}
```

---

## Nested Tests

JUnit 5 supports `@Nested` for grouping related tests:

```java
@DisplayName("Order Domain")
class OrderDomainTest extends BaseUnitTest {

    @Nested
    @DisplayName("Order creation")
    class Creation {
        @Test
        void withValidInput_isApproved() { /* ... */ }
    }

    @Nested
    @DisplayName("Order cancellation")
    class Cancellation {
        @Test
        void whenApproved_transitionsToCancelled() { /* ... */ }

        @Test
        void whenNotApproved_throwsException() { /* ... */ }
    }

    @Nested
    @DisplayName("Order lifecycle")
    class Lifecycle {
        @Test
        void fullLifecycle_fromApprovedToDelivered() { /* ... */ }
    }
}
```

---

## Display Names

Use `@DisplayName` for human-readable test names in reports:

```java
@DisplayName("Order Service")
class OrderServiceTest {

    @Test
    @DisplayName("should create order with valid consumer and restaurant")
    void createOrder_withValidInput_returnsOrder() { /* ... */ }

    @Test
    @DisplayName("should reject order when consumer validation fails")
    void createOrder_invalidConsumer_throwsException() { /* ... */ }
}
```

---

## Assumptions

```java
// JUnit 4
import static org.junit.Assume.assumeTrue;

// JUnit 5
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@Test
void testOnlyInCi() {
    assumeTrue(System.getenv("CI") != null, "Skipped outside CI");
    // test logic
}
```

---

## Timeout Handling

```java
// JUnit 4
@Test(timeout = 5000)
public void testWithTimeout() { /* ... */ }

// JUnit 5
@Test
@Timeout(value = 5, unit = TimeUnit.SECONDS)
void testWithTimeout() { /* ... */ }

// Or programmatically
@Test
void testWithAssertTimeout() {
    assertTimeout(Duration.ofSeconds(5), () -> {
        // code that should complete within 5 seconds
    });
}
```

---

## Migration Checklist

Use this checklist when migrating a test class from JUnit 4 to JUnit 5:

- [ ] **Update imports:** Replace `org.junit` with `org.junit.jupiter.api`
- [ ] **Replace annotations:**
  - `@Before` → `@BeforeEach`
  - `@After` → `@AfterEach`
  - `@BeforeClass` → `@BeforeAll`
  - `@AfterClass` → `@AfterAll`
  - `@Ignore` → `@Disabled`
  - `@RunWith` → `@ExtendWith`
- [ ] **Update test visibility:** Remove `public` from test classes and methods
- [ ] **Replace expected exceptions:** `@Test(expected=X)` → `assertThrows(X.class, ...)`
- [ ] **Replace timeout:** `@Test(timeout=N)` → `@Timeout(N)`
- [ ] **Replace Rules:** `@Rule` / `@ClassRule` → Extensions or `@TempDir`
- [ ] **Update assertion message order:** Message is now the last parameter
- [ ] **Consider AssertJ:** Replace `assertEquals` etc. with `assertThat(...).isEqualTo(...)`
- [ ] **Use test builders:** Replace inline object construction with `TestBuilders`
- [ ] **Use custom assertions:** Replace generic assertions with `FtgoAssertions`
- [ ] **Add `@DisplayName`:** Add readable test descriptions
- [ ] **Consider `@Nested`:** Group related tests into inner classes
- [ ] **Extend base class:** Use `BaseUnitTest`, `BaseIntegrationTest`, etc.
- [ ] **Rename test methods:** Follow `methodName_condition_expectedResult` convention
- [ ] **Verify:** Run `./gradlew test` and confirm all tests pass
