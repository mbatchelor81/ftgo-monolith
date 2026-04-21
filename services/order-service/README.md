# Order Service

Target home for the standalone **Order** bounded-context microservice.

This directory currently holds the scaffold (build file + standard layout).
The live code lives in the legacy `ftgo-order-service/` module at the
repository root and will be migrated here as part of the microservices
decomposition.

## Responsibilities

- Owning the order lifecycle state machine (`APPROVED → ACCEPTED → PREPARING
  → READY_FOR_PICKUP → PICKED_UP → DELIVERED`, with `CANCELLED` branches).
- Exposing REST endpoints for creating, revising, cancelling, and advancing
  orders.
- Coordinating with the Consumer, Restaurant, and Courier services (via their
  public APIs once the split is complete — never via shared JPA entities).
- Owning the `orders` table / eventual `ftgo_order` database.

## Structure

See [`../../templates/service-template/README.md`](../../templates/service-template/README.md)
for the canonical layout.

```
order-service/
├── build.gradle
├── README.md
├── config/
├── docker/Dockerfile
├── k8s/
└── src/
    ├── main/java/com/ftgo/order/
    └── test/java/com/ftgo/order/
```

## Gradle coordinates

- Project path: `:services:order-service`
- Root Java package: `com.ftgo.order`
