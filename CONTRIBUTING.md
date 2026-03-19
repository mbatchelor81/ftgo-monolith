# Contributing to FTGO

Thank you for contributing to the FTGO microservices project. This guide covers our code review process, coding standards, and quality expectations.

## Code Review Guidelines

### Review Checklist

Every PR reviewer should verify:

1. **Correctness**: Does the code do what the ticket requires? Are edge cases handled?
2. **Security**: No hardcoded secrets, proper input validation, authorization checks on endpoints.
3. **Performance**: No N+1 queries, proper use of lazy loading, efficient algorithms.
4. **Readability**: Clear naming, appropriate comments, consistent formatting.
5. **Testing**: Adequate test coverage (unit + integration where applicable).
6. **API Contract**: Breaking changes documented, backward compatibility maintained.

### Review Process

1. All PRs require **at least one approval** before merging.
2. PRs must pass all CI quality gates (build, tests, static analysis).
3. Address all review comments before requesting re-review.
4. Use the PR template — incomplete PRs will be sent back.

### Time-to-Review SLA

| Priority | SLA |
|----------|-----|
| Critical / Hotfix | 2 hours |
| Normal | 1 business day |
| Low / Refactoring | 2 business days |

## Coding Standards

### Java Style

- **Java 17** features encouraged (records, sealed classes, pattern matching).
- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) with project-specific overrides in `config/checkstyle/checkstyle.xml`.
- Use **constructor injection** (never `@Autowired` on fields).
- Prefer `Optional` over returning `null`.
- Use `final` for variables that should not be reassigned.

### Package Structure

Each microservice follows the standard layered architecture:

```
com.ftgo.<service>/
  ├── config/        # @Configuration classes
  ├── web/           # @RestController endpoints
  ├── domain/        # @Service business logic, @Entity classes
  ├── repository/    # @Repository interfaces
  ├── dto/           # Request/response DTOs
  └── exception/     # Custom exceptions
```

### Commit Messages

Follow conventional commits format:

```
<type>(<scope>): <description>

[optional body]
[optional footer]
```

Types: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`, `ci`

### Branch Naming

```
feat/<jira-key>-<short-description>
fix/<jira-key>-<short-description>
chore/<jira-key>-<short-description>
```

## Testing Requirements

### Coverage Thresholds

| Test Type | Minimum Coverage | Target Coverage |
|-----------|-----------------|-----------------|
| Unit Tests | 70% line coverage | 80%+ |
| Integration Tests | Critical paths covered | All API endpoints |

### What to Test

- **Always test**: Business logic, input validation, error handling, state transitions.
- **Integration test**: API endpoints, database queries, cross-service calls.
- **Skip testing**: Framework boilerplate, auto-generated code, trivial getters/setters.

### Running Tests

```bash
# Unit tests
./gradlew test

# Integration tests (requires MySQL)
./gradlew integrationTest

# All tests with coverage
./gradlew test jacocoTestReport

# Static analysis
./gradlew checkstyleMain spotbugsMain pmdMain
```

## Quality Gates

All PRs must pass these automated checks:

1. **Build**: `./gradlew build` succeeds
2. **Unit Tests**: All pass with JaCoCo coverage >= 70%
3. **Checkstyle**: No violations against project rules
4. **SpotBugs**: No high-priority bugs detected
5. **PMD**: No violations against project rules

## Getting Help

- Check existing code in `shared/` libraries for patterns and conventions.
- Review the architecture docs in the project wiki.
- Ask in the team Slack channel for guidance on design decisions.
