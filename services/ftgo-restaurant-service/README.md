# FTGO Restaurant Service

Manages restaurant registration and menu management for the FTGO platform.

## Bounded Context: Restaurant

**Responsibilities:**
- Restaurant registration and profile management
- Menu item management (create, update)
- Menu item lookup for order validation

**Package root:** `com.ftgo.restaurantservice`

## Monolith Origin

Extracted from the monolith modules:
- `ftgo-restaurant-service` — business logic and REST controllers
- `ftgo-restaurant-service-api` — DTOs, events, and API contracts
- Entities from `ftgo-domain`: `Restaurant`, `RestaurantMenu`, `MenuItem`

## Running Locally

```bash
./gradlew :services:ftgo-restaurant-service:bootRun
```

## API Endpoints

| Method | Path               | Description              |
|--------|--------------------|--------------------------|
| POST   | /restaurants       | Register a restaurant    |
| GET    | /restaurants/{id}  | Get restaurant by ID     |
