# Courier Service

Target home for the standalone **Courier** bounded-context microservice.

This directory currently holds the scaffold (build file + standard layout).
The live code lives in the legacy `ftgo-courier-service/` module at the
repository root and will be migrated here as part of the microservices
decomposition.

## Responsibilities

- Creating couriers and updating their availability.
- Maintaining per-courier delivery `Plan` (list of pickup/dropoff actions).
- Owning the `courier` and `courier_actions` tables / eventual `ftgo_courier`
  database.

## Structure

See [`../../templates/service-template/README.md`](../../templates/service-template/README.md)
for the canonical layout.

```
courier-service/
├── build.gradle
├── README.md
├── config/
├── docker/Dockerfile
├── k8s/
└── src/
    ├── main/java/com/ftgo/courier/
    └── test/java/com/ftgo/courier/
```

## Gradle coordinates

- Project path: `:services:courier-service`
- Root Java package: `com.ftgo.courier`
