## Description

<!-- Provide a concise summary of the changes and the motivation behind them. -->
<!-- Link to the relevant ticket: EM-XXX -->

**Related Ticket:** EM-

## Type of Change

<!-- Check the relevant option(s). -->

- [ ] Bug fix (non-breaking change that fixes an issue)
- [ ] New feature (non-breaking change that adds functionality)
- [ ] Breaking change (fix or feature that would cause existing functionality to change)
- [ ] Refactoring (no functional changes)
- [ ] Configuration / Infrastructure change
- [ ] Documentation update

## Changes Made

<!-- List the specific changes made in this PR. -->

-
-
-

## Testing

<!-- Describe the tests you ran to verify your changes. -->

### Tests Added/Modified

- [ ] Unit tests added/updated
- [ ] Integration tests added/updated
- [ ] E2E tests added/updated (if applicable)

### Test Results

<!-- Include relevant test output or screenshots. -->

```
# Example: ./gradlew :services:ftgo-order-service:test
```

## Quality Checklist

<!-- Verify all items before requesting review. -->

- [ ] Code compiles without errors (`./gradlew classes`)
- [ ] All unit tests pass (`./gradlew test`)
- [ ] Integration tests pass (`./gradlew integrationTest`)
- [ ] Static analysis passes (`./gradlew qualityGate`)
- [ ] JaCoCo coverage meets 70% threshold
- [ ] No new Checkstyle warnings introduced
- [ ] No new SpotBugs issues introduced
- [ ] No new PMD violations introduced

## Code Review Checklist

<!-- For reviewers — verify these during review. -->

- [ ] Code follows FTGO coding conventions and style guide
- [ ] Business logic is correct and handles edge cases
- [ ] Error handling is appropriate (no swallowed exceptions)
- [ ] No hardcoded values that should be configurable
- [ ] API contracts are backward-compatible (or breaking change is documented)
- [ ] Database migrations are backward-compatible
- [ ] Security considerations addressed (no exposed secrets, proper auth)
- [ ] Logging is adequate for debugging but not excessive
- [ ] No unnecessary dependencies added

## Breaking Changes

<!-- If this is a breaking change, describe the impact and migration steps. -->
<!-- Remove this section if not applicable. -->

**Impact:**

**Migration Steps:**

## Screenshots / Evidence

<!-- Add screenshots, logs, or other evidence if applicable. -->

## Additional Notes

<!-- Any additional context for reviewers. -->
