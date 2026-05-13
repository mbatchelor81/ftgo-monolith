# Per-Service Database Schema Design

## Overview

This document describes the database-per-service decomposition strategy for
the FTGO platform. The monolith currently uses a single MySQL database (`ftgo`)
with seven tables and one shared ID-generation table (`hibernate_sequence`).
After migration, each bounded context owns an independent database (or schema)
with its own Flyway migration history.

## Current State (Monolith)

```
Database: ftgo
├── consumers                  (Consumer Service)
├── courier                    (Courier Service)
├── courier_actions            (Courier Service)
├── orders                     (Order Service)
├── order_line_items           (Order Service)
├── restaurants                (Restaurant Service)
├── restaurant_menu_items      (Restaurant Service)
└── hibernate_sequence         (shared — used by Consumer entity)
```

### Cross-Service Foreign Keys (to be removed)

| FK Constraint | Source Table | Target Table | Owning Services |
|---------------|-------------|-------------|-----------------|
| `courier_actions_order_id` | `courier_actions.order_id` | `orders.id` | Courier → Order |
| `orders_assigned_courier_id` | `orders.assigned_courier_id` | `courier.id` | Order → Courier |
| `orders_restaurant_id` | `orders.restaurant_id` | `restaurants.id` | Order → Restaurant |

### Intra-Service Foreign Keys (retained)

| FK Constraint | Source Table | Target Table | Service |
|---------------|-------------|-------------|---------|
| `courier_actions_courier_id` | `courier_actions.courier_id` | `courier.id` | Courier |
| `order_line_items_id` | `order_line_items.order_id` | `orders.id` | Order |
| `restaurant_menu_items_restaurant_id` | `restaurant_menu_items.restaurant_id` | `restaurants.id` | Restaurant |

## Target State (Per-Service Databases)

Each service receives its own database with a dedicated Flyway migration
history. Cross-service foreign keys are replaced by plain ID columns that
reference entities in other services via API calls or domain events.

### Database Layout

```
Database: ftgo_consumer_service
├── consumers
└── flyway_schema_history

Database: ftgo_courier_service
├── courier
├── courier_actions
└── flyway_schema_history

Database: ftgo_order_service
├── orders
├── order_line_items
└── flyway_schema_history

Database: ftgo_restaurant_service
├── restaurants
├── restaurant_menu_items
└── flyway_schema_history
```

## Per-Service Schema Definitions

### Consumer Service (`ftgo_consumer_service`)

**Tables**: `consumers`

```sql
CREATE TABLE consumers (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(255),
    last_name  VARCHAR(255),
    PRIMARY KEY (id)
) ENGINE = InnoDB;
```

**Changes from monolith**:
- ID generation changed from `hibernate_sequence` (TABLE strategy) to
  `AUTO_INCREMENT` (IDENTITY strategy). This aligns with all other entities
  and removes the need for a shared sequence table.

### Courier Service (`ftgo_courier_service`)

**Tables**: `courier`, `courier_actions`

```sql
CREATE TABLE courier (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    available  BIT,
    first_name VARCHAR(255),
    last_name  VARCHAR(255),
    street1    VARCHAR(255),
    street2    VARCHAR(255),
    city       VARCHAR(255),
    state      VARCHAR(255),
    zip        VARCHAR(255),
    PRIMARY KEY (id)
) ENGINE = InnoDB;

CREATE TABLE courier_actions (
    courier_id BIGINT       NOT NULL,
    order_id   BIGINT,
    time       DATETIME,
    type       VARCHAR(255),
    INDEX idx_courier_actions_courier_id (courier_id),
    INDEX idx_courier_actions_order_id (order_id),
    CONSTRAINT fk_courier_actions_courier
        FOREIGN KEY (courier_id) REFERENCES courier (id)
) ENGINE = InnoDB;
```

**Changes from monolith**:
- `courier_actions.order_id` no longer has a foreign key to `orders`.
  The column is retained as a plain `BIGINT` reference. Order existence is
  validated at the application layer via the Order Service API.
- Added index on `order_id` for efficient lookup by order.

### Order Service (`ftgo_order_service`)

**Tables**: `orders`, `order_line_items`

```sql
CREATE TABLE orders (
    id                       BIGINT       NOT NULL AUTO_INCREMENT,
    accept_time              DATETIME,
    consumer_id              BIGINT,
    delivery_address_city    VARCHAR(255),
    delivery_address_state   VARCHAR(255),
    delivery_address_street1 VARCHAR(255),
    delivery_address_street2 VARCHAR(255),
    delivery_address_zip     VARCHAR(255),
    delivery_time            DATETIME,
    order_state              VARCHAR(255),
    order_minimum            DECIMAL(19, 2),
    payment_token            VARCHAR(255),
    picked_up_time           DATETIME,
    delivered_time           DATETIME,
    preparing_time           DATETIME,
    previous_ticket_state    INTEGER,
    ready_by                 DATETIME,
    ready_for_pickup_time    DATETIME,
    version                  BIGINT,
    assigned_courier_id      BIGINT,
    restaurant_id            BIGINT,
    PRIMARY KEY (id),
    INDEX idx_orders_consumer_id (consumer_id),
    INDEX idx_orders_restaurant_id (restaurant_id),
    INDEX idx_orders_assigned_courier_id (assigned_courier_id),
    INDEX idx_orders_state (order_state)
) ENGINE = InnoDB;

CREATE TABLE order_line_items (
    order_id     BIGINT       NOT NULL,
    menu_item_id VARCHAR(255),
    name         VARCHAR(255),
    price        DECIMAL(19, 2),
    quantity     INTEGER      NOT NULL,
    INDEX idx_order_line_items_order_id (order_id),
    CONSTRAINT fk_order_line_items_order
        FOREIGN KEY (order_id) REFERENCES orders (id)
) ENGINE = InnoDB;
```

