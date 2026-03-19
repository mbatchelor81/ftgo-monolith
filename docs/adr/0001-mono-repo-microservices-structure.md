# ADR-0001: Mono-Repo Structure for Microservices Migration

## Status

Accepted

## Date

2026-03-19

## Context

The FTGO ("Food To Go") application is currently a Spring Boot monolith with 14 Gradle modules in a flat directory structure. The monolith bundles four bounded contexts — Consumer, Order, Restaurant, and Courier — into a single deployable unit.

We need to migrate this monolith to microservices. A key architectural decision is how to organize the repository structure during and after the migration. The two primary options are:

1. **Mono-repo**: All microservices and shared libraries live in a single repository with clear directory boundaries.
2. **Multi-repo**: Each microservice gets its own dedicated repository.

### Current Monolith Structure

```
ftgo-monolith/
├── ftgo-common/                    # Shared value objects (Money, Address, PersonName)
├── ftgo-common-jpa/                # Shared JPA utilities
├── ftgo-domain/                    # Shared JPA entities and repositories
├── common-swagger/                 # Swagger configuration
├── ftgo-test-util/                 # Test utilities
├── ftgo-order-service/             # Order business logic and controllers
├── ftgo-order-service-api/         # Order DTOs and API contracts
├── ftgo-consumer-service/          # Consumer business logic and controllers
├── ftgo-consumer-service-api/      # Consumer DTOs
├── ftgo-restaurant-service/        # Restaurant business logic and controllers
├── ftgo-restaurant-service-api/    # Restaurant DTOs and events
├── ftgo-courier-service/           # Courier business logic and controllers
├── ftgo-courier-service-api/       # Courier DTOs
├── ftgo-application/               # Monolith entry point (@Import all services)
├── ftgo-flyway/                    # Flyway DB migrations
├── ftgo-end-to-end-tests/          # E2E tests
└── ftgo-end-to-end-tests-common/   # Shared E2E test infrastructure
```

Package root: `net.chrisrichardson.ftgo`

## Decision

We will use a **mono-repo approach** with a clear two-level directory structure separating microservice modules from legacy monolith modules.

### Repository Layout

```
ftgo-monolith/
│
├── services/                           # NEW: All microservice modules
│   ├── ftgo-consumer-service/          # Consumer bounded context
│   │   ├── src/main/java/com/ftgo/consumer/
│   │   │   ├── ConsumerServiceApplication.java
│   │   │   ├── config/                 # Spring @Configuration classes
│   │   │   ├── domain/                 # Business logic and @Service classes
│   │   │   ├── web/                    # @RestController endpoints
│   │   │   ├── dto/                    # Request/response DTOs
│   │   │   └── exception/             # Custom exceptions
│   │   ├── src/main/resources/
│   │   │   └── application.yml
│   │   ├── src/test/java/com/ftgo/consumer/
│   │   ├── docker/
│   │   │   └── Dockerfile
│   │   ├── k8s/
│   │   │   └── deployment.yml
│   │   └── build.gradle
│   ├── ftgo-consumer-service-api/      # Consumer API contracts
│   ├── ftgo-order-service/             # Order bounded context
│   ├── ftgo-order-service-api/         # Order API contracts
│   ├── ftgo-restaurant-service/        # Restaurant bounded context
│   ├── ftgo-restaurant-service-api/    # Restaurant API contracts
│   ├── ftgo-courier-service/           # Courier bounded context
│   └── ftgo-courier-service-api/       # Courier API contracts
│
├── shared/                             # NEW: Shared libraries for microservices
│   ├── ftgo-common/                    # Value objects, serialization, utilities
│   ├── ftgo-common-jpa/                # JPA infrastructure
│   └── ftgo-domain/                    # Shared domain entities (transition period)
│
├── docs/                               # NEW: Architecture documentation
│   └── adr/                            # Architecture Decision Records
│
├── ftgo-common/                        # LEGACY: Existing monolith modules
├── ftgo-common-jpa/                    #   (unchanged, kept intact during migration)
├── ftgo-domain/                        #
├── ftgo-order-service/                 #
├── ftgo-consumer-service/              #
├── ftgo-restaurant-service/            #
├── ftgo-courier-service/               #
├── ftgo-application/                   #
├── ...                                 #
│
├── settings.gradle                     # Updated: includes both legacy and new modules
├── build.gradle                        # Updated: configures both legacy and new modules
└── gradle.properties                   # Shared Gradle properties
```

### Module Naming Convention

To avoid naming collisions between legacy modules and new microservice modules in the Gradle project namespace:

