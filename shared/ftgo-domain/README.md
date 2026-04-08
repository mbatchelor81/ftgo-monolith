# ftgo-domain — Shared Domain Library

Shared JPA entities and repository interfaces extracted from the FTGO monolith's `ftgo-domain` module.

## Contents

### Entities
| Entity | Table | Description |
|--------|-------|-------------|
| `Order` | `orders` | Central order entity with state machine (APPROVED → DELIVERED) |
| `Consumer` | `consumers` | Consumer with embedded `PersonName` |
| `Restaurant` | `restaurants` | Restaurant with `@ElementCollection` of `MenuItem` |
| `Courier` | `courier` | Courier with embedded `Plan` (list of `Action`s) |

### Repository Interfaces
- `OrderRepository` — `CrudRepository<Order, Long>` + `findAllByConsumerId`
- `ConsumerRepository` — `CrudRepository<Consumer, Long>`
- `RestaurantRepository` — `CrudRepository<Restaurant, Long>`
- `CourierRepository` — `CrudRepository<Courier, Long>` + `findAllAvailable()`

### Value Objects / Embeddables
`OrderLineItem`, `OrderLineItems`, `MenuItem`, `RestaurantMenu`, `Plan`, `Action`, `ActionType`, `DeliveryInformation`, `PaymentInformation`, `OrderRevision`, `LineItemQuantityChange`

### Enums
`OrderState` — `APPROVED`, `ACCEPTED`, `PREPARING`, `READY_FOR_PICKUP`, `PICKED_UP`, `DELIVERED`, `CANCELLED`

## Usage

Add as a project dependency in your `build.gradle`:

```groovy
dependencies {
    implementation project(':shared:ftgo-domain')
}
```

Or, once published to a Maven repository:

```groovy
dependencies {
    implementation 'net.chrisrichardson.ftgo:ftgo-domain:1.0.0'
}
```

This transitively brings in `ftgo-common-jpa` and `ftgo-common`.

## Migration Path

During the monolith-to-microservices migration, this shared library provides backward compatibility. Each service should:

1. Initially depend on `ftgo-domain` for all shared entities
2. Gradually extract service-specific entities into the service's own domain package
3. Remove the `ftgo-domain` dependency once migration is complete

## Publishing

```bash
./gradlew :shared:ftgo-domain:publish
```

Artifacts are published to the local Maven repository at `build/repo`.

## Jakarta EE Migration

All JPA annotations have been migrated from `javax.persistence` to `jakarta.persistence` for Spring Boot 3.x compatibility. Similarly, Apache Commons Lang imports use `org.apache.commons.lang3`.
