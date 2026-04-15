# ADR-0001: Microservices Repository Structure and Naming Conventions

**Status:** Accepted

**Date:** 2026-04-15

**Deciders:** FTGO Platform Team

**Context:** EM-30 — Microservices Migration

---

## Context

The FTGO application is currently a Spring Boot monolith with 14 Gradle
submodules in a flat structure. The monolith bundles four bounded contexts
(Consumer, Order, Restaurant, Courier) into a single deployable unit composed
via `@Import` in `FtgoApplicationMain.java`.

The current module layout:

```
ftgo-monolith/
├── ftgo-common/                    # Shared value objects (Money, Address, PersonName)
├── ftgo-common-jpa/                # Shared JPA configuration
├── ftgo-domain/                    # All JPA entities and repositories (shared)
├── common-swagger/                 # Swagger configuration
├── ftgo-test-util/                 # Test utilities
├── ftgo-order-service/             # Order business logic + controllers
├── ftgo-order-service-api/         # Order DTOs and API contracts
├── ftgo-consumer-service/          # Consumer business logic + controllers
├── ftgo-consumer-service-api/      # Consumer DTOs and API contracts
├── ftgo-restaurant-service/        # Restaurant business logic + controllers
├── ftgo-restaurant-service-api/    # Restaurant DTOs and API contracts
├── ftgo-courier-service/           # Courier business logic + controllers
├── ftgo-courier-service-api/       # Courier DTOs and API contracts
├── ftgo-application/               # Spring Boot entry point (composes all services)
├── ftgo-flyway/                    # Flyway database migrations (single shared DB)
├── ftgo-end-to-end-tests/          # End-to-end tests
└── ftgo-end-to-end-tests-common/   # Shared E2E test infrastructure
```

Package root: `net.chrisrichardson.ftgo`

We need to define the target repository structure that supports independent
service development, deployment, and scaling while enabling a smooth incremental
migration from the monolith.

## Decision

### 1. Repository Strategy: Mono-repo with Service Folders

We will use a **mono-repo** approach with a `services/` top-level directory
containing each microservice as an isolated Gradle subproject.

**Rationale:**
- **Incremental migration**: The monolith modules remain at the repo root
  during migration. New microservice code lives under `services/`. Both can
  coexist and build together.
- **Atomic cross-service changes**: During migration, refactoring that touches
  multiple services (e.g., extracting shared domain types) can be done in a
  single commit/PR.
- **Shared build infrastructure**: Common Gradle plugins, CI pipelines, and
  dependency management can be shared across services without publishing
  artifacts to an external registry.
- **Simplified onboarding**: One `git clone` gives developers the entire
  platform.

**Trade-offs acknowledged:**
- Build times grow with number of services (mitigated by Gradle build cache and
  task-level parallelism).
- Requires discipline to maintain service boundaries (enforced by package
  structure and dependency rules).
- Can be split into multi-repo later if needed; mono-repo → multi-repo is
  easier than the reverse.

### 2. Directory Structure

```
ftgo-monolith/                              # Repository root
│
├── [existing monolith modules]             # Remain at root during migration
│   ├── ftgo-application/
│   ├── ftgo-order-service/
│   ├── ftgo-consumer-service/
│   ├── ftgo-restaurant-service/
│   ├── ftgo-courier-service/
│   ├── ftgo-*-api/
│   ├── ftgo-domain/
│   ├── ftgo-common/
│   ├── ftgo-common-jpa/
│   ├── ftgo-flyway/
│   └── ...
│
├── services/                               # NEW: Microservices home
│   ├── ftgo-common/                        # Shared library (value objects, utilities)
│   │   ├── build.gradle
│   │   └── src/main/java/com/ftgo/common/
│   │
│   ├── ftgo-common-jpa/                    # Shared JPA configuration library
│   │   ├── build.gradle
│   │   └── src/main/java/com/ftgo/common/jpa/
│   │
│   ├── ftgo-consumer-service/              # Consumer bounded context
│   │   ├── build.gradle
│   │   ├── docker/Dockerfile
│   │   ├── k8s/deployment.yaml
│   │   ├── README.md
│   │   └── src/
│   │       ├── main/
│   │       │   ├── java/com/ftgo/consumerservice/
│   │       │   │   ├── config/
│   │       │   │   ├── controller/
│   │       │   │   ├── service/
│   │       │   │   ├── repository/
│   │       │   │   ├── model/
│   │       │   │   ├── dto/
│   │       │   │   └── exception/
│   │       │   └── resources/
│   │       │       ├── application.yml
│   │       │       └── db/migration/
│   │       └── test/
│   │           ├── java/com/ftgo/consumerservice/
│   │           │   ├── controller/
│   │           │   ├── service/
│   │           │   └── repository/
│   │           └── resources/
│   │
│   ├── ftgo-order-service/                 # Order bounded context
│   │   └── [same layout as consumer-service]
│   │
│   ├── ftgo-restaurant-service/            # Restaurant bounded context
│   │   └── [same layout as consumer-service]
│   │
│   ├── ftgo-courier-service/               # Courier bounded context
│   │   └── [same layout as consumer-service]
│   │
│   └── ftgo-service-template/              # Template for creating new services
│       ├── build.gradle
│       ├── docker/Dockerfile
│       ├── k8s/deployment.yaml
│       ├── README.md
│       └── src/
│           ├── main/java/com/ftgo/SERVICENAME/
│           └── test/java/com/ftgo/SERVICENAME/
│
├── docs/
│   └── adr/
│       └── 0001-microservices-repository-structure.md  # This document
│
├── settings.gradle                         # Registers both legacy + new modules
├── build.gradle                            # Root build configuration
└── README.md
```

