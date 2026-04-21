<!--
  FTGO pull request template
  --------------------------
  Fill every section below. Delete sections that truly do not apply, but
  prefer "N/A" with a one-line justification over silent deletion so the
  reviewer can tell what was considered.
-->

## Summary

<!-- What does this PR change, and why? One or two paragraphs max. -->

## Related work

- Jira: <!-- e.g. EM-47 -->
- Design doc / ADR: <!-- link, or "N/A" -->
- Dependent / follow-up PRs: <!-- links, or "N/A" -->

## Type of change

<!-- Check all that apply. -->

- [ ] Bug fix (non-breaking change that fixes an issue)
- [ ] New feature (non-breaking change that adds functionality)
- [ ] Breaking change (fix or feature that changes existing behavior)
- [ ] Refactor (no user-visible behavior change)
- [ ] Build / CI / tooling
- [ ] Documentation only

## How was this tested?

<!--
  Describe the tests you ran. For service changes, state which service and
  which test tasks (e.g. `./gradlew :services:order-service:check`).
-->

- [ ] Added or updated unit tests
- [ ] Added or updated integration tests
- [ ] Verified manually (describe below)
- [ ] `./gradlew check` passes locally for the touched modules

## Quality gate checklist

- [ ] Code is formatted (`./gradlew spotlessApply`)
- [ ] Static analysis passes (`./gradlew checkstyleMain pmdMain spotbugsMain`)
- [ ] Coverage meets the 70% minimum for changed modules (JaCoCo)
- [ ] Public API changes are documented (OpenAPI, README, or ADR)
- [ ] No new `TODO` / `FIXME` without a linked Jira ticket
- [ ] Logs do not leak secrets, PII, or tokens

## Deployment / operational impact

<!--
  Migrations, config changes, feature flags, new env vars, ordering
  constraints with other services, rollback plan. "None" is an acceptable
  answer but it must be a deliberate one.
-->

## Reviewer notes

<!--
  Anything the reviewer should pay extra attention to, areas of the diff
  you are least confident in, or questions for the team.
-->
