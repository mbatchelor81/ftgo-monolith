# FTGO Consumer Service

Manages consumer registration and validation. Consumers are the users who place
food delivery orders through the FTGO platform.

## Bounded Context: Consumer

**Responsibilities:**
- Consumer registration and profile management
- Order validation (e.g., verifying consumer eligibility, payment limits)

**Package root:** `com.ftgo.consumerservice`

## Monolith Origin

Extracted from the monolith modules:
- `ftgo-consumer-service` — business logic and REST controllers
- `ftgo-consumer-service-api` — DTOs and API contracts
- Entities from `ftgo-domain`: `Consumer`

## Running Locally

```bash
./gradlew :services:ftgo-consumer-service:bootRun
```

## API Endpoints

| Method | Path              | Description           |
|--------|-------------------|-----------------------|
| POST   | /consumers        | Register a consumer   |
| GET    | /consumers/{id}   | Get consumer by ID    |
