# FTGO Order Service

Manages the full order lifecycle including creation, revision, acceptance,
preparation tracking, and delivery status updates.

## Bounded Context: Order

**Responsibilities:**
- Order creation, revision, and cancellation
- Order state machine management (APPROVED -> ACCEPTED -> PREPARING -> READY_FOR_PICKUP -> PICKED_UP -> DELIVERED)
- Coordination with Consumer, Restaurant, and Courier services

**Package root:** `com.ftgo.orderservice`

## Monolith Origin

Extracted from the monolith modules:
- `ftgo-order-service` — business logic and REST controllers
- `ftgo-order-service-api` — DTOs and API contracts
- Entities from `ftgo-domain`: `Order`, `OrderLineItems`, `OrderLineItem`, `OrderRevision`

## Running Locally

```bash
./gradlew :services:ftgo-order-service:bootRun
```

## API Endpoints

| Method | Path                          | Description                |
|--------|-------------------------------|----------------------------|
| POST   | /orders                       | Create a new order         |
| GET    | /orders/{id}                  | Get order by ID            |
| POST   | /orders/{id}/cancel           | Cancel an order            |
| POST   | /orders/{id}/revise           | Revise order line items    |
| POST   | /orders/{id}/accept           | Accept an order (restaurant) |
| POST   | /orders/{id}/preparing        | Mark order as preparing    |
| POST   | /orders/{id}/ready            | Mark order ready for pickup|
| POST   | /orders/{id}/pickedup         | Mark order as picked up    |
| POST   | /orders/{id}/delivered        | Mark order as delivered    |
| GET    | /orders?consumerId={id}       | Get orders by consumer     |
