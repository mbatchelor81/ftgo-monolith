# Restaurant Service

Manages restaurant registration, menus, and order acceptance/preparation.

## Bounded Context

The Restaurant bounded context owns:

- Restaurant registration and profile management
- Menu management (items, pricing)
- Order acceptance and preparation workflow

## Package Structure

```
com.ftgo.restaurant/
├── config/          # RestaurantServiceDomainConfiguration
├── controller/      # RestaurantController
├── domain/          # Restaurant, RestaurantMenu, MenuItem
├── repository/      # RestaurantRepository
├── service/         # RestaurantService
└── RestaurantServiceApplication.java
```

## API Module

`restaurant-service-api` provides:

- `CreateRestaurantRequest`
- `RestaurantMenuDTO`, `MenuItemDTO`

## Migration Status

- [ ] Extract domain entities from `ftgo-domain`
- [ ] Migrate service logic from `ftgo-restaurant-service`
- [ ] Migrate API contracts from `ftgo-restaurant-service-api`
- [ ] Set up independent database schema
- [ ] Configure independent Spring Boot application
- [ ] Add Dockerfile and Kubernetes manifests
