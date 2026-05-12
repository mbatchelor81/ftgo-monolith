# ADR-0001: Mono-Repo with Service Boundaries for Microservices Migration

## Status

Accepted

## Date

2026-05-12

## Context

The FTGO platform is currently a Spring Boot monolith composed of 14 Gradle
sub-modules in a flat layout. The modules fall into five categories:

| Category | Modules |
|----------|---------|
| Shared libraries | `ftgo-common`, `ftgo-common-jpa`, `ftgo-domain`, `common-swagger`, `ftgo-test-util` |
| Services | `ftgo-order-service`, `ftgo-consumer-service`, `ftgo-restaurant-service`, `ftgo-courier-service` |
| API contracts | `ftgo-order-service-api`, `ftgo-consumer-service-api`, `ftgo-restaurant-service-api`, `ftgo-courier-service-api` |
| Application shell | `ftgo-application` |
| Database & tests | `ftgo-flyway`, `ftgo-end-to-end-tests`, `ftgo-end-to-end-tests-common` |

All modules share a single `settings.gradle`, a single Gradle build, and a
single deployable artifact (`ftgo-application`). The monolith composes all
four services via `@Import` in `FtgoApplicationMain`.

We need to transition to a microservices architecture where each bounded
context is independently deployable. The two strategies considered are:

1. **Multi-repo** — one GitHub repository per microservice.
2. **Mono-repo with service boundaries** — a single repository with clear
   directory boundaries between services, shared libraries, and infrastructure.

## Decision

We adopt a **mono-repo with service boundaries**.

The repository is reorganised into the following top-level directories:

```
ftgo-monolith/
├── services/                    # One sub-directory per bounded context
│   ├── consumer-service/
│   ├── order-service/
│   ├── restaurant-service/
│   └── courier-service/
├── libs/                        # Shared libraries (future extraction target)
├── infrastructure/              # Docker Compose, Kubernetes manifests
├── docs/                        # ADRs, conventions, runbooks
├── buildSrc/                    # Gradle plugins (existing)
├── gradle/                      # Gradle wrapper (existing)
└── <legacy modules>             # Existing modules retained during migration
```

Each service directory contains two Gradle sub-projects:

```
services/<name>-service/
├── <name>-service-api/          # API contracts (DTOs, events, request/response)
├── <name>-service-app/          # Application code (controllers, domain, config)
└── README.md
```

### Rationale

| Factor | Mono-repo | Multi-repo |
|--------|-----------|------------|
| Migration overhead | Lower — code can be moved incrementally | Higher — requires CI/CD per repo from day one |
| Cross-cutting refactors | Single commit across services | Coordinated PRs across repos |
| Shared library versioning | Source dependency via Gradle composite builds | Published artifacts with semver |
| Code review visibility | Full diff in one PR | Fragmented across repos |
| CI/CD complexity | Single pipeline with selective triggers | N pipelines from the start |
| Team autonomy | Enforced via CODEOWNERS and directory boundaries | Natural repo-level isolation |

During the migration period, the monolith modules coexist alongside the new
service directories. This allows incremental extraction: code is moved from
the legacy modules into the corresponding `services/` directory, verified, and
the legacy module is eventually removed.

Once all services are extracted and independently deployable, the team can
evaluate splitting into multi-repo if team autonomy demands it.

## Consequences

### Positive

- Incremental migration: the monolith build continues to pass throughout the
  transition because legacy modules are not removed until their replacement is
  verified.
- Shared libraries remain source dependencies during migration, avoiding the
  overhead of publishing artifacts prematurely.
- A single CI pipeline can build, test, and deploy all services with
  path-based triggers (e.g., GitHub Actions `paths` filter).
- Consistent tooling, linting, and dependency versions across all services.

### Negative

- Repository size will grow; mitigated by Git sparse checkout for developers
  who only work on one service.
- Risk of coupling if service boundaries are not enforced; mitigated by
  Gradle project dependency rules and CODEOWNERS.
- CI builds may become slow without path-based filtering; must be addressed
  when CI is configured (see EM-33).

### Risks

- Teams accustomed to multi-repo may find mono-repo unfamiliar. Mitigated by
  clear documentation and the template/archetype in `services/service-template/`.
- Merge conflicts in `settings.gradle` when multiple services are added
  concurrently. Mitigated by grouping includes by section with comments.
