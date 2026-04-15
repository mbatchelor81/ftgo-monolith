# FTGO Courier Service

Manages courier registration, availability tracking, and delivery plan
management for the FTGO platform.

## Bounded Context: Courier

**Responsibilities:**
- Courier registration and profile management
- Availability status tracking
- Delivery plan management (pickup/dropoff scheduling)

**Package root:** `com.ftgo.courierservice`

## Monolith Origin

Extracted from the monolith modules:
- `ftgo-courier-service` — business logic and REST controllers
- `ftgo-courier-service-api` — DTOs and API contracts
- Entities from `ftgo-domain`: `Courier`, `Plan`, `Action`

## Running Locally

```bash
./gradlew :services:ftgo-courier-service:bootRun
```

## API Endpoints

| Method | Path                             | Description                |
|--------|----------------------------------|----------------------------|
| POST   | /couriers                        | Register a courier         |
| GET    | /couriers/{id}                   | Get courier by ID          |
| POST   | /couriers/{id}/availability      | Update courier availability|
