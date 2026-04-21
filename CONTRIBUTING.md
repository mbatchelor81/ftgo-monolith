# Contributing to FTGO

Thanks for contributing to the FTGO microservices migration. This guide
documents the engineering workflow, coding standards, and review
expectations for every change that lands on this repository.

If you are new to the codebase, start with:

- `README.adoc` — product / deployment overview
- `CONVENTIONS.md` — repository layout and build-logic contract
- `docs/build-configuration.md` — Gradle convention plugin reference

---

## Table of contents

1. [Workflow](#workflow)
2. [Branching and commits](#branching-and-commits)
3. [Code style](#code-style)
4. [Static analysis and quality gates](#static-analysis-and-quality-gates)
5. [Testing expectations](#testing-expectations)
6. [Code review guidelines](#code-review-guidelines)
7. [Secrets and security](#secrets-and-security)

---

## Workflow

1. Pick up a Jira ticket (`EM-xx`). Make sure the scope is written down
   before you open a branch.
2. Branch from `feat/microservices-migration` (the integration branch
   for the ongoing decomposition) unless you are fixing something on
   `master` directly.
3. Implement the change in focused, self-contained commits.
4. Run `./gradlew check` on the modules you touched. CI will run the same
   tasks on every push.
5. Open a pull request using the repository template and request review
   from the relevant code owners (`.github/CODEOWNERS`).
6. Address review comments in follow-up commits (do not force-push once
   a review has started).
7. A PR may only be merged once:
   - At least one code owner for every affected area has approved.
   - CI is green on the latest commit.
   - All quality gate checks pass.

## Branching and commits

- Branch naming: `devin/<ticket>-<timestamp>-<slug>` (e.g.
  `devin/em-47-1712345678-code-quality-gates`). Non-automation work may
  use `feat/<slug>`, `fix/<slug>`, or `chore/<slug>`.
- Commit message format: `<type>(<scope>): <summary>` — e.g.
  `feat(order-service): add revise endpoint`. `type` is one of
  `feat`, `fix`, `chore`, `docs`, `refactor`, `test`, `build`, `ci`.
- Keep commits small and reviewable. If a change is easier to review in
  three commits than one, split it.
- Never amend commits that have already been pushed to a shared branch.

## Code style

- **Java version:** `libs.versions.toml` pins the toolchain (currently
  Java 17). New code targets that version; legacy `ftgo-*` modules stay
  on Java 8 until they are migrated out.
- **Formatter:** `google-java-format` via Spotless. Run
  `./gradlew spotlessApply` before opening a PR; CI verifies with
  `spotlessCheck`.
- **Editor config:** `.editorconfig` mirrors the Google Java Style Guide
  (2-space indent, UTF-8, LF line endings). Configure your editor to
  honor it.
- **Package names:** New modules live under `com.ftgo.<bounded-context>`.
  Legacy code under `net.chrisrichardson.ftgo.*` must keep its package
  names until the owning service is extracted — do not rename in place.
- **Imports:** No wildcard imports. Group `java.*`, then third-party,
  then `com.ftgo.*`, separated by blank lines.
- **Dependency injection:** Constructor injection only. Never use
  field-level `@Autowired`. Mark dependencies `final`.
- **Nullability:** Prefer `Optional<T>` as a return type; never as a
  parameter type. Never return `null` from a collection-returning
  method — return an empty collection instead.
- **Logging:** SLF4J via `LoggerFactory.getLogger(...)`. Structured
  fields (`kv("orderId", id)`) over string concatenation. Never log
  credentials, auth tokens, or full request bodies.

## Static analysis and quality gates

Every module that applies `ftgo.quality-conventions` (all new modules
under `services/*`, `libs/*`, `platform/*`) runs the following as part
of `./gradlew check`:

| Tool         | Config file                              | Fails build on |
|--------------|------------------------------------------|----------------|
| Checkstyle   | `config/checkstyle/checkstyle.xml`       | Any violation  |
| PMD          | `config/pmd/pmd-ruleset.xml`             | Any violation  |
| SpotBugs     | `config/spotbugs/spotbugs-exclude.xml`   | Any violation  |
| Spotless     | `.editorconfig` + Google Java Format     | Unformatted    |
| JaCoCo       | `jacocoTestCoverageVerification`         | Coverage < 70% |

Local commands:

```bash
./gradlew spotlessApply                     # auto-format
./gradlew :services:order-service:check     # full quality gate for one module
./gradlew check                             # everything
```

### Coverage threshold

The minimum line-coverage threshold is **70%** and is enforced per
module. The number is calibrated to the value tests add without
rewarding "cover everything" tests that rely on reflection. If you
need to exclude generated code or framework boilerplate from
coverage, add the path to the module's `jacocoTestReport` /
`jacocoTestCoverageVerification` configuration rather than disabling
the rule globally.

### Suppressing a rule

1. Prefer fixing the underlying issue.
2. If the rule is genuinely wrong for this case, suppress **narrowly**:
   - Checkstyle: edit `config/checkstyle/suppressions.xml`.
   - PMD: `@SuppressWarnings("PMD.<RuleName>")`.
   - SpotBugs: `@SuppressFBWarnings("<PATTERN>")` or add a scoped
     match to `config/spotbugs/spotbugs-exclude.xml`.
3. Explain why in a code comment. Drive-by suppressions without
   justification will be requested out during review.

## Testing expectations

- Follow the testing pyramid — many unit tests, some integration
  tests, few E2E.
- Unit tests use JUnit 5 + AssertJ + Mockito. Name them
  `methodName_condition_expectedResult`.
- Controllers get `@WebMvcTest` slices; repositories get `@DataJpaTest`;
  full-stack tests use `@SpringBootTest` only when necessary.
- Integration / E2E suites live under `services/<svc>/src/integrationTest`
  or the top-level `ftgo-end-to-end-tests` project.
- Never commit tests that sleep as a substitute for synchronization.
- Prefer Testcontainers over embedded or in-memory databases where the
  production engine differs from what the test uses.

## Code review guidelines

Reviewers are responsible for enforcing this list; authors are
responsible for making them easy to check.

### What to look for

- **Correctness first.** Does the change do what the description says?
  Are error paths handled?
- **Scope discipline.** Unrelated refactors, formatting churn, and
  dependency bumps should be separate PRs.
- **Tests.** Every bug fix has a regression test. Every new feature
  has at least one unit test per new code path. Integration tests
  cover wiring, not logic already covered by unit tests.
- **Observability.** New code paths emit the metrics, logs, and trace
  spans needed to debug them in production (see
  `ftgo.observability-conventions`).
- **Security.** Input validation on every request boundary; no SQL
  string concatenation; secrets read from config, not hardcoded.
- **Contracts.** Any breaking change to a REST / event contract ships
  with a migration note and, where applicable, a versioned endpoint.
- **Documentation.** New env vars, config keys, or operational knobs
  are documented in the service README.

### Review etiquette

- Aim to provide a first-pass review within one business day.
- Be direct and specific. "This should use `Optional`" is a suggestion;
  "Return `Optional<Account>` here — the caller already handles empty"
  is actionable.
- Distinguish **blocking** feedback (must fix before merge) from
  **non-blocking** feedback (nice-to-have) with `nit:`, `question:`,
  or `blocking:` prefixes.
- Approve with confidence. If you are not comfortable approving, say so
  and request a co-reviewer instead of rubber-stamping.

### Author etiquette

- Reply to every review comment. "Done", "Ack", or a rationale are all
  acceptable — silent dismissal is not.
- Push follow-up commits rather than amending after review starts, so
  the reviewer can see the delta.
- Rebase on `feat/microservices-migration` when it moves, but do not
  rebase out of habit — prefer merges if review is in flight.

## Secrets and security

- Never commit a real API key, password, TLS cert, or access token —
  not even in a comment or test fixture. CI runs a secret scanner.
- Use Spring Boot's externalized config (`application.yml`,
  environment variables, the shared config server under
  `platform/config-server`). Dev defaults are fine to commit as long
  as they are obviously dev-only.
- Report suspected credential leaks privately to the security code
  owners in `.github/CODEOWNERS` — do not open a public issue.

---

Thanks for keeping the bar high. If anything in this document is
unclear, open a PR against it.