### 3. Package Naming Convention

**New package root:** `com.ftgo`

| Layer         | Package Pattern                             | Example                                    |
|---------------|---------------------------------------------|--------------------------------------------|
| Config        | `com.ftgo.<service>.config`                 | `com.ftgo.orderservice.config`             |
| Controllers   | `com.ftgo.<service>.controller`             | `com.ftgo.orderservice.controller`         |
| Service       | `com.ftgo.<service>.service`                | `com.ftgo.orderservice.service`            |
| Repository    | `com.ftgo.<service>.repository`             | `com.ftgo.orderservice.repository`         |
| Model         | `com.ftgo.<service>.model`                  | `com.ftgo.orderservice.model`              |
| DTO           | `com.ftgo.<service>.dto`                    | `com.ftgo.orderservice.dto`                |
| Exception     | `com.ftgo.<service>.exception`              | `com.ftgo.orderservice.exception`          |
| Shared lib    | `com.ftgo.common`                           | `com.ftgo.common.Money`                    |
| Shared JPA    | `com.ftgo.common.jpa`                       | `com.ftgo.common.jpa.AuditEntity`          |

**Rules:**
- Service names in packages use **camelCase with no hyphens**:
  `consumerservice`, `orderservice`, `restaurantservice`, `courierservice`.
- The legacy `net.chrisrichardson.ftgo` package root remains for monolith
  modules during migration. New code uses `com.ftgo`.
- Each service's `@SpringBootApplication` class lives at the service package
  root (e.g., `com.ftgo.orderservice.OrderServiceApplication`).

### 4. Module Naming Conventions (Gradle Subprojects)

| Type             | Pattern                              | Example                                    |
|------------------|--------------------------------------|--------------------------------------------|
| Service          | `services:ftgo-<context>-service`    | `services:ftgo-order-service`              |
| Shared library   | `services:ftgo-common`               | `services:ftgo-common`                     |
| Shared JPA       | `services:ftgo-common-jpa`           | `services:ftgo-common-jpa`                 |
| Legacy module    | `ftgo-<module>`                      | `ftgo-order-service` (root-level)          |

**Dependency declarations** in `build.gradle`:
```groovy
// New microservice depending on shared library
implementation project(':services:ftgo-common')

// Legacy monolith inter-module dependency (unchanged)
compile project(':ftgo-domain')
```

### 5. Resource Naming Conventions

| Resource           | Pattern                         | Example                          |
|--------------------|---------------------------------|----------------------------------|
| Docker image       | `ftgo/<service-name>`           | `ftgo/order-service`             |
| Database name      | `ftgo_<service_name>`           | `ftgo_order_service`             |
| K8s Deployment     | `ftgo-<service-name>`           | `ftgo-order-service`             |
| K8s Service        | `ftgo-<service-name>`           | `ftgo-order-service`             |
| Spring app name    | `ftgo-<service-name>`           | `ftgo-order-service`             |
| Config file        | `application.yml`               | Per-service in `src/main/resources/` |

### 6. Service Template

A template service (`services/ftgo-service-template/`) is provided as an
archetype. To create a new service:

1. Copy `services/ftgo-service-template/` to `services/ftgo-<new-service>/`
2. Replace all occurrences of `SERVICENAME` with the actual service name
3. Register the module in `settings.gradle`

See `services/ftgo-service-template/README.md` for detailed instructions.

## Consequences

### Positive
- Clear separation of bounded contexts with enforced directory boundaries
- Standard, predictable structure for every service (easy onboarding)
- Shared build infrastructure reduces duplication
- Template enables rapid bootstrapping of new services
- Monolith and microservices coexist during incremental migration
- Package naming eliminates cross-service import ambiguity

### Negative
- Mono-repo requires build-tool discipline (Gradle task graphs grow)
- Two package roots coexist during migration (`net.chrisrichardson.ftgo` and
  `com.ftgo`) which may cause confusion
- `services/` directory names shadow monolith root-level module names
  (intentional — makes the migration path explicit)

### Risks
- If services are not kept independent, the mono-repo could degenerate into a
  distributed monolith. Mitigated by enforcing clear dependency rules in
  `build.gradle` files and code review.

## References

- [Microservices Patterns](https://microservices.io/book) by Chris Richardson
- [Mono-repo vs Multi-repo](https://microservices.io/post/refactoring/2024/06/19/why-a-monorepo-for-microservices.html)
- Original monolith: `settings.gradle` (14 modules)
