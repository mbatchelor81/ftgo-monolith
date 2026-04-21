# FTGO Microservices Testing Strategy

> **Status:** Normative. Every new service, library, and platform component
> must follow the testing pyramid and tier guidelines below. Deviations
> require an ADR in [`docs/adr/`](adr/).

This document is the single source of truth for how FTGO microservices
are tested during and after the migration from the Spring Boot monolith
(EM-48). It complements:

- [`docs/testing/junit-migration-guide.md`](testing/junit-migration-guide.md) — JUnit 4 → 5 migration patterns.
- [`docs/testing/contract-testing.md`](testing/contract-testing.md) — Spring Cloud Contract setup for inter-service APIs.
- [`docs/testing/when-to-write-which-test.md`](testing/when-to-write-which-test.md) — Decision tree for picking the right tier.
- [`templates/test-templates/`](../templates/test-templates/) — Copy-ready Java templates for every tier.
- [`libs/ftgo-test-util/`](../libs/ftgo-test-util/) — Shared builders, assertions, and Testcontainers helpers.

---

## 1. The Testing Pyramid

```
                   ╱╲
                  ╱E2╲           Few   — 10s+ wall-clock, full stack
                 ╱────╲                  (nightly / on merge)
                ╱ Ctr. ╲          Some  — seconds, service boundaries
               ╱────────╲                (PR gate)
              ╱ Integr.  ╲        More  — seconds, DB + Spring context
             ╱────────────╲              (PR gate)
            ╱    Unit       ╲     Many  — ms, pure logic
           ╱──────────────────╲          (every push, <2min total)
```

| Tier            | Scope                                | Typical speed          | Spring context? | External deps              |
|-----------------|--------------------------------------|------------------------|-----------------|----------------------------|
| **Unit**        | Single class / method                | milliseconds           | No              | None (mock or stub)        |
| **Integration** | Class + Spring + real datastore      | seconds                | Yes (sliced)    | MySQL via Testcontainers   |
| **Contract**    | HTTP/API contract between services   | seconds                | Yes (WebMvc)    | Stub runner (WireMock)     |
| **API**         | Full service running over HTTP       | seconds                | Yes (full)      | Testcontainers + HTTP      |
| **E2E**         | Multiple services + gateway          | 10s+                   | All services    | Full docker-compose stack  |

**Rule of thumb:** Prefer the cheapest tier that can exercise the
behaviour you care about. Every higher tier costs ~10× more in wall
clock time and ~10× more in flakiness risk.

## 2. Coverage Expectations

- **Unit tests — 70 % of total coverage.** Line coverage target is
  **80 %+** on business logic / services and **60 %+** overall. Fast
  feedback comes from here; the full unit suite **must** complete in
  **< 2 minutes** across all modules.
- **Integration tests** cover repository queries, Spring wiring, and
  security rules. Run on every PR via the `integration-tests` job in
  [`.github/workflows/test-pipeline.yml`](../.github/workflows/test-pipeline.yml).
- **Contract tests** guard every public REST surface that at least one
  other service consumes. See [`contract-testing.md`](testing/contract-testing.md).
- **API tests** (Rest-Assured) cover service-level golden paths.
- **E2E tests** are reserved for critical user journeys (create order,
  consumer sign-up, courier assignment). Nightly only.

## 3. Unit Tests

### Stack
- JUnit Jupiter 5 (`@Test`, `@ParameterizedTest`, `@Nested`).
- Mockito 5 (`@Mock`, `@InjectMocks`, `MockitoExtension`).
- AssertJ `assertThat(...)` for fluent assertions.
- Builders from [`libs/ftgo-test-util`](../libs/ftgo-test-util/): `OrderBuilder`, `ConsumerBuilder`, …
- No Spring context. No network. No clock. Use
  `com.ftgo.test.config.TestClock` for "now"-dependent logic.

### When to write a unit test
- **Always** for: business rules, validations, state machines,
  calculations, mappers / converters, error branches.
- The rule: if a change could introduce a subtle bug and a mock can
  reproduce the scenario, the unit tier wins.

