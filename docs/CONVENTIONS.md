# FTGO Microservices — Conventions Guide

This document defines the naming conventions, directory structure standards, and development guidelines for the FTGO microservices platform.

For the full rationale behind these decisions, see [ADR-0001](adr/0001-repository-structure-and-naming-conventions.md).

---

## Table of Contents

1. [Repository Layout](#repository-layout)
2. [Package Naming](#package-naming)
3. [Module Naming (Gradle)](#module-naming-gradle)
4. [Class Naming](#class-naming)
5. [Database Naming](#database-naming)
6. [Docker Image Naming](#docker-image-naming)
7. [Configuration Files](#configuration-files)
8. [Creating a New Service](#creating-a-new-service)

---

## Repository Layout

```
ftgo-monolith/
├── services/                          # Microservice modules
│   ├── ftgo-<name>-service/           # Service implementation
│   ├── ftgo-<name>-service-api/       # API contracts (DTOs, events)
│   └── _service-template/             # Template for new services
├── shared/                            # Shared libraries
│   ├── ftgo-common/                   # Value objects (Money, Address)
│   ├── ftgo-common-jpa/               # JPA base configuration
│   ├── ftgo-domain/                   # Shared entities & repositories
│   ├── ftgo-test-util/                # Test utilities
│   └── common-swagger/                # Swagger/OpenAPI config
├── docs/                              # Documentation
│   └── adr/                           # Architecture Decision Records
├── buildSrc/                          # Gradle plugins
└── <legacy modules at root>/          # Monolith modules (during migration)
```

### Per-Service Layout

```
services/ftgo-<name>-service/
├── build.gradle
├── config/                            # Profile-specific config overrides
│   └── application-local.yml
├── docker/
│   └── Dockerfile
├── k8s/
│   └── deployment.yaml
└── src/
    ├── main/
    │   ├── java/com/ftgo/<name>/
    │   │   ├── config/                # @Configuration classes
    │   │   ├── domain/                # @Entity classes, value objects
    │   │   ├── messaging/             # Event publishers/consumers
    │   │   ├── repository/            # @Repository interfaces
    │   │   ├── service/               # @Service business logic
    │   │   └── web/                   # @RestController, request/response DTOs
    │   └── resources/
    │       ├── application.yml
    │       └── db/migration/          # Flyway scripts (per-service)
    ├── test/java/com/ftgo/<name>/     # Unit tests
    └── integration-test/java/com/ftgo/<name>/  # Integration tests
```

---

## Package Naming

**Root package**: `com.ftgo`

| Layer | Pattern | Example |
|-------|---------|---------|
| Domain | `com.ftgo.<service>.domain` | `com.ftgo.order.domain.Order` |
| Service | `com.ftgo.<service>.service` | `com.ftgo.order.service.OrderService` |
| Web | `com.ftgo.<service>.web` | `com.ftgo.order.web.OrderController` |
| Config | `com.ftgo.<service>.config` | `com.ftgo.order.config.OrderServiceConfiguration` |
| Repository | `com.ftgo.<service>.repository` | `com.ftgo.order.repository.OrderRepository` |
| Messaging | `com.ftgo.<service>.messaging` | `com.ftgo.order.messaging.OrderEventPublisher` |
| API DTOs | `com.ftgo.<service>.api.web` | `com.ftgo.order.api.web.CreateOrderRequest` |
| API Events | `com.ftgo.<service>.api.events` | `com.ftgo.order.api.events.OrderCreatedEvent` |
| Shared | `com.ftgo.common` | `com.ftgo.common.Money` |

### Bounded Context → Package Mapping

| Bounded Context | Directory | Package |
|-----------------|-----------|---------|
| Order | `ftgo-order-service` | `com.ftgo.order` |
| Consumer | `ftgo-consumer-service` | `com.ftgo.consumer` |
| Restaurant | `ftgo-restaurant-service` | `com.ftgo.restaurant` |
| Courier | `ftgo-courier-service` | `com.ftgo.courier` |

### Rules

- Use **singular** nouns for bounded context names: `order`, not `orders`.
- Package names are **lowercase** with no hyphens or underscores.
- Service package names use the **domain noun only**: `com.ftgo.order`, not `com.ftgo.orderservice`.

---

## Module Naming (Gradle)

| Type | Pattern | Example |
|------|---------|---------|
| Service | `services:ftgo-<context>-service` | `services:ftgo-order-service` |
| API | `services:ftgo-<context>-service-api` | `services:ftgo-order-service-api` |
| Shared lib | `shared:<name>` | `shared:ftgo-common` |
| Legacy | `<name>` (root) | `ftgo-order-service` |

### Dependency References

```groovy
// Microservice modules
compile project(":services:ftgo-order-service-api")
compile project(":shared:ftgo-common")

// Legacy modules (unchanged during migration)
compile project(":ftgo-common")
```

---

## Class Naming

| Type | Suffix | Example |
|------|--------|---------|
| Spring Boot entry point | `Application` | `OrderServiceApplication` |
| Configuration | `Configuration` | `OrderServiceConfiguration` |
| Service | `Service` | `OrderService` |
| Controller | `Controller` | `OrderController` |
| Repository | `Repository` | `OrderRepository` |
| Entity | (none — use domain noun) | `Order`, `Consumer` |
| Request DTO | `Request` | `CreateOrderRequest` |
| Response DTO | `Response` | `GetOrderResponse` |
| Domain event | `Event` | `OrderCreatedEvent` |
| Exception | `Exception` | `OrderNotFoundException` |

---

## Database Naming

Each service owns its own database:

| Service | Database |
|---------|----------|
| Order | `ftgo_order_service` |
| Consumer | `ftgo_consumer_service` |
| Restaurant | `ftgo_restaurant_service` |
| Courier | `ftgo_courier_service` |

- Table names: **snake_case**, plural (e.g., `orders`, `consumers`).
- Column names: **snake_case** (e.g., `order_total`, `created_at`).
- Flyway migration prefix: `V<number>__<description>.sql` (e.g., `V1__create_orders_table.sql`).

---

## Docker Image Naming

```
ftgo/ftgo-<service-name>:<version>
```

Examples:
- `ftgo/ftgo-order-service:1.0.0`
- `ftgo/ftgo-consumer-service:latest`

---

## Configuration Files

| File | Purpose | Location |
|------|---------|----------|
| `application.yml` | Default config | `src/main/resources/` |
| `application-local.yml` | Local dev overrides | `config/` |
| `application-k8s.yml` | K8s-specific config | `src/main/resources/` (future) |
| `Dockerfile` | Container image | `docker/` |
| `deployment.yaml` | K8s manifests | `k8s/` |

---

## Creating a New Service

1. Copy the template:
   ```bash
   cp -r services/_service-template services/ftgo-<name>-service
   cp -r services/_service-template-api services/ftgo-<name>-service-api
   ```

2. Rename packages from `com.ftgo.template_` to `com.ftgo.<name>`.

3. Replace `CHANGEME` placeholders in all config files.

4. Register in `settings.gradle`:
   ```groovy
   include "services:ftgo-<name>-service"
   include "services:ftgo-<name>-service-api"
   ```

5. Create the service database:
   ```sql
   CREATE DATABASE ftgo_<name>_service;
   ```

6. Add Flyway migrations under `src/main/resources/db/migration/`.

See `services/_service-template/README.md` for detailed instructions.
