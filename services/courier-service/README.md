# Courier Service

Manages courier registration, availability, and delivery plan scheduling.

## Bounded Context

The Courier bounded context owns:

- Courier registration and profile management
- Courier availability tracking
- Delivery plan management (pickup and dropoff actions)

## Package Structure

```
com.ftgo.courier/
├── config/          # CourierServiceConfiguration
├── controller/      # CourierController
├── domain/          # Courier, Plan, Action
├── repository/      # CourierRepository
├── service/         # CourierService
└── CourierServiceApplication.java
```

## API Module

`courier-service-api` provides:

- `CreateCourierRequest` / `CreateCourierResponse`
- `CourierAvailability`

## Migration Status

- [ ] Extract domain entities from `ftgo-domain`
- [ ] Migrate service logic from `ftgo-courier-service`
- [ ] Migrate API contracts from `ftgo-courier-service-api`
- [ ] Set up independent database schema
- [ ] Configure independent Spring Boot application
- [ ] Add Dockerfile and Kubernetes manifests
