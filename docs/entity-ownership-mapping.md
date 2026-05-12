# Entity-to-Service Ownership Mapping

This document maps each JPA entity in `ftgo-domain-lib` to its owning microservice, the database table it maps to, and the cross-service API contract (DTO) that other services use when they need to reference that entity's data.

## Entity Ownership

| Entity | DB Table | Owning Service | API Module | Notes |
|--------|----------|----------------|------------|-------|
| `Consumer` | `consumers` | **Consumer Service** | `consumer-service-api` | Customer who places orders. |
| `Order` | `orders` | **Order Service** | `order-service-api` | Full lifecycle state machine (APPROVED → DELIVERED). |
| `OrderLineItem` | `order_line_items` | **Order Service** | `order-service-api` | Embedded collection within `Order`. |
| `Restaurant` | `restaurants` | **Restaurant Service** | `restaurant-service-api` | Restaurant with menu and address. |
| `MenuItem` | `restaurant_menu_items` | **Restaurant Service** | `restaurant-service-api` | Embedded collection within `Restaurant`. |
| `Courier` | `courier` | **Courier Service** | `courier-service-api` | Delivery courier with availability and plan. |
| `Action` | `courier_actions` | **Courier Service** | `courier-service-api` | Embedded collection within `Courier.Plan`. |

## Cross-Service DTO Contracts

When a service needs data from another bounded context, it uses the DTO from that service's API module rather than importing the JPA entity directly.

### Consumer Service API (`consumer-service-api`)

| DTO | Description | Used By |
|-----|-------------|---------|
| `ConsumerDTO` | Consumer id and name. | Order Service (to display consumer on order). |
| `CreateConsumerRequest` | Request to create a new consumer. | API gateway / clients. |
| `CreateConsumerResponse` | Returns the new consumer's id. | API gateway / clients. |

### Order Service API (`order-service-api`)

| DTO | Description | Used By |
|-----|-------------|---------|
| `OrderDTO` | Order summary with state, consumer, restaurant, line items, total. | Courier Service (for delivery scheduling), Restaurant Service (for order display). |
| `OrderLineItemDTO` | Line item with menu item id, name, price, quantity. | Embedded within `OrderDTO`. |
| `CreateOrderRequest` | Request to place a new order. | API gateway / clients. |
| `CreateOrderResponse` | Returns the new order's id. | API gateway / clients. |
| `OrderAcceptance` | Restaurant acceptance with readyBy time. | Restaurant Service (to accept an order). |
| `ReviseOrderRequest` | Request to revise line item quantities. | API gateway / clients. |

### Restaurant Service API (`restaurant-service-api`)

| DTO | Description | Used By |
|-----|-------------|---------|
| `RestaurantDTO` | Restaurant with name, address, and menu items. | Order Service (to validate menu items on order creation). |
| `MenuItemDTO` | Menu item with id, name, and price. | Embedded within `RestaurantDTO` and `CreateRestaurantRequest`. |
| `CreateRestaurantRequest` | Request to create a new restaurant. | API gateway / clients. |

### Courier Service API (`courier-service-api`)

| DTO | Description | Used By |
|-----|-------------|---------|
| `CourierDTO` | Courier with name, address, availability. | Order Service (for delivery assignment). |
| `CreateCourierRequest` | Request to register a new courier. | API gateway / clients. |
| `CreateCourierResponse` | Returns the new courier's id. | API gateway / clients. |
| `CourierAvailability` | Toggle courier availability. | API gateway / clients. |

## Cross-Service Dependencies

The following diagram shows which API modules each service depends on for cross-service communication:

```
┌──────────────────────────────────────────────────────────┐
│                   Order Service                          │
│                                                          │
│  Depends on:                                             │
│    - consumer-service-api  (validate consumer exists)    │
│    - restaurant-service-api (validate menu items/prices) │
│    - courier-service-api   (assign courier for delivery) │
└──────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────┐
│                 Consumer Service                         │
│                                                          │
│  Depends on: (none — root aggregate)                     │
└──────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────┐
│                Restaurant Service                        │
│                                                          │
│  Depends on: (none — root aggregate)                     │
└──────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────┐
│                  Courier Service                         │
│                                                          │
│  Depends on:                                             │
│    - order-service-api (order data for delivery plans)   │
└──────────────────────────────────────────────────────────┘
```

## Migration Plan

### Phase 1: Extract Shared Libraries (Current — EM-31)
- Extract `ftgo-common-jpa` → `libs/ftgo-common-jpa-lib/` (versioned, publishable)
- Extract `ftgo-domain` → `libs/ftgo-domain-lib/` (versioned, publishable)
- Define cross-service DTO/API contracts in each service's API module
- All services temporarily depend on `ftgo-domain-lib` for shared entities

### Phase 2: Service-Owned Entities
Each microservice copies the entities it owns from `ftgo-domain-lib` into its own module:

| Service | Entities to Internalize |
|---------|------------------------|
| Consumer Service | `Consumer`, `ConsumerRepository` |
| Order Service | `Order`, `OrderLineItem`, `OrderLineItems`, `OrderRevision`, `LineItemQuantityChange`, `OrderState`, `OrderMinimumNotMetException`, `DeliveryInformation`, `PaymentInformation`, `OrderRepository` |
| Restaurant Service | `Restaurant`, `MenuItem`, `RestaurantMenu`, `RestaurantRepository` |
| Courier Service | `Courier`, `Plan`, `Action`, `ActionType`, `CourierRepository` |

### Phase 3: Remove Cross-Entity References
- Replace `Order.restaurant` (JPA `@ManyToOne`) with `Order.restaurantId` (long)
- Replace `Order.assignedCourier` (JPA `@ManyToOne`) with `Order.courierId` (long)
- Replace `Action.order` (JPA `@ManyToOne`) with `Action.orderId` (long)
- Services communicate via API DTOs and REST/messaging, not shared JPA relationships

### Phase 4: Database Decomposition
- Each service gets its own database schema (or separate database)
- Flyway migrations split per service
- `ftgo-domain-lib` dependency removed from all services

## Embeddable / Value Object Ownership

| Class | Description | Shared? | Location |
|-------|-------------|---------|----------|
| `Money` | Currency amount | Yes — all services | `ftgo-common-lib` |
| `Address` | Postal address | Yes — Consumer, Restaurant, Courier | `ftgo-common-lib` |
| `PersonName` | First/last name | Yes — Consumer, Courier | `ftgo-common-lib` |
| `OrderLineItems` | Collection wrapper | No — Order Service only | `ftgo-domain-lib` → Order Service |
| `RestaurantMenu` | Collection wrapper | No — Restaurant Service only | `ftgo-domain-lib` → Restaurant Service |
| `Plan` | Delivery plan | No — Courier Service only | `ftgo-domain-lib` → Courier Service |
| `DeliveryInformation` | Delivery details | No — Order Service only | `ftgo-domain-lib` → Order Service |
| `PaymentInformation` | Payment token | No — Order Service only | `ftgo-domain-lib` → Order Service |
