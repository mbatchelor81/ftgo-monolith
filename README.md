# FTGO — Food To Go

FTGO is a food delivery platform originally implemented as a Spring Boot
monolith. This repository contains both the legacy monolith and the target
microservices structure for an incremental migration.

For the original monolith documentation, see [README.adoc](README.adoc).

## Repository Layout

```
ftgo-monolith/
│
├── ftgo-application/                 # Monolith entry point (composes all services)
├── ftgo-order-service/               # Legacy: Order business logic
├── ftgo-consumer-service/            # Legacy: Consumer business logic
├── ftgo-restaurant-service/          # Legacy: Restaurant business logic
├── ftgo-courier-service/             # Legacy: Courier business logic
├── ftgo-*-api/                       # Legacy: Service API contracts/DTOs
├── ftgo-domain/                      # Legacy: Shared JPA entities
├── ftgo-common/                      # Legacy: Shared value objects
├── ftgo-common-jpa/                  # Legacy: Shared JPA config
├── ftgo-flyway/                      # Legacy: Database migrations
├── ftgo-end-to-end-tests/            # Legacy: E2E tests
│
├── services/                         # NEW: Microservices target structure
│   ├── ftgo-consumer-service/        #   Consumer bounded context
│   ├── ftgo-order-service/           #   Order bounded context
│   ├── ftgo-restaurant-service/      #   Restaurant bounded context
│   ├── ftgo-courier-service/         #   Courier bounded context
│   ├── ftgo-common/                  #   Shared library
│   ├── ftgo-common-jpa/              #   Shared JPA library
│   └── ftgo-service-template/        #   Template for new services
│
├── docs/
│   └── adr/
│       └── 0001-microservices-repository-structure.md
│
├── build.gradle                      # Root Gradle config
├── settings.gradle                   # Module registration
└── deployment/kubernetes/            # Kubernetes infrastructure
```

## Bounded Contexts

| Context    | Service Directory                    | Package Root                    |
|------------|--------------------------------------|---------------------------------|
| Consumer   | `services/ftgo-consumer-service/`    | `com.ftgo.consumerservice`      |
| Order      | `services/ftgo-order-service/`       | `com.ftgo.orderservice`         |
| Restaurant | `services/ftgo-restaurant-service/`  | `com.ftgo.restaurantservice`    |
| Courier    | `services/ftgo-courier-service/`     | `com.ftgo.courierservice`       |

## Building

```bash
# Build and test the legacy monolith
./gradlew clean build test
```

## Architecture Decisions

- [ADR-0001: Microservices Repository Structure and Naming Conventions](docs/adr/0001-microservices-repository-structure.md)

## Creating a New Service

See the [service template README](services/ftgo-service-template/README.md).

## Learn More

See the [original README](README.adoc) and the
[Microservices Patterns book](https://microservices.io/book) by Chris Richardson.
