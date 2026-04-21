# Restaurant Service

Target home for the standalone **Restaurant** bounded-context microservice.

This directory currently holds the scaffold (build file + standard layout).
The live code lives in the legacy `ftgo-restaurant-service/` module at the
repository root and will be migrated here as part of the microservices
decomposition.

## Responsibilities

- Creating and retrieving restaurants and their menus.
- Owning the `restaurants` table and the `restaurant_menu_items`
  `@ElementCollection` / eventual `ftgo_restaurant` database.
- Publishing `RestaurantMenu` updates that downstream services (e.g. Order)
  consume via the service's `*-api` contract module.

## Structure

See [`../../templates/service-template/README.md`](../../templates/service-template/README.md)
for the canonical layout.

```
restaurant-service/
├── build.gradle
├── README.md
├── config/
├── docker/Dockerfile
├── k8s/
└── src/
    ├── main/java/com/ftgo/restaurant/
    └── test/java/com/ftgo/restaurant/
```

## Gradle coordinates

- Project path: `:services:restaurant-service`
- Root Java package: `com.ftgo.restaurant`
