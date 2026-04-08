# ADR-0001: Repository Structure and Naming Conventions for Microservices Migration

## Status

Proposed

## Date

2026-04-08

## Context

The FTGO application is currently a monolithic Spring Boot application with 14 Gradle modules in a flat structure. All services (Consumer, Order, Restaurant, Courier) are co-deployed in a single JVM process via `FtgoApplicationMain.java`, which uses `@Import` to compose all service configurations.

The current module structure:

```
ftgo-monolith/
├── ftgo-common/                  # Shared value objects (Money, Address, PersonName)
├── ftgo-common-jpa/              # Shared JPA configuration
├── ftgo-domain/                  # Shared JPA entities and repository interfaces
├── common-swagger/               # Shared Swagger configuration
├── ftgo-test-util/               # Shared test utilities
├── ftgo-order-service/           # Order business logic and controllers
├── ftgo-order-service-api/       # Order DTOs and API contracts
├── ftgo-consumer-service/        # Consumer business logic and controllers
├── ftgo-consumer-service-api/    # Consumer DTOs and API contracts
├── ftgo-restaurant-service/      # Restaurant business logic and controllers
├── ftgo-restaurant-service-api/  # Restaurant DTOs and API contracts
├── ftgo-courier-service/         # Courier business logic and controllers
├── ftgo-courier-service-api/     # Courier DTOs and API contracts
├── ftgo-application/             # Spring Boot entry point (composes all services)
├── ftgo-flyway/                  # Flyway database migrations (single shared schema)
├── ftgo-end-to-end-tests/        # E2E tests
└── ftgo-end-to-end-tests-common/ # Shared E2E test infrastructure
```

The package root is `net.chrisrichardson.ftgo` with per-service sub-packages (e.g., `net.chrisrichardson.ftgo.orderservice`).

As part of Phase 1 of the microservices migration, we need to define the target repository structure, directory layout, and naming conventions that will guide the entire migration effort.

### Decision Drivers

- **Incremental migration**: The monolith must continue running while services are extracted one at a time.
- **Shared code management**: Shared libraries (`ftgo-common`, `ftgo-domain`, etc.) must be accessible to both the legacy monolith and the new microservices during the transition period.
- **Build tooling**: The existing Gradle multi-project build must remain functional for legacy modules.
- **Team velocity**: Developers should be able to work on individual services without full-repo builds.
- **Clear boundaries**: Each bounded context (Consumer, Order, Restaurant, Courier) must have well-defined ownership boundaries.

## Decision

### 1. Repository Strategy: Mono-repo with Clear Boundaries

We will use a **structured mono-repo** approach with clearly separated service directories and shared libraries. This is chosen over multi-repo because:

- The migration is incremental — both legacy and new modules coexist in the same Gradle build.
- Shared libraries are still evolving and tightly coupled to service code.
- Cross-cutting changes (e.g., upgrading Spring Boot) are easier to coordinate in a single repo.
- The team is small enough that a mono-repo does not cause undue contention.

Once the migration is complete and services are fully independent, individual services **may** be extracted to separate repositories if the team decides the overhead is justified.

### 2. Directory Structure

The repository is organized into three top-level directories for microservice code, plus the legacy modules at the root:

```
ftgo-monolith/
│
├── services/                              # All microservice modules
│   ├── ftgo-order-service/                # Order bounded context — service
│   ├── ftgo-order-service-api/            # Order bounded context — API contracts
│   ├── ftgo-consumer-service/             # Consumer bounded context — service
│   ├── ftgo-consumer-service-api/         # Consumer bounded context — API contracts
│   ├── ftgo-restaurant-service/           # Restaurant bounded context — service
│   ├── ftgo-restaurant-service-api/       # Restaurant bounded context — API contracts
│   ├── ftgo-courier-service/              # Courier bounded context — service
│   ├── ftgo-courier-service-api/          # Courier bounded context — API contracts
│   ├── _service-template/                 # Template for creating new services
│   └── _service-template-api/             # Template for creating new API modules
│
├── shared/                                # Shared libraries (used by all services)
│   ├── ftgo-common/                       # Value objects: Money, Address, PersonName
│   ├── ftgo-common-jpa/                   # Shared JPA base classes and config
│   ├── ftgo-domain/                       # Shared JPA entities and repositories
│   ├── ftgo-test-util/                    # Shared test utilities
│   └── common-swagger/                    # Shared Swagger/OpenAPI configuration
│
├── docs/                                  # Architecture documentation
│   └── adr/                               # Architecture Decision Records
│
├── buildSrc/                              # Gradle build plugins
│
│  ── Legacy modules (unchanged during migration) ──
├── ftgo-application/                      # Monolith entry point
├── ftgo-common/                           # (legacy — will be migrated to shared/)
├── ftgo-common-jpa/                       # (legacy — will be migrated to shared/)
├── ftgo-domain/                           # (legacy — will be migrated to shared/)
├── ftgo-order-service/                    # (legacy — will be migrated to services/)
├── ftgo-order-service-api/                # (legacy)
├── ftgo-consumer-service/                 # (legacy)
├── ftgo-consumer-service-api/             # (legacy)
├── ftgo-restaurant-service/               # (legacy)
├── ftgo-restaurant-service-api/           # (legacy)
├── ftgo-courier-service/                  # (legacy)
├── ftgo-courier-service-api/              # (legacy)
├── ftgo-flyway/                           # (legacy — will be split per-service)
├── ftgo-end-to-end-tests/                 # (legacy)
├── ftgo-end-to-end-tests-common/          # (legacy)
├── ftgo-test-util/                        # (legacy)
└── common-swagger/                        # (legacy)
```

