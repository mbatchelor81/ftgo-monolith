# Contributing to FTGO

Thank you for contributing to the FTGO microservices migration project. This guide outlines our code review process, quality standards, and development workflow.

## Table of Contents

- [Development Workflow](#development-workflow)
- [Branch Strategy](#branch-strategy)
- [Code Style and Formatting](#code-style-and-formatting)
- [Static Analysis and Quality Gates](#static-analysis-and-quality-gates)
- [Testing Requirements](#testing-requirements)
- [Code Review Process](#code-review-process)
- [Code Review Checklist](#code-review-checklist)
- [Commit Messages](#commit-messages)

---

## Development Workflow

1. **Create a branch** from `feat/microservices-migration-v2` following the branch naming conventions.
2. **Implement your changes** following the coding standards below.
3. **Run quality checks locally** before pushing:
   ```bash
   export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
   ./gradlew :services:<service-name>:check
   ./gradlew :services:<service-name>:qualityGate
   ```
4. **Push your branch** and create a Pull Request targeting `feat/microservices-migration-v2`.
5. **Address review feedback** — all conversations must be resolved before merge.
6. **Ensure CI passes** — all quality gates must be green.

## Branch Strategy

| Branch | Purpose |
|--------|---------|
| `main` | Production-ready code |
| `feat/microservices-migration-v2` | Active migration work |
| `feature/EM-XXX-description` | Feature branches (from migration branch) |
| `fix/EM-XXX-description` | Bug fix branches |
| `refactor/EM-XXX-description` | Refactoring branches |

## Code Style and Formatting

### Java Code Style

- **Java 17** — use modern language features (records, sealed classes, switch expressions, `var`).
- **Google Java Style** — enforced via Checkstyle (see `config/checkstyle/checkstyle.xml`).
- **EditorConfig** — IDE formatting rules in `.editorconfig` at the project root.
- **Max line length:** 150 characters.
- **Indentation:** 4 spaces (no tabs).

### Key Conventions

- Use **constructor injection** (never field injection with `@Autowired`).
- Mark injected dependencies as `final`.
- Prefer `Optional` over returning `null`.
- Use `List.of()`, `Map.of()`, `Set.of()` for immutable collections.
- Follow the **layered architecture**: Controller -> Service -> Repository.
- Keep controllers thin — delegate all logic to services.

### Spring Boot Conventions

- Use `@RestController` with `@RequestMapping` for REST APIs.
- Use `@Valid` with Bean Validation on request DTOs.
- Externalize configuration to `application.yml`.
- Use `@Transactional` on service methods, not controllers or repositories.
- Use `FetchType.LAZY` by default for JPA associations.

## Static Analysis and Quality Gates

All microservice modules enforce the following quality gates via the `ftgo.quality-conventions` Gradle plugin:

| Tool | Purpose | Configuration |
|------|---------|---------------|
| **Checkstyle** | Code style enforcement | `config/checkstyle/checkstyle.xml` |
| **SpotBugs** | Bug pattern detection | `config/spotbugs/exclude-filter.xml` |
| **PMD** | Code quality rules | `config/pmd/pmd-ruleset.xml` |
| **JaCoCo** | Code coverage (70% minimum) | `ftgo.testing-conventions` plugin |

### Running Quality Checks Locally

```bash
# Run all quality checks for a specific service
./gradlew :services:ftgo-order-service:qualityGate

# Run individual checks
./gradlew :services:ftgo-order-service:checkstyleMain
./gradlew :services:ftgo-order-service:spotbugsMain
./gradlew :services:ftgo-order-service:pmdMain

# Run coverage verification
./gradlew :services:ftgo-order-service:jacocoTestCoverageVerification
```

### Suppressing False Positives

- **Checkstyle:** Use `@SuppressWarnings("checkstyle:RuleName")` sparingly and document why.
- **SpotBugs:** Use `@SuppressFBWarnings(value = "XX_CODE", justification = "reason")`.
- **PMD:** Use `@SuppressWarnings("PMD.RuleName")` with a comment explaining why.

Suppressions must be reviewed and approved during code review.

## Testing Requirements

### Test Coverage

- **Minimum 70% line coverage** per service module (enforced by JaCoCo in CI).
- Focus on business logic and service layer — aim for 80%+ on these.
- Unit tests for all public methods with meaningful logic.
- Integration tests for API endpoints and database interactions.

### Test Structure

Follow the **Arrange-Act-Assert** pattern:

```java
@Test
void createOrder_withValidRequest_returnsCreatedOrder() {
    // Arrange
    var request = new CreateOrderRequest(consumerId, restaurantId, lineItems);

    // Act
    var result = orderService.createOrder(request);

    // Assert
    assertThat(result.getState()).isEqualTo(OrderState.APPROVED);
}
```

### Naming Convention

```
methodName_condition_expectedResult
```

Examples:
- `createOrder_withInvalidConsumer_throwsNotFoundException`
- `cancel_whenOrderIsDelivered_throwsUnsupportedStateTransition`

### Running Tests

```bash
# Unit tests
./gradlew :services:ftgo-order-service:test

# Integration tests (requires MySQL)
./gradlew :services:ftgo-order-service:integrationTest

# All tests with coverage
./gradlew :services:ftgo-order-service:check
```

## Code Review Process

### For Authors

1. **Self-review first** — review your own diff before requesting review.
2. **Fill out the PR template** completely — description, testing, checklist.
3. **Keep PRs focused** — one logical change per PR. Aim for < 400 lines changed.
4. **Respond to feedback promptly** — address all comments within 24 hours.
5. **Don't force-push** during review — add new commits so reviewers can see incremental changes.

### For Reviewers

1. **Review within 24 hours** of being assigned.
2. **Be constructive** — suggest improvements, don't just point out problems.
3. **Distinguish blocking vs. non-blocking** — prefix non-blocking comments with `nit:` or `suggestion:`.
4. **Test locally** if the change is non-trivial — pull the branch and run `./gradlew check`.
5. **Approve explicitly** — don't leave a PR in limbo.

### Approval Requirements

- **Minimum 1 approval** required from a code owner.
- All CI checks must pass (unit tests, integration tests, quality gates).
- All review conversations must be resolved.

## Code Review Checklist

Use this checklist when reviewing PRs:

### Correctness
- [ ] Business logic is correct and handles edge cases
- [ ] Error handling is appropriate (no swallowed exceptions)
- [ ] Null safety — no potential `NullPointerException` paths
- [ ] Concurrency safety — thread-safe where required

### Design
- [ ] Single Responsibility Principle followed
- [ ] No unnecessary coupling between services
- [ ] API contracts are versioned and backward-compatible
- [ ] Database migrations are backward-compatible and reversible

### Security
- [ ] No secrets or credentials in code
- [ ] Input validation on all external inputs
- [ ] Authentication and authorization enforced
- [ ] SQL injection prevention (parameterized queries)
- [ ] No sensitive data in logs

### Performance
- [ ] No N+1 query patterns
- [ ] Appropriate use of caching where beneficial
- [ ] No unnecessary eager loading of JPA associations
- [ ] Pagination used for list endpoints

### Observability
- [ ] Adequate logging for debugging (structured JSON format)
- [ ] Metrics exposed for key operations
- [ ] Distributed tracing context propagated

### Testing
- [ ] Unit tests cover new/changed logic
- [ ] Integration tests cover API contracts
- [ ] Edge cases and error paths tested
- [ ] Test names are descriptive and follow naming conventions

## Commit Messages

Follow the [Conventional Commits](https://www.conventionalcommits.org/) format:

```
<type>(<scope>): <description>

[optional body]

[optional footer(s)]
```

### Types

| Type | Description |
|------|-------------|
| `feat` | New feature |
| `fix` | Bug fix |
| `refactor` | Code refactoring (no functional change) |
| `test` | Adding or updating tests |
| `docs` | Documentation changes |
| `chore` | Build, CI, or tooling changes |
| `perf` | Performance improvements |

### Scope

Use the service name or module: `order-service`, `consumer-service`, `common`, `ci`, etc.

### Examples

```
feat(order-service): add order revision endpoint
fix(courier-service): handle null availability gracefully
chore(ci): add SpotBugs quality gate to pipeline
test(order-service): add unit tests for order cancellation
docs: update CONTRIBUTING.md with review guidelines
```

---

## Questions?

If you have questions about the contribution process, please reach out to the team leads or open a discussion in the repository.
