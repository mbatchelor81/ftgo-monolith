# FTGO Shared Libraries

This directory holds reusable libraries that are consumed by multiple
microservices under `services/`. Libraries here are **pure code** — they
must not contain Spring Boot application entry points, REST controllers,
or service-specific business logic.

Each library is an independent Gradle subproject, produces its own JAR, and
is versioned independently from the services that consume it.

## Planned Modules

| Module              | Purpose                                                         |
|---------------------|-----------------------------------------------------------------|
| `ftgo-common`       | Cross-cutting value objects (`Money`, `PersonName`, `Address`). |
| `ftgo-common-jpa`   | Shared JPA configuration, base entities, auditing helpers.      |
| `ftgo-test-util`    | Test fixtures, assertion helpers, in-memory infra helpers.      |

These currently live at the repository root (`ftgo-common/`,
`ftgo-common-jpa/`, `ftgo-test-util/`). They will be migrated here as part of
EM-32 / EM-31.

## When to add a new library

A new library is justified when:

1. **Two or more services** legitimately need the same code.
2. The code is **stable** (low change rate relative to its consumers).
3. The library has **no dependency** on a specific service's domain. If it
   does, it belongs in that service's `*-api` module instead.

If any of those conditions is not met, inline the code in each service
instead — libraries carry a real coordination cost.

## Conventions

- Gradle project path: `:libs:<name>`
- Java root package: `com.ftgo.<name>` (no service name suffix).
- Libraries only depend on other `libs/*` modules or third-party artifacts,
  never on `services/*` modules.

See [`../CONVENTIONS.md`](../CONVENTIONS.md) for the full convention list.