### 3. Per-Service Directory Layout

Each microservice follows a standardized internal structure:

```
services/ftgo-<name>-service/
├── build.gradle                           # Gradle build configuration
├── config/
│   └── application-local.yml              # Local development profile overrides
├── docker/
│   └── Dockerfile                         # Container image definition
├── k8s/
│   └── deployment.yaml                    # Kubernetes Deployment + Service manifests
└── src/
    ├── main/
    │   ├── java/com/ftgo/<name>/
    │   │   ├── config/                    # @Configuration classes
    │   │   ├── domain/                    # JPA @Entity classes, value objects
    │   │   ├── messaging/                 # Event publishers and consumers
    │   │   ├── repository/                # Spring Data @Repository interfaces
    │   │   ├── service/                   # @Service business logic
    │   │   └── web/                       # @RestController endpoints, DTOs
    │   └── resources/
    │       ├── application.yml            # Spring Boot configuration
    │       └── db/migration/              # Flyway migrations (per-service schema)
    ├── test/
    │   ├── java/com/ftgo/<name>/          # Unit tests (mirrors main structure)
    │   │   ├── domain/
    │   │   ├── service/
    │   │   └── web/
    │   └── resources/
    └── integration-test/
        ├── java/com/ftgo/<name>/          # Integration tests
        └── resources/
```

Each API module follows:

```
services/ftgo-<name>-service-api/
├── build.gradle
└── src/
    └── main/
        ├── java/com/ftgo/<name>/api/
        │   ├── web/                       # Request/response DTOs
        │   └── events/                    # Domain event definitions
        └── resources/
```

### 4. Package Naming Conventions

**New package root**: `com.ftgo`

This replaces the legacy `net.chrisrichardson.ftgo` root to reflect organizational ownership.

| Layer | Package Pattern | Example |
|-------|----------------|---------|
| Domain entities | `com.ftgo.<service>.domain` | `com.ftgo.order.domain.Order` |
| Business logic | `com.ftgo.<service>.service` | `com.ftgo.order.service.OrderService` |
| REST controllers | `com.ftgo.<service>.web` | `com.ftgo.order.web.OrderController` |
| Configuration | `com.ftgo.<service>.config` | `com.ftgo.order.config.OrderServiceConfiguration` |
| Repositories | `com.ftgo.<service>.repository` | `com.ftgo.order.repository.OrderRepository` |
| Messaging | `com.ftgo.<service>.messaging` | `com.ftgo.order.messaging.OrderEventPublisher` |
| API DTOs | `com.ftgo.<service>.api.web` | `com.ftgo.order.api.web.CreateOrderRequest` |
| API events | `com.ftgo.<service>.api.events` | `com.ftgo.order.api.events.OrderCreatedEvent` |
| Shared common | `com.ftgo.common` | `com.ftgo.common.Money` |
| Shared domain | `com.ftgo.domain` | `com.ftgo.domain.Order` |
| Shared JPA | `com.ftgo.common.jpa` | `com.ftgo.common.jpa.CommonJpaConfiguration` |

**Service name mapping** (service directory name → Java package name):

| Bounded Context | Service Directory | Package Name |
|-----------------|-------------------|--------------|
| Order | `ftgo-order-service` | `com.ftgo.order` |
| Consumer | `ftgo-consumer-service` | `com.ftgo.consumer` |
| Restaurant | `ftgo-restaurant-service` | `com.ftgo.restaurant` |
| Courier | `ftgo-courier-service` | `com.ftgo.courier` |