### Template

See [`templates/test-templates/UnitTestTemplate.java`](../templates/test-templates/UnitTestTemplate.java).

### Mocking guidelines
- Mock **external boundaries**: repositories, HTTP clients,
  `MessagePublisher`, clocks. Never mock the class under test.
- Keep mock setup minimal — if you need to stub a graph of dependent
  objects, the test is probably misplaced (reach for an integration
  test instead).
- Prefer **fakes** over mocks when the behaviour is stateful (e.g. an
  in-memory `CourierRepository` is easier to reason about than 10
  `when().thenReturn()` calls).
- `verify(...)` only when the side-effect **is** the behaviour under
  test — calling `save()` once is often not worth asserting.

## 4. Integration Tests

### Stack
- `@SpringBootTest` (`webEnvironment = MOCK` by default) + test slices
  (`@DataJpaTest`, `@WebMvcTest`) where a narrower context is cheaper.
- **Testcontainers** (MySQL 8) for any test that touches SQL. See
  `com.ftgo.test.containers.AbstractIntegrationTest`.
- Rest-Assured MockMvc for controller-layer tests.
- Flyway migrations run automatically against the container so schema
  stays in lock-step with production.

### Testcontainers — the one rule

> **Never start MySQL with `docker-compose` from a test.** Use
> `FtgoMySqlContainer` / `AbstractIntegrationTest` instead.

Rationale: `docker-compose` requires host-port coordination, races
with parallel Gradle workers, and leaks state across test classes.
Testcontainers gives us:
- Per-JVM isolation (the JUnit 5 `@Testcontainers` extension starts
  one container per test-class lifecycle).
- Reusable containers on developer laptops (`testcontainers.reuse.enable=true`).
- Automatic teardown — no zombie containers after a failed run.

### Template

See [`templates/test-templates/IntegrationTestTemplate.java`](../templates/test-templates/IntegrationTestTemplate.java).

### What lives here
- Repository queries (Spring Data derived + `@Query`).
- Flyway migration correctness (schema applies clean on an empty DB).
- Security filters and `@Secured` boundaries.
- Configuration bindings (`@ConfigurationProperties`).
- Controller ↔ service wiring through MockMvc.

### What does **not** live here
- Pure calculations — belong in unit tests.
- Cross-service interactions — belong in contract or E2E tests.

## 5. Contract Tests

### Stack
- **Spring Cloud Contract** (producer side) or **Pact** (when a
  consumer owns the contract). Defaults to Spring Cloud Contract for
  intra-FTGO calls because it ships a Gradle plugin with verifier
  codegen.
- WireMock stubs produced by the verifier are published to the local
  Maven repo under group `com.ftgo.<service>` and consumed by the
  calling service.

### Template

See [`templates/test-templates/ContractTestTemplate.java`](../templates/test-templates/ContractTestTemplate.java)
and the accompanying Groovy DSL examples in
[`docs/testing/contract-testing.md`](testing/contract-testing.md).

### Ownership
- Each service **owns** the contracts for its public REST endpoints.
  Contracts live under `src/test/resources/contracts/<consumer>/…`.
- Consumers pull the published stubs via `@AutoConfigureStubRunner`
  and run real HTTP traffic against them — no hand-written WireMock.

## 6. API Tests

### Stack
- `@SpringBootTest(webEnvironment = RANDOM_PORT)` + Rest-Assured.
- `FtgoMySqlContainer` for persistence, same as integration tests.
- Seed data loaded through builders, not SQL fixtures.

### Template

See [`templates/test-templates/ApiTestTemplate.java`](../templates/test-templates/ApiTestTemplate.java).

### When to use API vs. integration tests
- **Integration (MockMvc):** contract between controllers and services.
- **API (RANDOM_PORT + Rest-Assured):** golden-path behaviour over the
  real HTTP stack, including content negotiation, CORS, security
  filters, serialization.

A service typically has **dozens** of integration tests and **~5-10**
API tests per bounded context.

## 7. E2E Tests

