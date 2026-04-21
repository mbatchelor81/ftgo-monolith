# FTGO Domain — Entity-to-Service Ownership

This document captures the target microservice owner for every JPA entity,
repository, and supporting value object currently bundled in
`libs/ftgo-domain`. The library ships them together as a migration aid so
the existing monolith keeps compiling while individual services are
extracted; the long-term plan is for each entity to live with exactly one
service and for cross-service references to be replaced by API calls or
locally-cached read models.

## Ownership map

| Type                              | Kind                  | Owner service       | Notes                                                              |
|-----------------------------------|-----------------------|---------------------|--------------------------------------------------------------------|
| `Order`                           | `@Entity`             | Order Service       | Owns the order state machine; table: `orders`.                     |
| `OrderRepository`                 | Repository            | Order Service       | `CrudRepository<Order, Long>` with `findAllByConsumerId`.          |
| `OrderLineItem`                   | `@Embeddable`         | Order Service       | Line item inside an `Order`.                                       |
| `OrderLineItems`                  | `@Embeddable`         | Order Service       | Collection wrapper computing totals for an `Order`.                |
| `OrderState`                      | Enum                  | Order Service       | Valid states in the order lifecycle.                               |
| `OrderRevision`                   | Value object          | Order Service       | Input for `Order.revise()`.                                        |
| `LineItemQuantityChange`          | Value object          | Order Service       | Output of a revision calculation.                                  |
| `OrderMinimumNotMetException`     | Exception             | Order Service       | Thrown when a revised order falls below the restaurant minimum.    |
| `DeliveryInformation`             | `@Embeddable`         | Order Service       | Delivery address/time embedded in `Order`.                         |
| `PaymentInformation`              | `@Embeddable`         | Order Service       | Payment token embedded in `Order`.                                 |
| `Consumer`                        | `@Entity`             | Consumer Service    | Embeds `PersonName`; table: `consumers`.                           |
| `ConsumerRepository`              | Repository            | Consumer Service    | `CrudRepository<Consumer, Long>`.                                  |
| `Restaurant`                      | `@Entity`             | Restaurant Service  | Owns `@ElementCollection` of `MenuItem`; table: `restaurants`.     |
| `RestaurantRepository`            | Repository            | Restaurant Service  | `CrudRepository<Restaurant, Long>`.                                |
| `RestaurantMenu`                  | `@Embeddable` helper  | Restaurant Service  | Transient wrapper used during `Restaurant` construction.           |
| `MenuItem`                        | `@Embeddable`         | Restaurant Service  | Stored in `restaurant_menu_items` collection table.                |
| `Courier`                         | `@Entity`             | Courier Service     | Embeds `Plan`; table: `courier`.                                   |
| `CourierRepository`               | Repository            | Courier Service     | Adds `findAllAvailable()` JPQL query.                              |
| `Plan`                            | `@Embeddable`         | Courier Service     | Ordered list of `Action`s attached to a `Courier`.                 |
| `Action`                          | `@Embeddable`         | Courier Service     | Pickup / dropoff event inside a `Plan`.                            |
| `ActionType`                      | Enum                  | Courier Service     | `PICKUP` / `DROPOFF`.                                              |
| `DomainConfiguration`             | `@Configuration`      | Shared              | Enables JPA repos + entity scan for the shared domain package.     |

## Cross-service references to resolve during decomposition

The monolith persists a handful of cross-aggregate references that each
service split will need to break. These are documented here so they don't
get lost when the entities move to their owning services.

- **`Order.restaurant` (`@ManyToOne Restaurant`)** — Order Service will
  drop the FK in favor of an immutable `restaurantId` plus a locally
  cached, eventually-consistent restaurant read model.
- **`Order.assignedCourier` (`@ManyToOne Courier`)** — same story for
  courier assignments: Order Service stores `assignedCourierId`, and the
  Courier Service publishes availability updates.
- **`Action` (embedded in `Courier.plan`) references `Order.id`** —
  Courier Service keeps the `orderId` scalar and resolves order details
  via the Order Service API.
- **`Order.consumerId`** — already a plain scalar; no schema change
  required when the Consumer table moves to its own service.

## Shared dependency

- `libs/ftgo-common` — `Money`, `Address`, `PersonName` value objects,
  `UnsupportedStateTransitionException`.
- `libs/ftgo-common-jpa` — ships `META-INF/orm.xml` that declares
  `Money` and `Address` as JPA embeddables without annotating the value
  objects themselves.

## Consumption

```gradle
dependencies {
    implementation "net.chrisrichardson.ftgo:ftgo-domain:1.0.0"
}
```

Services that only need the value objects should depend on
`ftgo-common:1.0.0` (or `ftgo-common-jpa:1.0.0` for the `orm.xml`)
directly — avoid pulling in `ftgo-domain` unless the service actually
persists one of the entities above.
