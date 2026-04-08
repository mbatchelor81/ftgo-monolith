# Database-Per-Service Migration Strategy

> **Jira**: EM-29  
> **Status**: Approved  
> **Scope**: Transition from a single shared MySQL database (`ftgo`) to independent per-service databases with their own Flyway migration histories.

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [Current State](#current-state)
3. [Target State — Per-Service Database Design](#target-state--per-service-database-design)
4. [Cross-Service Foreign Key Removal Plan](#cross-service-foreign-key-removal-plan)
5. [ID Generation Strategy](#id-generation-strategy)
6. [Per-Service Flyway Migration Structure](#per-service-flyway-migration-structure)
7. [Data Synchronization Approach](#data-synchronization-approach)
8. [Rollback and Data Consistency Strategy](#rollback-and-data-consistency-strategy)
9. [Appendix: Table-to-Service Ownership Map](#appendix-table-to-service-ownership-map)

---

## Executive Summary

The FTGO platform currently uses a **single shared MySQL database** (`ftgo`) with one Flyway migration history.  All seven tables share the same schema, and cross-table foreign keys couple services at the database level.

This document defines the migration to a **database-per-service** architecture where each of the four bounded contexts (Consumer, Courier, Order, Restaurant) owns its own database, Flyway migration history, and ID generation sequence.  Cross-service references are maintained as plain ID columns without FK constraints; data consistency is enforced through **domain events** published via Spring Application Events (in-process during the transition) and eventually via an asynchronous message broker.

---

## Current State

### Shared Database: `ftgo`

| Component | Details |
|-----------|---------|
| Database | `ftgo` on MySQL |
| Flyway | v6.0.0 via `ftgo-flyway/build.gradle` |
| Migration file | `V1__create_ftgo_db.sql` (single file, all 7 tables) |
| ID generation | Shared `hibernate_sequence` table (single row, shared counter) |

### Current Tables

| Table | Owner Service | Notes |
|-------|---------------|-------|
| `consumers` | Consumer Service | `id` assigned via `hibernate_sequence` |
| `courier` | Courier Service | `AUTO_INCREMENT` |
| `courier_actions` | Courier Service | FK → `orders`, FK → `courier` |
| `orders` | Order Service | `AUTO_INCREMENT`, FK → `courier`, FK → `restaurants` |
| `order_line_items` | Order Service | FK → `orders` |
| `restaurants` | Restaurant Service | `AUTO_INCREMENT` |
| `restaurant_menu_items` | Restaurant Service | FK → `restaurants` |
| `hibernate_sequence` | Shared | Used by Consumer entity |

### Current Cross-Service Foreign Keys

| FK Constraint | Source Table | Target Table | Cross-Service? |
|---------------|-------------|-------------|----------------|
| `orders_assigned_courier_id` | `orders.assigned_courier_id` | `courier.id` | **Yes** — Order → Courier |
| `orders_restaurant_id` | `orders.restaurant_id` | `restaurants.id` | **Yes** — Order → Restaurant |
| `courier_actions_order_id` | `courier_actions.order_id` | `orders.id` | **Yes** — Courier → Order |
| `courier_actions_courier_id` | `courier_actions.courier_id` | `courier.id` | No — same service |
| `order_line_items_id` | `order_line_items.order_id` | `orders.id` | No — same service |
| `restaurant_menu_items_restaurant_id` | `restaurant_menu_items.restaurant_id` | `restaurants.id` | No — same service |

---

## Target State — Per-Service Database Design

### Database Mapping

| Service | Database Name | Tables |
|---------|--------------|--------|
| Consumer Service | `ftgo_consumer_service` | `consumers` |
| Courier Service | `ftgo_courier_service` | `couriers`, `courier_actions` |
| Order Service | `ftgo_order_service` | `orders`, `order_line_items` |
| Restaurant Service | `ftgo_restaurant_service` | `restaurants`, `restaurant_menu_items` |

### Schema Changes Summary

| Change | Rationale |
|--------|-----------|
| `courier` → `couriers` (pluralized) | Aligns with project naming convention (see `docs/CONVENTIONS.md`) |
| `hibernate_sequence` removed | Replaced by per-table `AUTO_INCREMENT` |
| Cross-service FK constraints removed | Each service manages its own data; references stored as plain IDs |
| `created_at` / `updated_at` columns added | Supports event ordering and consistency auditing |
| `utf8mb4` charset added | Proper Unicode support for all new tables |
| `order_line_items.id` added | Surrogate PK (was previously a composite/implicit key) |
| `courier_actions.id` added | Surrogate PK for explicit row identity |
| `restaurant_menu_items.id` + `menu_item_id` columns | Surrogate PK + logical menu item identifier |

### Consumer Service — `ftgo_consumer_service`

```sql
CREATE TABLE consumers (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(255) NOT NULL,
    last_name  VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
```

### Courier Service — `ftgo_courier_service`

```sql
CREATE TABLE couriers (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    available  BIT          NOT NULL DEFAULT 0,
    first_name VARCHAR(255),
    last_name  VARCHAR(255),
    street1    VARCHAR(255),
    street2    VARCHAR(255),
    city       VARCHAR(255),
    state      VARCHAR(255),
    zip        VARCHAR(255),
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE courier_actions (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    courier_id BIGINT       NOT NULL,
    order_id   BIGINT,         -- cross-service reference (no FK)
    time       DATETIME,
    type       VARCHAR(255),
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (courier_id) REFERENCES couriers (id)
);
```

### Order Service — `ftgo_order_service`

```sql
CREATE TABLE orders (
    id                       BIGINT       NOT NULL AUTO_INCREMENT,
    order_state              VARCHAR(255) NOT NULL DEFAULT 'APPROVED',
    consumer_id              BIGINT       NOT NULL,   -- cross-service reference (no FK)
    restaurant_id            BIGINT       NOT NULL,   -- cross-service reference (no FK)
    assigned_courier_id      BIGINT,                  -- cross-service reference (no FK)
    payment_token            VARCHAR(255),
    order_minimum            DECIMAL(19, 2),
    delivery_address_street1 VARCHAR(255),
    delivery_address_street2 VARCHAR(255),
    delivery_address_city    VARCHAR(255),
    delivery_address_state   VARCHAR(255),
    delivery_address_zip     VARCHAR(255),
    accept_time              DATETIME,
    preparing_time           DATETIME,
    ready_for_pickup_time    DATETIME,
    picked_up_time           DATETIME,
    delivered_time           DATETIME,
    delivery_time            DATETIME,
    ready_by                 DATETIME,
    previous_ticket_state    INTEGER,
    version                  BIGINT       NOT NULL DEFAULT 0,
    created_at               TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at               TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE order_line_items (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    order_id     BIGINT       NOT NULL,
    menu_item_id VARCHAR(255) NOT NULL,
    name         VARCHAR(255),
    price        DECIMAL(19, 2),
    quantity     INTEGER      NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE
);
```

### Restaurant Service — `ftgo_restaurant_service`

```sql
CREATE TABLE restaurants (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    name       VARCHAR(255) NOT NULL,
    street1    VARCHAR(255),
    street2    VARCHAR(255),
    city       VARCHAR(255),
    state      VARCHAR(255),
    zip        VARCHAR(255),
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE restaurant_menu_items (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    restaurant_id BIGINT       NOT NULL,
    menu_item_id  VARCHAR(255) NOT NULL,
    name          VARCHAR(255),
    price         DECIMAL(19, 2),
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (restaurant_id) REFERENCES restaurants (id) ON DELETE CASCADE
);
```

---

## Cross-Service Foreign Key Removal Plan

### Principle

In a database-per-service architecture, foreign keys **cannot span databases**.  Cross-service references are stored as **plain ID columns** with application-level validation.

### Removed FK Constraints

| Removed FK | Service Boundary | Replacement Strategy |
|------------|-----------------|---------------------|
| `orders.assigned_courier_id → courier.id` | Order → Courier | Store `assigned_courier_id` as BIGINT; validate via Courier Service API call during assignment |
| `orders.restaurant_id → restaurants.id` | Order → Restaurant | Store `restaurant_id` as BIGINT; validate via Restaurant Service API call during order creation |
| `orders.consumer_id → consumers.id` (implicit) | Order → Consumer | Store `consumer_id` as BIGINT; validate via `ConsumerService.validateOrderForConsumer()` |
| `courier_actions.order_id → orders.id` | Courier → Order | Store `order_id` as BIGINT; populated via domain events from Order Service |

### Data Integrity Without FKs

For each cross-service reference, we apply the following safeguards:

1. **Write-time validation**: Before persisting a cross-service ID, the owning service calls the referenced service's API to verify the entity exists (e.g., Order Service calls Consumer Service before creating an order).

2. **Event-driven propagation**: When an entity is deleted or its state changes, the owning service publishes a domain event.  Consuming services react by updating or invalidating their local references.

3. **Indexed columns**: All cross-service ID columns have indexes for efficient lookups and orphan detection.

4. **Periodic reconciliation** (optional): A scheduled job can compare cross-service IDs against the owning service's API to detect and report orphaned references.

---

## ID Generation Strategy

### Decision: Per-Table AUTO_INCREMENT

| Approach | Pros | Cons | Selected? |
|----------|------|------|-----------|
| **Per-table AUTO_INCREMENT** | Simple, native MySQL, zero coordination, excellent performance | IDs not globally unique across services | **Yes** |
| UUIDs | Globally unique, no coordination | 36 chars, poor index locality, larger storage | No |
| Snowflake IDs | Globally unique, sortable, compact | Requires clock synchronization, custom generator | No |

### Rationale

- The current schema already uses `AUTO_INCREMENT` for `courier`, `orders`, and `restaurants`.  Only `consumers` used the shared `hibernate_sequence`.
- With database-per-service, there is no risk of ID collision between services since each service has its own database.
- Cross-service references always include the **service context** (e.g., `consumer_id` on the `orders` table), making it clear which service owns the ID.
- `AUTO_INCREMENT` gives optimal INSERT performance and B-tree locality on InnoDB.

### Migration from `hibernate_sequence`

The shared `hibernate_sequence` table is **not created** in any per-service schema.  Instead:

1. The `consumers` table switches from `hibernate_sequence`-based ID assignment to `AUTO_INCREMENT`.
2. During data migration, the `AUTO_INCREMENT` counter is set to `MAX(id) + 1` from the existing data to avoid ID conflicts.
3. JPA entities should use `@GeneratedValue(strategy = GenerationType.IDENTITY)` instead of `GenerationType.TABLE` or `GenerationType.AUTO`.

---

## Per-Service Flyway Migration Structure

### Directory Layout

```
services/
├── ftgo-consumer-service/
│   └── src/main/resources/db/migration/
│       └── V1__create_consumer_service_schema.sql
├── ftgo-courier-service/
│   └── src/main/resources/db/migration/
│       └── V1__create_courier_service_schema.sql
├── ftgo-order-service/
│   └── src/main/resources/db/migration/
│       └── V1__create_order_service_schema.sql
└── ftgo-restaurant-service/
    └── src/main/resources/db/migration/
        └── V1__create_restaurant_service_schema.sql
```

### Naming Convention

```
V{version}__{description}.sql
```

| Component | Rule | Example |
|-----------|------|---------|
| Version | Sequential integer, starting at 1 | `V1`, `V2`, `V3` |
| Separator | Double underscore `__` | |
| Description | `snake_case`, descriptive | `create_order_service_schema` |
| Extension | `.sql` | |

Examples:
- `V1__create_order_service_schema.sql` — initial schema
- `V2__add_order_total_column.sql` — additive change
- `V3__create_outbox_table.sql` — event outbox for Saga

### Flyway Configuration (per service `application.yml`)

Each service's `application.yml` already configures Flyway to use `classpath:db/migration`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ftgo_order_service
  flyway:
    enabled: true
    locations: classpath:db/migration
```

### Key Rules

1. **Each service manages its own migration history independently.**  The `flyway_schema_history` table lives in each service's database.
2. **Never reference tables from another service's database** in a migration.
3. **Migrations are additive and forward-only.**  Destructive changes (column drops, table drops) require a multi-step approach (deprecate → stop writing → migrate data → drop).
4. **The legacy `ftgo-flyway` module is preserved** for the monolith during the transition period.  It is not modified by this task.

---

## Data Synchronization Approach

### Strategy: Event-Driven Synchronization

Data consistency across service boundaries is maintained through **domain events** rather than shared database transactions.

### Phase 1 — In-Process Spring Application Events (Current)

During the monolith-to-microservices transition, all services still run in the same JVM (`FtgoApplicationMain`).  Cross-service communication uses Spring's `ApplicationEventPublisher`:

```java
// Order Service publishes when a courier is assigned
applicationEventPublisher.publishEvent(new OrderAssignedEvent(orderId, courierId));

// Courier Service listens
@EventListener
public void onOrderAssigned(OrderAssignedEvent event) {
    courierService.addAction(event.getCourierId(),
        Action.makePickup(event.getOrderId()));
}
```

This provides **synchronous, transactional consistency** within the monolith.

### Phase 2 — Asynchronous Messaging (Post-Extraction)

Once services are deployed independently, events transition to an **asynchronous message broker** (e.g., Apache Kafka, RabbitMQ, or AWS SNS/SQS):

```
┌──────────────┐    Domain Event     ┌──────────────────┐
│ Order Service │ ──────────────────► │  Message Broker   │
│               │  OrderCreated       │  (Kafka / SQS)    │
└──────────────┘  OrderAssigned       └────────┬─────────┘
                  OrderCancelled               │
                                               ▼
                                    ┌──────────────────┐
                                    │ Courier Service   │
                                    │ Consumer Service  │
                                    │ Restaurant Service│
                                    └──────────────────┘
```

### Domain Events Catalog

| Event | Publisher | Consumers | Payload |
|-------|-----------|-----------|---------|
| `ConsumerCreated` | Consumer Service | Order Service (cache) | `consumerId`, `name` |
| `ConsumerValidated` | Consumer Service | Order Service | `consumerId`, `valid` |
| `RestaurantCreated` | Restaurant Service | Order Service (cache) | `restaurantId`, `name`, `menuItems` |
| `RestaurantMenuRevised` | Restaurant Service | Order Service | `restaurantId`, `menuItems` |
| `OrderCreated` | Order Service | Courier Service, Restaurant Service | `orderId`, `restaurantId`, `deliveryAddress` |
| `OrderAccepted` | Order Service | Courier Service | `orderId`, `restaurantId` |
| `OrderAssigned` | Order Service | Courier Service | `orderId`, `courierId` |
| `OrderCancelled` | Order Service | Courier Service, Restaurant Service | `orderId` |
| `CourierAvailable` | Courier Service | Order Service | `courierId`, `location` |
| `CourierPickedUp` | Courier Service | Order Service | `courierId`, `orderId` |
| `CourierDelivered` | Courier Service | Order Service | `courierId`, `orderId` |

### Eventual Consistency Guarantees

- **At-least-once delivery**: Events may be delivered more than once; consumers must be **idempotent**.
- **Ordering within an aggregate**: Events for the same aggregate (e.g., same `orderId`) are processed in order.
- **Transactional Outbox** (Phase 2): To avoid dual-write problems, domain events are written to an `outbox` table within the same transaction as the business data, then relayed to the broker by a separate polling/CDC process.

---

## Rollback and Data Consistency Strategy

### Flyway Rollback

Flyway Community Edition does not support `undo` migrations.  Rollback is handled via **compensating forward migrations**:

| Scenario | Rollback Approach |
|----------|------------------|
| Bad V2 migration (additive) | Create V3 that reverses the V2 change |
| Bad V1 (initial schema) | Drop and recreate the database, re-run V1 |
| Data corruption | Restore from database backup + replay events |

### Database Backup Strategy

| Phase | Backup Approach |
|-------|----------------|
| Pre-migration | Full `mysqldump` of the shared `ftgo` database |
| During dual-write | Incremental backups of both old and new databases |
| Post-migration | Per-service automated backups (MySQL scheduled backups or cloud-native snapshots) |

### Cross-Service Data Consistency

Since we lose ACID transactions across service boundaries, we adopt:

1. **Saga Pattern**: Multi-step business processes (e.g., order creation involving Consumer validation, Restaurant verification, and Courier assignment) are coordinated via a Saga that issues compensating actions on failure.

   ```
   CreateOrder Saga:
     1. Consumer Service → validateConsumer(consumerId)
     2. Restaurant Service → validateRestaurant(restaurantId)
     3. Order Service → createOrder(...)
     4. Courier Service → scheduleCourier(orderId)
   
   Compensation (on failure at step 4):
     3c. Order Service → cancelOrder(orderId)
   ```

2. **Idempotent Event Handlers**: All event consumers use idempotency keys (event ID or aggregate version) to safely handle duplicate deliveries.

3. **Reconciliation Jobs**: Periodic background jobs compare cross-service references against source-of-truth services and flag discrepancies for manual review or automated correction.

---

## Appendix: Table-to-Service Ownership Map

| Table (Monolith) | Table (Microservice) | Database | Owner Service | Cross-Service Refs |
|-------------------|---------------------|----------|---------------|-------------------|
| `consumers` | `consumers` | `ftgo_consumer_service` | Consumer Service | — |
| `courier` | `couriers` | `ftgo_courier_service` | Courier Service | — |
| `courier_actions` | `courier_actions` | `ftgo_courier_service` | Courier Service | `order_id` → Order Service |
| `orders` | `orders` | `ftgo_order_service` | Order Service | `consumer_id` → Consumer, `restaurant_id` → Restaurant, `assigned_courier_id` → Courier |
| `order_line_items` | `order_line_items` | `ftgo_order_service` | Order Service | — |
| `restaurants` | `restaurants` | `ftgo_restaurant_service` | Restaurant Service | — |
| `restaurant_menu_items` | `restaurant_menu_items` | `ftgo_restaurant_service` | Restaurant Service | — |
| `hibernate_sequence` | *(removed)* | — | — | Replaced by per-table `AUTO_INCREMENT` |
