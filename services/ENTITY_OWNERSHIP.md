# Entity-to-Service Ownership Mapping

This document maps each JPA entity in `ftgo-domain` to the microservice that owns it.
During the migration, entities will be moved from the shared `ftgo-domain` library into
service-specific domain modules.

## Entity Ownership

| Entity | Table | Owning Service | Notes |
|---|---|---|---|
| `Order` | `orders` | `ftgo-order-service` | Core entity with state machine; most complex entity |
| `OrderLineItem` | `order_line_items` | `ftgo-order-service` | Embeddable, part of Order aggregate |
| `OrderLineItems` | (embedded) | `ftgo-order-service` | Collection wrapper for OrderLineItem |
| `Consumer` | `consumers` | `ftgo-consumer-service` | Embeds PersonName |
| `Restaurant` | `restaurants` | `ftgo-restaurant-service` | Has ElementCollection of MenuItem |
| `RestaurantMenu` | (embedded) | `ftgo-restaurant-service` | Wraps List of MenuItem |
| `MenuItem` | `restaurant_menu_items` | `ftgo-restaurant-service` | Embeddable, part of Restaurant aggregate |
| `Courier` | `courier` | `ftgo-courier-service` | Embeds Plan; DynamicUpdate |
| `Plan` | (embedded) | `ftgo-courier-service` | List of Action objects for courier scheduling |
| `Action` | `courier_actions` | `ftgo-courier-service` | Pickup/dropoff events |

## Value Objects (Shared)

| Value Object | Used By | Location After Migration |
|---|---|---|
| `Money` | Order, OrderLineItem, MenuItem | `ftgo-common` (already extracted) |
| `Address` | Restaurant, Courier, DeliveryInformation | `ftgo-common` (already extracted) |
| `PersonName` | Consumer | `ftgo-common` (already extracted) |
| `DeliveryInformation` | Order | `ftgo-domain` → `ftgo-order-service` |
| `PaymentInformation` | Order | `ftgo-domain` → `ftgo-order-service` |
| `OrderRevision` | Order | `ftgo-domain` → `ftgo-order-service` |
| `LineItemQuantityChange` | OrderLineItems | `ftgo-domain` → `ftgo-order-service` |

## Enums

| Enum | Used By | Location After Migration |
|---|---|---|
| `OrderState` | Order | `ftgo-order-service-api` (shared contract) |
| `ActionType` | Action | `ftgo-courier-service` |

## Repositories

| Repository | Entity | Owning Service |
|---|---|---|
| `OrderRepository` | Order | `ftgo-order-service` |
| `ConsumerRepository` | Consumer | `ftgo-consumer-service` |
| `RestaurantRepository` | Restaurant | `ftgo-restaurant-service` |
| `CourierRepository` | Courier | `ftgo-courier-service` |

## Cross-Service Dependencies

The following cross-service entity references exist in the monolith and must be
resolved during migration via API calls or events:

| Source Service | Target Entity | Resolution Strategy |
|---|---|---|
| `ftgo-order-service` | `Restaurant` | REST API call to `ftgo-restaurant-service` or local cache |
| `ftgo-order-service` | `Consumer` | REST API call via `ftgo-consumer-service-api` |
| `ftgo-order-service` | `Courier` | REST API call via `ftgo-courier-service-api` |
| `ftgo-courier-service` | `Order` | Event-driven via `ftgo-order-service-api` events |

## Migration Plan

### Phase 1: Shared Libraries (Current — EM-31)
- [x] Extract `ftgo-common` as versioned library (EM-32)
- [x] Extract `ftgo-common-jpa` as versioned library
- [x] Extract `ftgo-domain` as versioned shared library
- [x] Define API/DTO contracts for each service

### Phase 2: Service-Owned Domains (Future)
1. Move `Order`, `OrderLineItem`, `OrderLineItems`, `DeliveryInformation`,
   `PaymentInformation`, `OrderRevision`, `LineItemQuantityChange` into
   `ftgo-order-service` domain package
2. Move `Consumer` into `ftgo-consumer-service` domain package
3. Move `Restaurant`, `RestaurantMenu`, `MenuItem` into
   `ftgo-restaurant-service` domain package
4. Move `Courier`, `Plan`, `Action`, `ActionType` into
   `ftgo-courier-service` domain package

### Phase 3: Replace Direct References
1. Replace `@ManyToOne Restaurant` in `Order` with `restaurantId` (Long) +
   REST API lookup
2. Replace `@ManyToOne Courier` in `Order` with `courierId` (Long) +
   event-driven assignment
3. Replace `Order` references in `Action` with `orderId` (Long)
4. Introduce anti-corruption layers at service boundaries

### Phase 4: Deprecate Shared Domain
1. Once all entities are owned by their respective services, deprecate
   `ftgo-domain` shared library
2. Each service depends only on its own domain + API contract modules
3. Remove `ftgo-domain` from the dependency graph
