# ADR-0001: Repository Structure for the FTGO Microservices Migration

- **Status**: Accepted
- **Date**: 2026-04-21
- **Deciders**: FTGO Platform / Architecture team
- **Related**: EM-30 (this ADR), EM-28, EM-31, EM-32

## Context

The FTGO ("Food To Go") codebase is currently a single Gradle multi-project
Spring Boot monolith. `settings.gradle` includes 14 modules under the
repository root in a flat layout:

- **Shared code**: `ftgo-common`, `ftgo-common-jpa`, `ftgo-domain`,
  `common-swagger`, `ftgo-test-util`.
- **Services**: `ftgo-order-service`, `ftgo-consumer-service`,
  `ftgo-restaurant-service`, `ftgo-courier-service`.
- **Service API contracts**: `ftgo-*-service-api` for each of the four
  services.
- **Application**: `ftgo-application` (composes every service via `@Import`
  in `FtgoApplicationMain.java`).
- **Database / tests**: `ftgo-flyway`, `ftgo-end-to-end-tests`,
  `ftgo-end-to-end-tests-common`.

The microservices migration (tracked under the **EM-28 … EM-49** epic)
requires us to peel these modules apart into independently deployable units
while still allowing incremental migration. Before any code moves, we have to
agree on **where** it moves to, **what** it is called, and **how** it is
laid out on disk.

The forces in play:

1. **Incremental migration.** We cannot "big bang" — the monolith must keep
   building and running throughout the migration.
2. **Independent lifecycle per service.** Each bounded context eventually
   needs its own release cadence, Dockerfile, k8s manifests, and owning
   team.
3. **Shared code exists and is stable.** `Money`, `PersonName`, JPA helpers,
   etc. are genuinely reusable and should not be duplicated across four
   services.
4. **Cross-cutting platform concerns** (gateway, discovery, observability,
   config) are not owned by any single bounded context.
5. **Tooling lock-in.** CI/CD, CodeOwners, Dependabot, release automation
   all key off of repository layout; we want a layout that is easy to
   reason about from the start.

## Decision

We adopt a **mono-repo with clear top-level boundaries** rather than
splitting into per-service Git repositories. The repository root is
reorganized as follows:

```
ftgo-monolith/
├── services/             # One directory per deployable microservice
│   ├── consumer-service/
│   ├── order-service/
│   ├── restaurant-service/
│   └── courier-service/
├── libs/                 # Reusable JVM libraries shared across services
│   ├── ftgo-common/
│   ├── ftgo-common-jpa/
│   └── ftgo-test-util/
├── platform/             # Cross-cutting infrastructure owned by Platform
│   ├── api-gateway/
│   ├── config-server/
│   ├── service-discovery/
│   ├── observability/
│   └── shared-infrastructure/
├── templates/
│   └── service-template/ # Canonical layout to clone for new services
├── docs/
│   └── adr/              # This directory
├── CONVENTIONS.md        # Naming and layout rules (normative)
└── settings.gradle       # Registers services/* and libs/* as subprojects
```

Each service directory follows a fixed internal layout (see
`templates/service-template/` and `CONVENTIONS.md`) and owns its own
`build.gradle`, `Dockerfile`, Kubernetes manifests, and configuration.

The pre-existing flat modules (`ftgo-order-service`, `ftgo-common`, etc.)
**remain at the repository root** for now. They will be migrated into
`services/` and `libs/` incrementally by subsequent tickets (EM-28, EM-31,
EM-32). During the overlap period, `settings.gradle` registers both the
legacy modules and the new scaffolds.

## Consequences

### Positive

- **Atomic cross-service changes.** Renaming a shared type, evolving an API
  contract, or rolling out a platform change is one PR instead of N.
- **Single source of truth for conventions.** `CONVENTIONS.md` and
  `templates/service-template/` mean "how do we lay out a new service?"
  has exactly one answer.
- **Cheap service creation.** A new bounded context is a `cp -r` of the
  template plus one entry in `settings.gradle`.
- **Clear ownership via directories.** CODEOWNERS can map
  `services/order-service/` to the Order team without repo-splitting
  overhead.
- **Incremental migration is safe.** Legacy modules stay functional while
  their replacements are scaffolded alongside.

### Negative

- **Build time grows with every service.** We accept this; per-service
  tasks (e.g. `./gradlew :services:order-service:bootJar`) keep the
  common case fast, and CI will shard by module.
- **Weaker deployment isolation than multi-repo.** A bad merge can still
  break other services' builds. Mitigated by per-module CI jobs and by
  the fact that Git history is preserved intact.
- **Harder to open-source or extract a single service later.** Acceptable:
  FTGO is internal and extraction would be the exception, not the rule.

### Neutral

- Module paths change (`:ftgo-order-service` → `:services:order-service`).
  Downstream tooling (scripts, Docker Compose files) will be updated when
  each service migrates; until then, references to legacy modules keep
  working.

## Alternatives Considered

### A. Multi-repo (one Git repo per service)

- **Pros**: Strongest deployment isolation; independent permissions,
  branch protection, and release cadence; scales to dozens of teams.
- **Cons**: Cross-cutting changes require coordinated PRs across N repos;
  shared libraries become a separate versioning/publishing problem;
  onboarding cost (each new service = new repo creation + CI setup +
  CODEOWNERS); dependency upgrades (e.g. Spring Boot bump) fan out to N
  PRs; hard to make atomic refactors during the migration itself.
- **Verdict**: Rejected for now. FTGO has four services and one platform
  team. The coordination cost of multi-repo outweighs its isolation
  benefit at this scale. Revisit if service count grows past ~10 or if
  teams diverge significantly in tooling needs.

### B. Keep the current flat layout

- **Pros**: Zero migration cost.
- **Cons**: Does not scale: `settings.gradle` is already 14 modules and
  every new service adds two more (`*-service` and `*-service-api`).
  There is no clear place for platform code (gateway, observability)
  or non-Java artifacts (k8s manifests, Dockerfiles per service).
  Makes CODEOWNERS and module ownership ambiguous.
- **Verdict**: Rejected. The flat layout is what motivated this ADR.

### C. Mono-repo with a Bazel / Pants / Nx layer

- **Pros**: Finer-grained caching and remote build execution.
- **Cons**: Significant tooling investment; team does not have prior
  experience; Gradle is the current source of truth and works fine.
- **Verdict**: Out of scope. Revisit if Gradle build time becomes a
  bottleneck.

## Follow-up

- **EM-28**: Create a shared parent Gradle configuration (convention plugin)
  that `services/*` and `libs/*` inherit from.
- **EM-32**: Migrate `ftgo-common` into `libs/ftgo-common`.
- **EM-31**: Migrate `ftgo-common-jpa` and relevant pieces of `ftgo-domain`
  into `libs/`.
- **EM-33 / EM-36**: Configure CI to run module-scoped builds and tests.
- **EM-34 / EM-35**: Wire `services/*/docker/` and `services/*/k8s/` into
  the container registry and deployment pipelines.
- **Per-service migration tickets**: Move each of the four legacy modules
  into its `services/<name>-service/` counterpart. Flip `settings.gradle`
  to reference only the new path. Retire the legacy module entries.

Until the migration completes, the legacy flat modules remain the
authoritative source of business logic. The new directories under
`services/`, `libs/`, and `platform/` are scaffolds that define the target
shape.
