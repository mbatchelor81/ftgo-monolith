# FTGO Test Templates

Canonical templates for each tier of the FTGO testing pyramid (see
[`docs/testing-strategy.md`](../../docs/testing-strategy.md)). Copy the
file that matches the tier you're writing, rename the class to match
the system under test, and delete everything that doesn't apply.

These files are **not** compiled as part of any Gradle project —
`templates/` is excluded from `settings.gradle` so the package paths
use placeholder names like `com.ftgo.example`. That is intentional:
templates must not accidentally satisfy a production build, and stale
templates must not break CI.

| File                              | Tier         | Stack                                                                |
|-----------------------------------|--------------|----------------------------------------------------------------------|
| `UnitTestTemplate.java`           | Unit         | JUnit 5 + Mockito + AssertJ (+ `libs/ftgo-test-util` builders)       |
| `IntegrationTestTemplate.java`    | Integration  | `@SpringBootTest` + Testcontainers MySQL (`AbstractIntegrationTest`) |
| `ContractTestTemplate.java`       | Contract     | Spring Cloud Contract base class + Rest-Assured MockMvc              |
| `ApiTestTemplate.java`            | API          | `@SpringBootTest(RANDOM_PORT)` + Rest-Assured over real HTTP         |

## How to use a template

1. Pick the tier — if in doubt, see
   [`docs/testing/when-to-write-which-test.md`](../../docs/testing/when-to-write-which-test.md).
2. Copy the template into your service:
   ```bash
   cp templates/test-templates/UnitTestTemplate.java \
      services/order-service/src/test/java/com/ftgo/order/domain/OrderServiceTest.java
   ```
3. Rename the class, update the package declaration, and delete the
   template's placeholder TODO markers.
4. Replace the placeholder fixture/assertion calls with real ones.
5. Run the test once locally before pushing.

## Conventions embedded in these templates

- JUnit Jupiter only — no JUnit 4.
- AssertJ (`org.assertj.core.api.Assertions.assertThat`) over the
  built-in `org.junit.jupiter.api.Assertions`.
- Fixtures come from `libs/ftgo-test-util` (`OrderBuilder`,
  `MoneyBuilder`, `FtgoMothers`, `TestClock`).
- Spring context sizing: `@WebMvcTest` > `@DataJpaTest` >
  `@SpringBootTest(MOCK)` > `@SpringBootTest(RANDOM_PORT)`. Prefer
  the narrowest slice that exercises the behaviour.
- Testcontainers replaces `docker-compose` for every integration test
  that touches MySQL.
- Each template includes an `ArchitectureNotes` comment explaining
  *why* the tier was chosen — keep that comment when you copy.
