# Quality Gates

This document describes the automated quality gates enforced across the FTGO
Platform.

## Overview

New microservices under `services/` should apply the `ftgo.quality-conventions`
Gradle plugin (see [Applying to a New Module](#applying-to-a-new-module)).
The plugin configures four tools:

| Tool | Version | Purpose |
|------|---------|---------|
| Checkstyle | 10.17.0 | Code style enforcement (Google Java Style) |
| PMD | 7.2.0 | Static analysis for common bug patterns |
| SpotBugs | 4.8.5 | Bytecode-level bug detection |
| JaCoCo | 0.8.12 | Code coverage measurement and enforcement |

## Thresholds

### JaCoCo Coverage

| Metric | Minimum |
|--------|---------|
| Line coverage | 70% |
| Branch coverage | 50% |

### Checkstyle

- Zero warnings policy (`maxWarnings = 0`).
- Based on Google Java Style with project-specific adjustments.

### PMD

- Rules defined in `config/quality/pmd-ruleset.xml`.
- Cyclomatic complexity limit: 15 per method.

### SpotBugs

- Report level: `high` (only high-confidence findings fail the build).
- Exclusions defined in `config/quality/spotbugs-exclude.xml`.

## CI Integration

Quality checks run as part of the `check` task in each service's CI workflow.
The monolith CI also runs a dedicated quality gate step for new microservice
modules.

### SonarQube

SonarQube analysis is available via:

```bash
./gradlew sonar \
  -Dsonar.host.url=$SONAR_HOST_URL \
  -Dsonar.token=$SONAR_TOKEN
```

Required environment variables:
- `SONAR_TOKEN` — authentication token
- `SONAR_HOST_URL` — server URL (CI defaults to `https://sonarcloud.io` if not set)
- `SONAR_ORGANIZATION` — organization key (defaults to `mbatchelor81`)

## Configuration Files

```
config/quality/
├── checkstyle.xml               # Checkstyle rules
├── checkstyle-suppressions.xml  # Checkstyle suppressions
├── pmd-ruleset.xml              # PMD rules
└── spotbugs-exclude.xml         # SpotBugs exclusion filter
```

## Applying to a New Module

Add the quality conventions plugin to your module's `build.gradle`:

```groovy
plugins {
    id 'ftgo.quality-conventions'
}
```

This transitively applies `ftgo.java-conventions` and configures all four
quality tools with the shared configuration files.

## Suppressing Findings

See [CONTRIBUTING.md](../../CONTRIBUTING.md#suppressing-false-positives) for
instructions on suppressing false positives per tool.
