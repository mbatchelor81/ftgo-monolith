# Package and Module Naming Conventions

## Package Root

All new microservice code uses the package root:

```
com.ftgo.<service>.<layer>
```

The legacy monolith uses `net.chrisrichardson.ftgo.*`. New service code **must
not** use the legacy root. During migration, shared libraries in `libs/` may
retain the legacy package until they are refactored.

## Service Packages

Each service follows a layered package structure:

```
com.ftgo.<service>/
├── config/          # @Configuration classes, bean wiring
├── controller/      # @RestController endpoints (thin — delegates to service)
├── domain/          # JPA @Entity classes, aggregates, value objects
├── repository/      # Spring Data @Repository interfaces
├── service/         # @Service business logic
├── dto/             # Request/response DTOs (if not in the API module)
├── exception/       # Custom exceptions + @ControllerAdvice handlers
└── <ServiceName>Application.java   # @SpringBootApplication entry point
```

### Package Names by Bounded Context

| Bounded Context | Service Package | API Package |
|-----------------|-----------------|-------------|
| Consumer | `com.ftgo.consumer` | `com.ftgo.consumer.api` |
| Order | `com.ftgo.order` | `com.ftgo.order.api` |
| Restaurant | `com.ftgo.restaurant` | `com.ftgo.restaurant.api` |
| Courier | `com.ftgo.courier` | `com.ftgo.courier.api` |

### Mapping from Legacy Packages

| Legacy Package | New Package |
|----------------|-------------|
| `net.chrisrichardson.ftgo.consumerservice.*` | `com.ftgo.consumer.*` |
| `net.chrisrichardson.ftgo.orderservice.*` | `com.ftgo.order.*` |
| `net.chrisrichardson.ftgo.restaurantservice.*` | `com.ftgo.restaurant.*` |
| `net.chrisrichardson.ftgo.courierservice.*` | `com.ftgo.courier.*` |
| `net.chrisrichardson.ftgo.common.*` | `com.ftgo.common.*` |
| `net.chrisrichardson.ftgo.domain.*` | `com.ftgo.domain.*` |

## Gradle Module Naming

### Convention

```
services/<context>-service/<context>-service-app
services/<context>-service/<context>-service-api
```

In `settings.gradle`, modules are included with their full path and assigned a
project directory:

```groovy
include 'consumer-service-app'
project(':consumer-service-app').projectDir = file('services/consumer-service/consumer-service-app')
```

### Module Name Reference

| Gradle Module | Directory | Purpose |
|---------------|-----------|---------|
| `:consumer-service-app` | `services/consumer-service/consumer-service-app` | Consumer service application |
| `:consumer-service-api` | `services/consumer-service/consumer-service-api` | Consumer API contracts |
| `:order-service-app` | `services/order-service/order-service-app` | Order service application |
| `:order-service-api` | `services/order-service/order-service-api` | Order API contracts |
| `:restaurant-service-app` | `services/restaurant-service/restaurant-service-app` | Restaurant service application |
| `:restaurant-service-api` | `services/restaurant-service/restaurant-service-api` | Restaurant API contracts |
| `:courier-service-app` | `services/courier-service/courier-service-app` | Courier service application |
| `:courier-service-api` | `services/courier-service/courier-service-api` | Courier API contracts |

## Class Naming Conventions

| Element | Convention | Example |
|---------|------------|---------|
| Entity | PascalCase noun | `Order`, `Consumer`, `Restaurant` |
| Repository | `<Entity>Repository` | `OrderRepository` |
| Service | `<Entity>Service` | `OrderService` |
| Controller | `<Entity>Controller` | `OrderController` |
| DTO | `<Action><Entity>Request/Response` | `CreateOrderRequest` |
| Configuration | `<Context>Configuration` | `OrderConfiguration` |
| Exception | `<Descriptive>Exception` | `OrderNotFoundException` |
| Application | `<Context>ServiceApplication` | `OrderServiceApplication` |

## API Module Guidelines

API modules (`*-service-api`) contain **only**:

- Request/response DTOs
- Event classes
- Shared value objects needed by consumers of the service

API modules **must not** contain:

- Domain entities
- Repository interfaces
- Service implementations
- Spring configuration classes

This ensures that downstream services depend only on the lightweight API
contract, not the full service implementation.
