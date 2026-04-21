# ftgo-domain

Shared JPA entities, Spring Data repositories, and supporting value
objects for the FTGO platform. Extracted from the legacy root-level
`ftgo-domain/` module as part of **EM-31**.

## Coordinates

```
group:    net.chrisrichardson.ftgo
artifact: ftgo-domain
version:  1.0.0
```

Package namespace preserved from the monolith: `net.chrisrichardson.ftgo.domain`.

## Contents

- **Entities**: `Order`, `Consumer`, `Restaurant`, `Courier`.
- **Repositories**: `OrderRepository`, `ConsumerRepository`,
  `RestaurantRepository`, `CourierRepository`.
- **Embeddables / value objects**: `OrderLineItem`, `OrderLineItems`,
  `MenuItem`, `RestaurantMenu`, `Plan`, `Action`, `DeliveryInformation`,
  `PaymentInformation`, `OrderRevision`, `LineItemQuantityChange`.
- **Enums**: `OrderState`, `ActionType`.
- **Configuration**: `DomainConfiguration` — `@EnableJpaRepositories`,
  `@EntityScan`, `@EnableAutoConfiguration`, imports `CommonConfiguration`.
- **Exceptions**: `OrderMinimumNotMetException`.

See [`ENTITY_OWNERSHIP.md`](./ENTITY_OWNERSHIP.md) for the full
entity-to-service ownership map that drives the per-service database
split.

## Consuming

```gradle
dependencies {
    implementation "net.chrisrichardson.ftgo:ftgo-domain:1.0.0"
}
```

Transitively pulls in `ftgo-common-jpa` (ships `META-INF/orm.xml`) and
`ftgo-common` (value objects).

## Build & publish

```bash
./gradlew :libs:ftgo-domain:build
./gradlew :libs:ftgo-domain:publish   # → build/repo by default
```

## Migration note

This library is published in parallel with the legacy `ftgo-domain/`
module so the monolith keeps building while services migrate. New
services should depend on `ftgo-domain:1.0.0`; the legacy module will
be removed once all services have switched over.
