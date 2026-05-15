# Contributing to FTGO Platform

Thank you for contributing to the FTGO Platform. This guide covers code review
standards, development workflow, and quality expectations.

---

## Table of Contents

1. [Development Workflow](#development-workflow)
2. [Branch Strategy](#branch-strategy)
3. [Commit Messages](#commit-messages)
4. [Code Style](#code-style)
5. [Code Review Guidelines](#code-review-guidelines)
6. [Testing Requirements](#testing-requirements)
7. [Static Analysis & Quality Gates](#static-analysis--quality-gates)
8. [Pull Request Process](#pull-request-process)

---

## Development Workflow

1. Pick up or create an issue in the project tracker.
2. Create a feature branch from `feat/microservices-migration` (or `main` for
   hotfixes).
3. Implement changes following the conventions below.
4. Run the build locally before pushing:
   ```bash
   ./gradlew clean build \
     -x :ftgo-end-to-end-tests:test \
     -x :ftgo-end-to-end-tests-common:build
   ```
5. Open a pull request using the PR template.
6. Address review feedback and ensure CI passes.

## Branch Strategy

| Branch | Purpose |
|--------|---------|
| `main` | Production-ready code |
| `feat/microservices-migration` | Active migration work |
| `feat/<ticket>-description` | Feature branches |
| `fix/<ticket>-description` | Bug-fix branches |
| `chore/<description>` | Build, CI, docs changes |

## Commit Messages

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <subject>

[optional body]

[optional footer(s)]
```

**Types:** `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`, `ci`, `build`

**Scope:** module name (e.g., `order-service`, `consumer-service`, `build-logic`)

Examples:
```
feat(order-service): add order revision endpoint
fix(consumer-service): handle null address in validation
chore(build-logic): add Checkstyle to quality conventions
```

## Code Style

### Java

- Follow the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
  with project-specific adjustments defined in `config/quality/checkstyle.xml`.
- Indent with **2 spaces** (configured in `.editorconfig`).
- Maximum line length: **120 characters**.
- Use `final` for variables that should not be reassigned.
- Prefer composition over inheritance.
- Keep methods short (< 60 lines). Extract helpers for clarity.
- Avoid returning `null` — use `Optional`, empty collections, or throw.
- Use try-with-resources for `Closeable` objects.

### Naming Conventions

| Element | Convention | Example |
|---------|-----------|---------|
| Classes | `PascalCase` (noun) | `OrderService` |
| Interfaces | `PascalCase` (adjective/noun) | `Schedulable` |
| Methods | `camelCase` (verb) | `createOrder()` |
| Constants | `UPPER_SNAKE_CASE` | `MAX_RETRY_COUNT` |
| Packages | `lowercase` | `net.chrisrichardson.ftgo.orderservice` |
| Test methods | `methodName_condition_expectedResult` | `createOrder_nullConsumer_throwsException` |

### Imports

- No wildcard imports (`import foo.*`).
- Remove unused imports.
- Order: `java.*`, `javax.*`, blank line, third-party, blank line, project.

## Code Review Guidelines

### For Authors

1. **Keep PRs small.** Aim for < 400 lines of diff. Split large changes into
   stacked PRs.
2. **Write a clear PR description.** Explain *what* changed and *why*. Link the
   issue.
3. **Self-review first.** Read your own diff before requesting review.
4. **Add tests.** Every behavioral change needs a test.
5. **Don't mix concerns.** Separate refactors, features, and formatting into
   distinct commits or PRs.

### For Reviewers

1. **Be timely.** Respond to review requests within one business day.
2. **Be constructive.** Suggest improvements; don't just point out problems.
3. **Categorize feedback:**
   - **Blocking:** Must fix before merge (bugs, security, missing tests).
   - **Non-blocking:** Suggestions for improvement (style nits, naming).
   - **Question:** Clarification needed to understand intent.
4. **Check for:**
   - Correctness: Does it do what it claims?
   - Tests: Are edge cases covered?
   - Security: No secrets, no injection vectors, proper auth checks.
   - Performance: No N+1 queries, no unbounded collections.
   - API contracts: Are breaking changes documented?
   - Error handling: Are failures surfaced, not swallowed?
5. **Approve when satisfied.** Don't block on nitpicks — leave non-blocking
   comments and approve.

### Review Checklist

```
- [ ] Code compiles and tests pass
- [ ] Logic is correct and handles edge cases
- [ ] No security issues (secrets, injection, auth bypass)
- [ ] Error handling is appropriate
- [ ] Test coverage is adequate
- [ ] API changes are backward-compatible (or documented)
- [ ] No unnecessary complexity
- [ ] Naming is clear and consistent
```

## Testing Requirements

### Testing Pyramid

| Level | Scope | Required Coverage |
|-------|-------|-------------------|
| **Unit** | Single class/method | 70% line, 50% branch |
| **Integration** | Multiple components | API contracts, DB queries |
| **E2E** | Full user flow | Critical paths only |

### Standards

- Use **JUnit 5** with **Mockito** for unit tests.
- Use **AssertJ** (`assertThat`) for assertions.
- Follow the **Arrange–Act–Assert** pattern.
- Name tests: `methodName_condition_expectedResult`.
- Use `@SpringBootTest` sparingly — prefer `@WebMvcTest` or `@DataJpaTest`.
- Tests must be **isolated** and **repeatable** — no shared mutable state.

### Running Tests

```bash
# Unit tests only
./gradlew test

# Full build with tests (excludes E2E)
./gradlew clean build \
  -x :ftgo-end-to-end-tests:test \
  -x :ftgo-end-to-end-tests-common:build
```

## Static Analysis & Quality Gates

The build enforces quality via Checkstyle, PMD, SpotBugs, and JaCoCo.
New microservices (under `services/`) apply the `ftgo.quality-conventions`
plugin which configures all four tools.

### Tools

| Tool | Purpose | Config |
|------|---------|--------|
| **Checkstyle** | Code style enforcement | `config/quality/checkstyle.xml` |
| **PMD** | Bug pattern detection | `config/quality/pmd-ruleset.xml` |
| **SpotBugs** | Bytecode bug finder | `config/quality/spotbugs-exclude.xml` |
| **JaCoCo** | Code coverage | 70% line / 50% branch minimum |
| **SonarQube** | Continuous inspection | CI integration via `sonar` task |

### Running Locally

```bash
# Run all quality checks
./gradlew check

# Individual tools
./gradlew checkstyleMain checkstyleTest
./gradlew pmdMain pmdTest
./gradlew spotbugsMain spotbugsTest
./gradlew jacocoTestReport jacocoTestCoverageVerification
```

### Suppressing False Positives

- **Checkstyle:** Use `@SuppressWarnings("checkstyle:RuleName")` or add to
  `config/quality/checkstyle-suppressions.xml`.
- **PMD:** Use `@SuppressWarnings("PMD.RuleName")` on the method or class.
- **SpotBugs:** Use `@SuppressFBWarnings("BUG_PATTERN")` or add to
  `config/quality/spotbugs-exclude.xml`.

Document the reason for any suppression in a code comment.

## Pull Request Process

1. Fill out the PR template completely.
2. Ensure CI passes (build, tests, quality gates).
3. Request review from the appropriate code owners (see `CODEOWNERS`).
4. At least **one approval** is required to merge.
5. Squash-merge feature branches. Use the PR title as the commit message.
6. Delete the feature branch after merge.
