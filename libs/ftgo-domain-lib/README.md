# ftgo-domain-lib

Standalone, versioned shared library extracted from the `ftgo-domain` monolith module. Contains all shared JPA entities, repositories, and domain configuration for the FTGO platform. This is a **transitional library** — as each microservice takes full ownership of its bounded context, it will replace its dependency on this library with service-local entities.

## Version

Current version: **1.0.0** (managed in `gradle.properties` as `ftgoDomainLibVersion`)

## Contents

### JPA Entities

| Entity | Table | Owning Service | Description |
|--------|-------|----------------|-------------|
| `Order` | `orders` | Order Service | Core order entity with full lifecycle state machine. |
| `Consumer` | `consumers` | Consumer Service | Customer who places orders. |
| `Restaurant` | `restaurants` | Restaurant Service | Restaurant with menu and address. |
| `Courier` | `courier` | Courier Service | Delivery courier with availability and plan. |

### Embeddable Value Objects

| Class | Description |
|-------|-------------|
| `OrderLineItem` | Line item within an order (quantity, menu item, price). |
| `OrderLineItems` | Collection wrapper for `OrderLineItem`. |
| `MenuItem` | Menu item with id, name, and price. |
| `RestaurantMenu` | Collection wrapper for `MenuItem`. |
| `DeliveryInformation` | Delivery time and address. |
| `PaymentInformation` | Payment token. |
| `Plan` | Courier delivery plan (list of actions). |
| `Action` | Scheduled pickup/dropoff event. |

### Enumerations

| Enum | Values |
|------|--------|
| `OrderState` | `APPROVED`, `ACCEPTED`, `PREPARING`, `READY_FOR_PICKUP`, `PICKED_UP`, `DELIVERED`, `CANCELLED` |
| `ActionType` | `PICKUP`, `DROPOFF` |

### Domain Classes

| Class | Description |
|-------|-------------|
| `OrderRevision` | Represents a revision to an existing order. |
| `LineItemQuantityChange` | Captures the delta from a line item quantity change. |
| `OrderMinimumNotMetException` | Thrown when a revised order total falls below the minimum. |

### Spring Data Repositories

| Repository | Entity | Description |
|------------|--------|-------------|
| `OrderRepository` | `Order` | CRUD + `findAllByConsumerId`. |
| `ConsumerRepository` | `Consumer` | CRUD operations. |
| `RestaurantRepository` | `Restaurant` | CRUD operations. |
| `CourierRepository` | `Courier` | CRUD + `findAllAvailable`. |

### Configuration

| Class | Description |
|-------|-------------|
| `DomainConfiguration` | `@Configuration` enabling JPA auto-config, entity scanning, and repository scanning. |

## Usage

### Gradle Dependency

```groovy
// In a microservice build.gradle
dependencies {
    implementation project(':ftgo-domain-lib')
}
```

Once published to a Maven repository:

```groovy
dependencies {
    implementation 'net.chrisrichardson.ftgo:ftgo-domain-lib:1.0.0'
}
```

### Publishing

Publish to the local project repository:

```bash
./gradlew :ftgo-domain-lib:publish
```

## Dependencies

- `ftgo-common-lib` (value objects: `Money`, `Address`, `PersonName`)
- `ftgo-common-jpa-lib` (JPA ORM mappings for value objects)
- `spring-boot-starter-data-jpa` (JPA/Hibernate runtime)
- `commons-lang` 2.6 (`EqualsBuilder`, `HashCodeBuilder`, `ToStringBuilder`)

## Relationship to `ftgo-domain`

This library is a versioned extraction of the legacy `ftgo-domain` module. The original module remains in place for backward compatibility with existing monolith modules. New microservices under `services/` should depend on `ftgo-domain-lib` instead.

## Migration Notes

As microservices mature, each service should:
1. Copy the entities it owns from this library into its own module
2. Remove the dependency on `ftgo-domain-lib`
3. Replace cross-service entity references with DTOs from the corresponding service API module

See `docs/entity-ownership-mapping.md` for the planned entity-to-service ownership mapping.
