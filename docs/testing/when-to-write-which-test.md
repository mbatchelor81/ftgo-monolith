# When to Write Which Test

A decision tree for picking the right tier on the testing pyramid
(see [`docs/testing-strategy.md`](../testing-strategy.md) §1). If in
doubt, err toward the **cheaper** tier — you can always add a
higher-tier test later if coverage drifts.

## 1. Start here

```
  ┌──────────────────────────────────────────────┐
  │  What am I trying to validate?               │
  └──────────────────────────────────────────────┘
                       │
     ┌─────────────────┼──────────────────────────┐
     ▼                 ▼                          ▼
  Business logic    Wiring +              Cross-service
  / calculations    persistence           or full stack
     │                 │                          │
     ▼                 ▼                          ▼
  Unit test        Integration test       Contract / API / E2E
```

## 2. Decision table

| What you're testing                                    | Tier           | Why                                                           |
|--------------------------------------------------------|----------------|---------------------------------------------------------------|
| Domain entity state transitions (e.g. `Order.accept`)  | **Unit**       | Pure logic; no need to boot Spring.                           |
| `Money.add`, `Money.isGreaterThanOrEqual`, etc.        | **Unit**       | Value-object math.                                            |
| `OrderService.createOrder` with all deps mocked        | **Unit**       | Service orchestration — mocks are the whole point.            |
| Validation annotations on a DTO                        | **Unit**       | Use `Validator.validate` directly.                            |
| Controller routing + HTTP status codes                 | **Integration**| `@WebMvcTest` with MockMvc.                                   |
| JPA query by non-PK field                              | **Integration**| `@DataJpaTest` + Testcontainers MySQL.                        |
| Flyway migration applies cleanly                       | **Integration**| `AbstractIntegrationTest` starts with an empty container.     |
| Security filter behaviour (401/403)                    | **Integration**| Needs the Spring Security filter chain.                       |
| Request reaches downstream service over HTTP           | **Contract**   | Don't couple two CI jobs; use published stubs.                |
| Downstream service's contract hasn't changed           | **Contract**   | Producer contract test runs during their PR.                  |
| JSON schema / response shape                           | **Contract**   | Encoded in Spring Cloud Contract DSL.                         |
| CORS, rate-limit, circuit-breaker behaviour            | **API**        | Exercise the full HTTP stack on `RANDOM_PORT`.                |
| Happy-path through controller → service → repo → DB    | **API**        | One test per bounded context's golden path.                   |
| Create order → restaurant accepts → courier delivers   | **E2E**        | Multi-service workflow; belongs in `ftgo-end-to-end-tests/`.  |

## 3. Unit test — checklist

- **Mock** the dependencies of the class under test.
- **Don't** boot Spring. No `@SpringBootTest`, `@DataJpaTest`, etc.
- **Don't** hit a real datastore or network.
- Use builders from `libs/ftgo-test-util` to assemble fixtures.
- Assert with AssertJ (`assertThat`) and custom domain assertions
  (`MoneyAssert`, `OrderAssert`).
- Tests should complete in **milliseconds**.

### Example
See [`templates/test-templates/UnitTestTemplate.java`](../../templates/test-templates/UnitTestTemplate.java).

## 4. Integration test — checklist

- Pick the **narrowest** Spring slice that exercises the behaviour:
  - `@DataJpaTest` for repositories.
  - `@WebMvcTest(XxxController.class)` for controller + web config.
  - `@SpringBootTest(webEnvironment = MOCK)` only if slices can't
    cover the wiring.
- Extend `AbstractIntegrationTest` if a MySQL instance is needed.
- Wire dynamic properties through `@DynamicPropertySource` — don't
  override the datasource bean directly.
- Reset data per test (`@Transactional` rollback is the default).
- Budget: **~seconds per test**, not minutes.

### Example
See [`templates/test-templates/IntegrationTestTemplate.java`](../../templates/test-templates/IntegrationTestTemplate.java).

## 5. Contract test — checklist

- **Producer** defines contracts in Groovy DSL under
  `src/test/resources/contracts/<consumer>/`.
- Each service has **one** `BaseContract` class; don't fan out.
- The Spring Cloud Contract plugin generates the verifier — you do
  not hand-write the assertions.
- **Consumers** use `@AutoConfigureStubRunner` against the producer's
  published stubs. No hand-rolled WireMock.

### Example
See [`docs/testing/contract-testing.md`](contract-testing.md) and
[`templates/test-templates/ContractTestTemplate.java`](../../templates/test-templates/ContractTestTemplate.java).

## 6. API test — checklist

- `@SpringBootTest(webEnvironment = RANDOM_PORT)`.
- Rest-Assured (not MockMvc) so the request goes over the real HTTP
  stack.
- Cover **golden paths**, not branches — branches belong to unit and
  integration tests.
- ~5-10 API tests per bounded context is the target.

### Example
See [`templates/test-templates/ApiTestTemplate.java`](../../templates/test-templates/ApiTestTemplate.java).

## 7. E2E test — checklist

- Lives in `ftgo-end-to-end-tests/`.
- Nightly run only (schedule in `.github/workflows/test-pipeline.yml`).
- Scope: 5-10 user journeys per release.
- Targets a fully deployed stack (docker-compose or k8s).

## 8. "It depends" — common gotchas

- **Parameterized scenarios.** Prefer `@ParameterizedTest` at the unit
  tier over repeating the same integration test 10 times.
- **Flaky tests.** Don't skip with `@Disabled`. Root-cause or move
  down one tier. Every flake erodes trust in CI.
- **Shared state.** Never depend on execution order. Parallel unit
  tests must still pass; if they can't, carve out clock/ID
  dependencies through builders or `TestClock`.
- **Large fixtures.** If a test needs 50 lines of setup, the system
  under test is probably misplaced — revisit whether the logic
  belongs deeper in the domain model.

## 9. Summary

| If the behaviour…                                      | Write…        |
|--------------------------------------------------------|---------------|
| …can be reproduced with a mock in <1 s                 | Unit test     |
| …depends on Spring wiring or SQL                       | Integration   |
| …spans two services                                    | Contract      |
| …must go through real HTTP + filters                   | API           |
| …is a user journey across multiple services            | E2E           |
