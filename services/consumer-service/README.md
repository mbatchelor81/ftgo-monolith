# Consumer Service

Manages consumer registration, verification, and profile data.

## Bounded Context

The Consumer bounded context owns:

- Consumer registration and profile management
- Consumer verification for order placement
- Consumer payment information

## Package Structure

```
com.ftgo.consumer/
├── config/          # ConsumerConfiguration
├── controller/      # ConsumerController
├── domain/          # Consumer entity, ConsumerService
├── repository/      # ConsumerRepository
├── service/         # Business logic
└── ConsumerServiceApplication.java
```

## API Module

`consumer-service-api` provides:

- `CreateConsumerRequest` / `CreateConsumerResponse`
- Consumer event DTOs

## Migration Status

- [ ] Extract domain entities from `ftgo-domain`
- [ ] Migrate service logic from `ftgo-consumer-service`
- [ ] Migrate API contracts from `ftgo-consumer-service-api`
- [ ] Set up independent database schema
- [ ] Configure independent Spring Boot application
- [ ] Add Dockerfile and Kubernetes manifests