- Live in [`ftgo-end-to-end-tests/`](../ftgo-end-to-end-tests/).
- Run against a fully composed stack (docker-compose or k8s).
- **Nightly** only, from [`.github/workflows/test-pipeline.yml`](../.github/workflows/test-pipeline.yml).
- Scope: 5 - 10 critical user journeys per release.

## 8. Mock vs. Real Dependencies — Decision Table

| Dependency              | Unit                | Integration        | Contract           | API                |
|-------------------------|---------------------|--------------------|--------------------|--------------------|
| Repository (JPA)        | Mockito mock        | Real + Testcontainers | n/a             | Real + Testcontainers |
| HTTP client (other svc) | Mockito mock        | WireMock           | Published stub     | WireMock           |
| MessagePublisher        | Mockito mock        | In-memory broker   | Real               | In-memory broker   |
| Clock                   | `TestClock.frozen()`| `TestClock.frozen()`| `TestClock.frozen()`| Real             |
| External API (Stripe…)  | Mockito mock        | WireMock           | WireMock           | WireMock           |
| Security / JWT          | `@WithMockUser`     | `@WithMockUser`    | Real signer        | Real signer        |

**Guidance:**
- Reach for a **mock** when the dependency's *behaviour* doesn't
  matter — only its response.
- Reach for a **real** instance when the dependency's *behaviour*
  (e.g. MySQL UNIQUE constraint, Flyway migration, Spring wiring) is
  what you're validating.
- Reach for **WireMock / published stubs** for anything that crosses a
  service boundary. Hand-rolled mocks drift; stubs from contracts
  can't.

## 9. Test Structure & Naming

### Arrange – Act – Assert

```java
@Test
void processTrade_withZeroQuantity_throwsValidationException() {
    // Arrange
    var request = new TradeRequest(1L, "AAPL", "Buy", 0);

    // Act & Assert
    assertThatThrownBy(() -> tradeService.processTrade(request))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("positive");
}
```

### Naming

`<method>_<condition>_<expectedResult>` — e.g.
`createOrder_withUnknownRestaurant_throws404`.

`@Nested` classes group by behaviour, not by method.

## 10. Test Isolation

- Each test is independent. No shared mutable state, no ordering.
- Fresh fixtures per test. `AbstractIntegrationTest` restarts the
  per-class container, and within a class each test should reset the
  data it touched (usually via `@Transactional` rollback or a
  `@BeforeEach` cleanup).
- Parallel execution is **on** for unit tests
  (`junit.jupiter.execution.parallel.enabled=true`) and **off** for
  integration tests (Testcontainers isolation is per-class, not
  per-method).

## 11. Reference Layout for a Service

```
services/<context>-service/
└── src/test/java/com/ftgo/<context>/
    ├── domain/                 # Unit tests — pure logic
    │   └── <Aggregate>Test.java
    ├── web/                    # Integration — MockMvc slices
    │   └── <Controller>Test.java
    ├── repository/             # Integration — @DataJpaTest
    │   └── <Repository>Test.java
    ├── contract/               # Contract tests — generated + custom
    │   └── BaseContract.java
    └── api/                    # API tests — Rest-Assured
        └── <Context>ApiTest.java
```

## 12. CI Integration

- Unit tests run on every push; 2-minute budget enforced per module.
- Integration tests run on PR creation / merge; gate merges to
  `feat/microservices-migration` and `main`.
- Contract stubs are published to the module's `build/stubs/` and
  uploaded as artifacts in the service's CI job.
- E2E tests run nightly on a schedule.

JaCoCo coverage thresholds live in
[`build-logic/convention/src/main/groovy/ftgo.quality-conventions.gradle`](../build-logic/convention/src/main/groovy/ftgo.quality-conventions.gradle);
tightening them is a separate ticket.

---

## References

- ADR template — [`docs/adr/`](adr/).
- FTGO Java style — [Java Coding Best Practices knowledge note].
- [`CONVENTIONS.md`](../CONVENTIONS.md) §3 for service directory layout.
