# Consumer Service

Target home for the standalone **Consumer** bounded-context microservice.

This directory currently holds the scaffold (build file + standard layout). The
live code lives in the legacy `ftgo-consumer-service/` module at the repository
root and will be migrated here as part of the microservices decomposition.

## Responsibilities

- Creating and retrieving consumer records (`Consumer` aggregate).
- Validating whether a given order total is permissible for a consumer
  (`validateOrderForConsumer`).
- Owning the `consumers` table / eventual `ftgo_consumer` database.

## Structure

See [`../../templates/service-template/README.md`](../../templates/service-template/README.md)
for the canonical layout. This service follows it:

```
consumer-service/
├── build.gradle
├── README.md
├── config/           # application.yml and profile overrides
├── docker/Dockerfile
├── k8s/              # deployment.yaml, service.yaml
└── src/
    ├── main/java/com/ftgo/consumer/
    └── test/java/com/ftgo/consumer/
```

## Gradle coordinates

- Project path: `:services:consumer-service`
- Root Java package: `com.ftgo.consumer`