### 5. Module Naming Conventions for Gradle

| Type | Pattern | Example |
|------|---------|---------|
| Service implementation | `services:ftgo-<context>-service` | `services:ftgo-order-service` |
| Service API contracts | `services:ftgo-<context>-service-api` | `services:ftgo-order-service-api` |
| Shared library | `shared:<library-name>` | `shared:ftgo-common` |
| Legacy module | `<module-name>` (flat, at root) | `ftgo-order-service` |

Gradle project references use colon-separated paths:

```groovy
// New microservice modules (nested under services/ or shared/)
compile project(":services:ftgo-order-service-api")
compile project(":shared:ftgo-common")

// Legacy modules (flat at root — unchanged)
compile project(":ftgo-order-service-api")
compile project(":ftgo-common")
```

### 6. Naming Rules

- **Service directories**: Always prefixed with `ftgo-` and suffixed with `-service` or `-service-api`.
- **Bounded context name**: Use the singular domain noun (e.g., `order`, not `orders`).
- **Package names**: Lowercase, no hyphens. Use the singular bounded context name (e.g., `com.ftgo.order`, not `com.ftgo.orderservice`).
- **Configuration classes**: Suffixed with `Configuration` (e.g., `OrderServiceConfiguration`).
- **Service classes**: Suffixed with `Service` (e.g., `OrderService`).
- **Controller classes**: Suffixed with `Controller` (e.g., `OrderController`).
- **Repository interfaces**: Suffixed with `Repository` (e.g., `OrderRepository`).
- **DTOs**: Suffixed with `Request`/`Response` (e.g., `CreateOrderRequest`, `GetOrderResponse`).
- **Events**: Suffixed with `Event` (e.g., `OrderCreatedEvent`, `OrderCancelledEvent`).

### 7. Database Naming

Each microservice owns its own database schema:

| Service | Database Name |
|---------|---------------|
| Order Service | `ftgo_order_service` |
| Consumer Service | `ftgo_consumer_service` |
| Restaurant Service | `ftgo_restaurant_service` |
| Courier Service | `ftgo_courier_service` |

Flyway migrations are stored per-service at `src/main/resources/db/migration/`.

### 8. Docker Image Naming

```
ftgo/ftgo-<service-name>:<version>
```

Examples:
- `ftgo/ftgo-order-service:1.0.0`
- `ftgo/ftgo-consumer-service:latest`

### 9. Template Repository

A service template is provided at `services/_service-template/` (and `services/_service-template-api/` for the API module). New services should be created by copying this template and following the instructions in its `README.md`.

The template includes:
- Standard directory layout with all package directories
- Pre-configured `build.gradle` with common dependencies
- Dockerfile with health check
- Kubernetes Deployment + Service manifests
- `application.yml` with sensible defaults
- Local development profile (`application-local.yml`)

## Consequences

### Positive

- **Coexistence**: Legacy and new modules live side-by-side, enabling incremental migration without disrupting the existing build.
- **Consistency**: All services follow identical directory layouts, package naming, and build conventions.
- **Discoverability**: The `services/` and `shared/` directories clearly communicate code organization to new developers.
- **Template**: New services can be scaffolded quickly by copying the template.
- **Clear ownership**: Each bounded context has a designated directory with a well-defined package boundary.

### Negative

- **Duplication during transition**: Parallel `services/` and root-level directories for the same service create temporary duplication. This is intentional and will be resolved as each service is migrated.
- **Mono-repo overhead**: All services share a single CI pipeline and Git history. This is acceptable for the current team size but may need revisiting if the team grows significantly.
- **Package rename**: Moving from `net.chrisrichardson.ftgo` to `com.ftgo` requires careful migration of imports during the service extraction phase.

### Migration Path

1. **Phase 1 (current)**: Create the target directory structure with placeholder files. Legacy modules remain unchanged and functional.
2. **Phase 2**: Extract shared libraries by moving source code from legacy `ftgo-common`, `ftgo-domain`, etc. into `shared/` and updating `settings.gradle`.
3. **Phase 3**: Extract each service by moving source code from legacy `ftgo-<service>` into `services/ftgo-<service>` and updating package declarations.
4. **Phase 4**: Remove legacy modules once all services are migrated and the monolith entry point (`ftgo-application`) is no longer needed.

## References

- [Microservices Patterns](https://microservices.io/patterns/) — Chris Richardson
- [Mono-repo vs Multi-repo](https://earthly.dev/blog/monorepo-vs-polyrepo/) — Earthly
- [Spring Boot Microservice Best Practices](https://docs.spring.io/spring-boot/docs/current/reference/html/)
