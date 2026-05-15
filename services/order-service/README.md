# Order Service

Manages the order lifecycle: creation, approval, revision, cancellation,
and delivery scheduling.

## Bounded Context

The Order bounded context owns:

- Order creation and validation
- Order state machine (APPROVAL_PENDING → APPROVED → … → DELIVERED)
- Order revision and cancellation
- Ticket management for restaurant preparation
- Courier scheduling for pickup and delivery

## Package Structure

```
com.ftgo.order/
├── config/          # OrderConfiguration, OrderServiceWithRepositoriesConfiguration
├── controller/      # OrderController, TicketController
├── domain/          # Order, OrderLineItems, RevisedOrder
├── repository/      # OrderRepository
├── service/         # OrderService
└── OrderServiceApplication.java
```

## API Module

`order-service-api` provides:

- `CreateOrderRequest` / `CreateOrderResponse`
- `ReviseOrderRequest`
- `OrderAcceptance`
- `OrderDetails`, `OrderLineItemDTO`

## Migration Status

- [ ] Extract domain entities from `ftgo-domain`
- [ ] Migrate service logic from `ftgo-order-service`
- [ ] Migrate API contracts from `ftgo-order-service-api`
- [ ] Set up independent database schema
- [ ] Configure independent Spring Boot application
- [ ] Add Dockerfile and Kubernetes manifests
