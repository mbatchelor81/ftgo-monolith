# Contributing to FTGO

Thank you for contributing to the FTGO project. This document provides guidelines
and standards for contributing code, reviewing pull requests, and maintaining
code quality across the monolith-to-microservices migration.

## Table of Contents

- [Getting Started](#getting-started)
- [Development Workflow](#development-workflow)
- [Code Review Guidelines](#code-review-guidelines)
- [Code Style and Formatting](#code-style-and-formatting)
- [Testing Requirements](#testing-requirements)
- [Quality Gates](#quality-gates)
- [Reviewer Assignment](#reviewer-assignment)

---

## Getting Started

### Prerequisites

- **Java 17** (Temurin/Adoptium recommended)
- **Gradle 8.7** (use the wrapper: `./gradlew`)
- **Docker** (for integration tests with Testcontainers)

### Building the Project

```bash
cd services
./gradlew build
```

### Running Tests

```bash
# Unit tests
cd services
./gradlew test

# Integration tests
./gradlew integrationTest

# All checks (tests + static analysis + coverage)
./gradlew check
```

### Formatting Code

```bash
cd services
./gradlew spotlessApply
```

---

## Development Workflow

1. **Create a branch** from `feat/microservices-migration`:
   ```bash
   git checkout feat/microservices-migration
   git pull origin feat/microservices-migration
   git checkout -b <type>/<ticket-id>-<short-description>
   ```
   Branch naming examples:
   - `feat/EM-50-add-payment-service`
   - `fix/EM-51-order-validation-null-check`
   - `refactor/EM-52-extract-courier-domain`

2. **Make your changes** following the code style and testing guidelines below.

3. **Run quality checks locally** before pushing:
   ```bash
   cd services
   ./gradlew spotlessApply   # Auto-format code
   ./gradlew check           # Run all checks
   ```

4. **Push and open a PR** into `feat/microservices-migration`.

5. **Address review feedback** promptly. All conversations must be resolved
   before merging.

---

## Code Review Guidelines

Every pull request must be reviewed and approved before merging. Reviews should
evaluate the following areas:

### Correctness

- [ ] Does the code do what it claims to do?
- [ ] Are edge cases handled (null inputs, empty collections, boundary values)?
- [ ] Are error conditions handled gracefully with appropriate exceptions?
- [ ] Is the state machine / lifecycle logic correct (especially for Order states)?
- [ ] Are database transactions scoped correctly (`@Transactional`)?

### Security

- [ ] No secrets, credentials, or API keys in source code
- [ ] Input validation on all controller endpoints (`@Valid`, Bean Validation)
- [ ] SQL injection prevention (parameterized queries, Spring Data)
- [ ] Authorization checks enforced at the service layer
- [ ] Sensitive data not logged (passwords, tokens, PII)
- [ ] Dependencies free of known CVEs

### Performance

- [ ] No N+1 query patterns (check JPA fetch strategies)
- [ ] Database queries use appropriate indexes
- [ ] No unnecessary eager loading (`FetchType.LAZY` preferred)
- [ ] Large collections paginated, not loaded entirely into memory
- [ ] No blocking calls in request-handling threads without timeouts

### Readability and Maintainability

- [ ] Code follows existing project conventions
- [ ] Classes and methods have clear, single responsibilities
- [ ] Method length is reasonable (< 30 lines preferred)
- [ ] Variable and method names are descriptive
- [ ] Complex logic has explanatory comments
- [ ] No dead code, commented-out code, or TODOs without tickets

### Architecture

- [ ] Changes respect service boundaries (see `ENTITY_OWNERSHIP.md`)
- [ ] Shared code goes in `ftgo-common` or `ftgo-common-jpa`, not duplicated
- [ ] API contracts defined in `*-service-api` modules
- [ ] No circular dependencies between modules
- [ ] Convention plugins used consistently (not ad-hoc build config)

### Testing

- [ ] New code has corresponding unit tests
- [ ] Integration tests cover API contracts and database interactions
- [ ] Tests follow Arrange-Act-Assert pattern
- [ ] Test names clearly describe the scenario being tested
- [ ] No test interdependencies or shared mutable state

---

## Code Style and Formatting

### Automated Enforcement

Code style is enforced automatically via:

| Tool | Purpose | Config |
|------|---------|--------|
| **Spotless** | Auto-formatting (Google Java Format) | `ftgo.quality-conventions.gradle` |
| **Checkstyle** | Style rule enforcement | `config/checkstyle/checkstyle.xml` |
| **EditorConfig** | IDE-agnostic formatting | `.editorconfig` |

Run `./gradlew spotlessApply` to auto-format before committing.

### Key Style Rules

- **Indentation:** 4 spaces (no tabs)
- **Line length:** 120 characters max
- **Imports:** No wildcard imports; remove unused imports
- **Braces:** Required for all control structures (even single-line `if`)
- **Naming:**
  - Classes: `PascalCase` (e.g., `OrderService`)
  - Methods/variables: `camelCase` (e.g., `createOrder`)
  - Constants: `UPPER_SNAKE_CASE` (e.g., `MAX_RETRY_COUNT`)
  - Packages: `lowercase` (e.g., `net.chrisrichardson.ftgo.orderservice`)

### Java Conventions

- Use constructor injection (not `@Autowired` on fields)
- Mark dependencies `final`
- Prefer `Optional` over returning `null`
- Use `List.of()`, `Map.of()` for immutable collections
- Close resources with try-with-resources
- Use `@Override` annotation consistently

---

## Testing Requirements

### Coverage Thresholds

| Metric | Minimum | Enforced By |
|--------|---------|-------------|
| Line coverage | 70% | JaCoCo |
| Branch coverage | 70% | JaCoCo |

### Test Organization

```
src/
  test/java/          # Unit tests
  integration-test/java/  # Integration tests
```

### Naming Convention

```java
// Unit tests: <method>_<condition>_<expected>
@Test
void createOrder_withInvalidConsumer_throwsException() { ... }

// Integration tests: class name ends with IT
class OrderControllerIT { ... }
```

### What to Test

- **Always:** Business logic, validation, state transitions, error paths
- **Integration:** API endpoints, repository queries, cross-service calls
- **Skip:** Getters/setters, framework boilerplate, auto-generated code

---

## Quality Gates

All of the following must pass before a PR can be merged:

| Gate | Tool | Threshold |
|------|------|-----------|
| Compilation | Gradle `build` | Zero errors |
| Unit tests | JUnit 5 | All pass |
| Integration tests | JUnit 5 + Testcontainers | All pass |
| Code style | Checkstyle | Zero violations |
| Bug detection | SpotBugs | Zero bugs (medium+ confidence) |
| Code coverage | JaCoCo | 70% line + branch |
| Formatting | Spotless | No unformatted files |
| CI pipeline | GitHub Actions | All jobs green |

Quality gates are enforced in CI. The `check` task runs all gates:

```bash
cd services
./gradlew check
```

---

## Reviewer Assignment

### Required Reviewers

- All PRs require **at least 1 approval** from a code owner.
- PRs touching multiple services require approval from **each affected service owner**.
- See `.github/CODEOWNERS` for automatic reviewer assignment.

### Service Ownership

| Module | Team |
|--------|------|
| `ftgo-order-service` | @mbatchelor81/ftgo-order-team |
| `ftgo-consumer-service` | @mbatchelor81/ftgo-consumer-team |
| `ftgo-restaurant-service` | @mbatchelor81/ftgo-restaurant-team |
| `ftgo-courier-service` | @mbatchelor81/ftgo-courier-team |
| `ftgo-common`, `ftgo-common-jpa`, `ftgo-domain` | @mbatchelor81/ftgo-platform-team |
| `build-logic`, CI workflows | @mbatchelor81/ftgo-platform-team |

### Review Turnaround

- **Target:** Review within 1 business day
- **Urgent fixes:** Tag the PR with `urgent` for expedited review
- If a reviewer is unavailable for more than 2 days, reassign to another team member

---

## Commit Messages

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <description>

[optional body]

[optional footer(s)]
```

**Types:** `feat`, `fix`, `refactor`, `test`, `docs`, `chore`, `ci`, `perf`

**Scope:** Service or module name (e.g., `order-service`, `common`, `ci`)

**Examples:**
```
feat(order-service): add order revision endpoint
fix(courier-service): handle null delivery address
refactor(domain): extract OrderLineItems value object
test(consumer-service): add integration tests for validation
ci: add quality gate checks to workflow
docs: update CONTRIBUTING.md with review guidelines
```
