# Per-Service Database Schema Migration Strategy

## Overview

This document describes the strategy for migrating from a single shared MySQL database (`ftgo`) to independent per-service databases, each with its own Flyway migration history.

## Current State (Monolith)

- **Single database**: `ftgo` on MySQL
- **Single Flyway migration set**: `ftgo-flyway/src/main/resources/db/migration/V1__create_ftgo_db.sql`
- **Shared `hibernate_sequence`** for ID generation
- **Cross-table foreign keys** spanning service boundaries

### Table Ownership

| Table | Owning Service | Notes |
|-------|---------------|-------|
| `consumers` | Consumer Service | Embeds `PersonName` |
| `courier` | Courier Service | Embeds `PersonName`, `Address`, `Plan` |
| `courier_actions` | Courier Service | References `order_id` (cross-service) |
| `orders` | Order Service | References `restaurant_id`, `assigned_courier_id`, `consumer_id` (cross-service) |
| `order_line_items` | Order Service | Child of `orders` |
| `restaurants` | Restaurant Service | Embeds `Address` |
| `restaurant_menu_items` | Restaurant Service | Child of `restaurants` |
| `hibernate_sequence` | Shared (removed) | Replaced by per-service IDENTITY strategy |

### Cross-Service Foreign Keys (Removed)

| FK Constraint | From | To | Resolution |
|--------------|------|-----|------------|
| `orders.restaurant_id → restaurants.id` | Order Service | Restaurant Service | Store as plain `BIGINT` (no FK) |
| `orders.assigned_courier_id → courier.id` | Order Service | Courier Service | Store as plain `BIGINT` (no FK) |
| `orders.consumer_id` | Order Service | Consumer Service | Already plain `BIGINT` (no change) |
| `courier_actions.order_id → orders.id` | Courier Service | Order Service | Store as plain `BIGINT` (no FK) |
| `courier_actions.courier_id → courier.id` | Courier Service | Courier Service | Retained (same service) |

## Target State (Microservices)

### Per-Service Databases

| Service | Database Name | Tables |
|---------|--------------|--------|
| Consumer Service | `ftgo_consumer_service` | `consumers` |
| Courier Service | `ftgo_courier_service` | `courier`, `courier_actions` |
| Order Service | `ftgo_order_service` | `orders`, `order_line_items` |
| Restaurant Service | `ftgo_restaurant_service` | `restaurants`, `restaurant_menu_items` |

### ID Generation Strategy

**Decision: IDENTITY (auto_increment)**

The shared `hibernate_sequence` is replaced by per-service `AUTO_INCREMENT` primary keys (`GenerationType.IDENTITY`). This avoids cross-service sequence coordination and is the simplest approach for MySQL.

**Trade-offs considered:**
- ✅ `IDENTITY` — Simple, no shared state, native MySQL support. **Chosen.**
- ❌ `UUID` — Larger storage, poor index performance on InnoDB clustered indexes.
- ❌ `Snowflake IDs` — Requires additional infrastructure for ID generation.

### Flyway Migration Structure

Each service has its own Flyway migration directory:

```
services/
├── ftgo-consumer-service/src/main/resources/db/migration/
│   └── V1__create_consumer_service_schema.sql
├── ftgo-courier-service/src/main/resources/db/migration/
│   └── V1__create_courier_service_schema.sql
├── ftgo-order-service/src/main/resources/db/migration/
│   └── V1__create_order_service_schema.sql
└── ftgo-restaurant-service/src/main/resources/db/migration/
    └── V1__create_restaurant_service_schema.sql
```

**Naming convention**: `V<version>__<description>.sql`
- Version numbers are per-service (each service starts at V1)
- Use descriptive snake_case names

### Flyway Configuration

Each service's `application.yml` includes:

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    baseline-version: 0
```

## Data Consistency Strategy

### Approach: Event-Driven Eventual Consistency

With cross-service foreign keys removed, data consistency is maintained through:

1. **Domain Events**: Services publish events when entities change (e.g., `OrderCreated`, `CourierAssigned`). Other services consume these events to update local read models or trigger workflows.

2. **API Calls for Validation**: When a service needs to validate data owned by another service (e.g., Order Service validating a consumer exists), it makes a synchronous API call to the owning service.

3. **Saga Pattern**: Multi-service transactions (e.g., order creation involving Consumer, Restaurant, and Courier services) use the Saga pattern with compensating transactions for rollback.

### Cross-Service Reference Integrity

| Reference | Validation Approach |
|-----------|-------------------|
| `orders.consumer_id` | API call to Consumer Service during order creation |
| `orders.restaurant_id` | API call to Restaurant Service during order creation |
| `orders.assigned_courier_id` | Set via Courier Service event after assignment |
| `courier_actions.order_id` | Set via Order Service event after order state change |

## Migration Runbook

### Phase 1: Schema Preparation (Current)
1. ✅ Create per-service Flyway migration files
2. ✅ Add Flyway dependencies to service `build.gradle` files
3. ✅ Configure Flyway in service `application.yml` files
4. ✅ Update JDBC driver to `com.mysql.cj.jdbc.Driver`

### Phase 2: Data Migration (Future)
1. Create per-service databases on MySQL
2. Run Flyway migrations for each service
3. Export data from shared `ftgo` database, partitioned by service
4. Import data into per-service databases
5. Verify row counts and data integrity

### Phase 3: Cutover (Future)
1. Stop monolith application
2. Point each microservice to its own database
3. Start microservices
4. Verify all services are healthy
5. Run integration tests

### Rollback Plan
1. Stop microservices
2. Re-point to shared `ftgo` database
3. Restart monolith application
4. Data written during microservices operation may need manual reconciliation

## Dependencies

- **EM-30**: Repository structure (provides `services/` directory layout)
- **EM-31**: ftgo-domain extraction (provides entity ownership mapping)
- **Flyway 9.22.3**: Version catalog entry in `gradle/libs.versions.toml`
- **MySQL Connector 8.3.0**: Updated driver class `com.mysql.cj.jdbc.Driver`