| Module Type | Gradle Name Pattern | Directory | Example |
|---|---|---|---|
| Legacy monolith | `:<module-name>` | `<module-name>/` | `:ftgo-order-service` |
| New shared library | `:shared-<lib-name>` | `shared/<lib-name>/` | `:shared-ftgo-common` |
| New service | `:services-<svc-name>` | `services/<svc-name>/` | `:services-ftgo-order-service` |
| New service API | `:services-<svc-name>-api` | `services/<svc-name>-api/` | `:services-ftgo-order-service-api` |

### Package Naming Convention

New microservice modules use a new package root to clearly distinguish them from legacy code:

| Component | Package Pattern | Example |
|---|---|---|
| Service main | `com.ftgo.<context>` | `com.ftgo.order` |
| Domain layer | `com.ftgo.<context>.domain` | `com.ftgo.order.domain` |
| Web layer | `com.ftgo.<context>.web` | `com.ftgo.order.web` |
| Configuration | `com.ftgo.<context>.config` | `com.ftgo.order.config` |
| DTOs | `com.ftgo.<context>.dto` | `com.ftgo.order.dto` |
| Exceptions | `com.ftgo.<context>.exception` | `com.ftgo.order.exception` |
| API contracts | `com.ftgo.<context>.api` | `com.ftgo.order.api` |
| Shared common | `com.ftgo.common` | `com.ftgo.common` |
| Shared JPA | `com.ftgo.common.jpa` | `com.ftgo.common.jpa` |
| Shared domain | `com.ftgo.common.domain` | `com.ftgo.common.domain` |

Legacy monolith code retains the existing `net.chrisrichardson.ftgo` package root.

### Service Port Assignments

| Service | Port |
|---|---|
| Consumer Service | 8081 |
| Order Service | 8082 |
| Restaurant Service | 8083 |
| Courier Service | 8084 |

### Build Configuration

- Legacy modules use `group = "net.chrisrichardson.ftgo"` and the existing `compile`/`testCompile` dependency syntax.
- New modules use `group = "com.ftgo"` and the modern `implementation`/`testImplementation` dependency syntax.
- All new modules use the Spring Boot dependency management BOM for version alignment.
- New service modules apply the `spring-boot` and `dependency-management` plugins.
- New API/shared library modules apply the `java-library` plugin for proper API exposure.

## Rationale

### Why Mono-Repo

1. **Migration continuity**: The monolith already uses a multi-module Gradle layout. A mono-repo preserves this pattern and allows the monolith and new microservices to coexist during the incremental migration.

2. **Atomic cross-service changes**: During migration, changes frequently span multiple services and shared libraries. A mono-repo enables atomic commits across service boundaries.

3. **Shared build infrastructure**: All services share the same Gradle wrapper, root build configuration, and CI pipeline. This reduces per-service build maintenance overhead.

4. **Simplified dependency management**: Shared libraries (`shared/ftgo-common`, etc.) can be referenced as Gradle project dependencies without publishing to a separate artifact repository.

5. **Easier code review**: Reviewers can see the full impact of a change across services in a single PR.

6. **Incremental extraction**: Services can be extracted to their own repositories later if the team outgrows the mono-repo. The clear directory boundaries (`services/<name>/`) make this extraction straightforward.

### Why Separate `services/` and `shared/` Directories

1. **Clear ownership**: Each service directory is self-contained and could be extracted to its own repository.
2. **Deployment independence**: Only the contents of a service directory need to be built and deployed for that service.
3. **No accidental coupling**: The directory structure makes it physically harder to create imports between unrelated services.

### Why New Package Root (`com.ftgo`)

1. **Clean separation**: `com.ftgo.*` clearly distinguishes new microservice code from legacy `net.chrisrichardson.ftgo.*` monolith code.
2. **No classpath conflicts**: During the transition period, both package hierarchies can coexist without ambiguity.
3. **Industry standard**: `com.ftgo` follows Java package naming conventions for the organization.

## Consequences

### Positive

- Legacy monolith continues to build and run without any changes.
- New services have a clean, standardized structure ready for independent deployment.
- The migration can proceed incrementally — one bounded context at a time.
- All 4 bounded contexts (Consumer, Order, Restaurant, Courier) have designated directories.

### Negative

- The repository will temporarily contain two copies of similar code (legacy modules + new service modules) during migration.
- Gradle project names for new modules are longer due to the `services-`/`shared-` prefix.
- The `settings.gradle` file is more complex with explicit `projectDir` mappings.

### Risks

- Teams must be disciplined about not creating dependencies from legacy modules to new modules or vice versa.
- The shared domain library (`shared/ftgo-domain`) should be treated as temporary — each service should eventually own its own domain entities.

## References

- [Microservices Patterns](https://microservices.io/book) by Chris Richardson
- [Mono-repo vs Multi-repo](https://microservices.io/post/architecture/2023/02/09/assemblage-architecture.html)
- Original monolith: [ftgo-application](https://github.com/microservices-patterns/ftgo-application)
