## Description

<!-- Provide a brief description of the changes in this PR -->

**Jira Ticket:** <!-- e.g., EM-XX -->

## Type of Change

- [ ] Bug fix (non-breaking change which fixes an issue)
- [ ] New feature (non-breaking change which adds functionality)
- [ ] Breaking change (fix or feature that would cause existing functionality to change)
- [ ] Refactoring (no functional changes)
- [ ] Documentation update
- [ ] Infrastructure/CI change

## Testing

### How Has This Been Tested?

<!-- Describe the tests you ran to verify your changes -->

- [ ] Unit tests pass (`./gradlew test`)
- [ ] Integration tests pass (`./gradlew integrationTest`)
- [ ] E2E tests pass (if applicable)
- [ ] Manual testing performed

### Test Coverage

<!-- Note any coverage impact -->

## Review Checklist

### Code Quality
- [ ] Code follows the project's coding standards
- [ ] Self-review of the code has been performed
- [ ] No unnecessary commented-out code
- [ ] No hardcoded values that should be configurable

### Security
- [ ] No secrets or credentials in the code
- [ ] Input validation implemented for new endpoints
- [ ] Authorization checks in place for protected resources
- [ ] No SQL injection vulnerabilities (parameterized queries used)

### Performance
- [ ] No N+1 query issues introduced
- [ ] Database queries are optimized (proper indexes, joins)
- [ ] No unnecessary eager loading of associations

### Documentation
- [ ] Code is self-documenting or has appropriate comments
- [ ] API changes documented (OpenAPI/Swagger annotations updated)
- [ ] README updated if setup steps changed

## Breaking Changes

<!-- List any breaking changes and migration steps required -->

## Screenshots / Logs

<!-- If applicable, add screenshots or relevant log output -->