**Changes from monolith**:
- `orders.assigned_courier_id` no longer has a foreign key to `courier`.
  Courier assignment is validated via the Courier Service API.
- `orders.restaurant_id` no longer has a foreign key to `restaurants`.
  Restaurant existence is validated via the Restaurant Service API.
- `orders.consumer_id` has no foreign key (was never present in monolith
  either, but noted for completeness). Consumer existence is validated via
  the Consumer Service API.
- Added indexes on `consumer_id`, `restaurant_id`, `assigned_courier_id`,
  and `order_state` for common query patterns.

### Restaurant Service (`ftgo_restaurant_service`)

**Tables**: `restaurants`, `restaurant_menu_items`

```sql
CREATE TABLE restaurants (
    id      BIGINT       NOT NULL AUTO_INCREMENT,
    name    VARCHAR(255),
    street1 VARCHAR(255),
    street2 VARCHAR(255),
    city    VARCHAR(255),
    state   VARCHAR(255),
    zip     VARCHAR(255),
    PRIMARY KEY (id)
) ENGINE = InnoDB;

CREATE TABLE restaurant_menu_items (
    restaurant_id BIGINT       NOT NULL,
    id            VARCHAR(255),
    name          VARCHAR(255),
    price         DECIMAL(19, 2),
    INDEX idx_restaurant_menu_items_restaurant_id (restaurant_id),
    CONSTRAINT fk_restaurant_menu_items_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants (id)
) ENGINE = InnoDB;
```

**Changes from monolith**: No structural changes. All tables and
relationships are already self-contained within the Restaurant bounded context.

## ID Generation Strategy

### Current State

| Entity | Strategy | Mechanism |
|--------|----------|-----------|
| `Consumer` | `@GeneratedValue` (AUTO) | `hibernate_sequence` table |
| `Courier` | `@GeneratedValue(IDENTITY)` | MySQL `AUTO_INCREMENT` |
| `Order` | `@GeneratedValue(IDENTITY)` | MySQL `AUTO_INCREMENT` |
| `Restaurant` | `@GeneratedValue(IDENTITY)` | MySQL `AUTO_INCREMENT` |

### Target State

All entities use `@GeneratedValue(strategy = GenerationType.IDENTITY)` with
MySQL `AUTO_INCREMENT`. This:

1. Eliminates the shared `hibernate_sequence` table.
2. Provides per-table, per-database ID generation with no cross-service
   coordination.
3. Aligns Consumer Service with the pattern already used by the other
   three services.

**Note**: After decomposition, IDs are only unique within a service's
database. Cross-service references use the combination of service name
and entity ID (e.g., "consumer:42"). Services must never assume that an
ID from another service's domain is globally unique.

## Flyway Migration Structure

### Directory Layout

```
services/
├── consumer-service/
│   └── consumer-service-app/
│       └── src/main/resources/db/migration/
│           └── V1__create_consumer_schema.sql
├── courier-service/
│   └── courier-service-app/
│       └── src/main/resources/db/migration/
│           └── V1__create_courier_schema.sql
├── order-service/
│   └── order-service-app/
│       └── src/main/resources/db/migration/
│           └── V1__create_order_schema.sql
└── restaurant-service/
    └── restaurant-service-app/
        └── src/main/resources/db/migration/
            └── V1__create_restaurant_schema.sql
```

### Naming Conventions

| Convention | Rule |
|-----------|------|
| Prefix | `V` for versioned (repeatable migrations use `R`) |
| Version | Integer, monotonically increasing per service |
| Separator | `__` (double underscore) |
| Description | Snake_case summary of the change |
| Example | `V1__create_consumer_schema.sql` |
| Example | `V2__add_consumer_email_column.sql` |

Each service maintains its own independent version sequence starting at
`V1`. Flyway tracks migration history in a per-database
`flyway_schema_history` table.

### Flyway Configuration

Each service's `application.yml` should configure Flyway to target its
own database:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ftgo_consumer_service
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: false
```

## Migration Execution Plan

### Phase 1: Create Per-Service Schemas (This PR)

1. Define per-service Flyway migration files.
2. Document schema design and data consistency strategy.
3. Monolith continues to use the shared `ftgo` database — no changes to
   existing monolith code or migrations.

### Phase 2: Dual-Write Period

1. Deploy per-service databases alongside the monolith database.
2. Monolith writes to both old and new databases during transition.
3. Validate data consistency between old and new schemas.

### Phase 3: Cutover

1. Switch each service to read/write exclusively from its own database.
2. Update `application.yml` datasource URLs to per-service databases.
3. Decommission cross-service foreign keys in the monolith database.

### Phase 4: Cleanup

1. Remove the monolith's `ftgo` database once all services are migrated.
2. Remove `hibernate_sequence` table (no longer needed).
3. Archive `ftgo-flyway/` module (retained for reference only).
