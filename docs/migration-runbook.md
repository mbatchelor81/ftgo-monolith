# Database Migration Runbook

> **Jira**: EM-29  
> **Purpose**: Step-by-step operational guide for transitioning from the shared `ftgo` database to per-service databases.  
> **Audience**: DevOps engineers, SREs, and backend developers executing the migration.

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Phase 1 — Preparation](#phase-1--preparation)
3. [Phase 2 — Create Per-Service Databases](#phase-2--create-per-service-databases)
4. [Phase 3 — Migrate Data](#phase-3--migrate-data)
5. [Phase 4 — Dual-Write Verification](#phase-4--dual-write-verification)
6. [Phase 5 — Cutover](#phase-5--cutover)
7. [Phase 6 — Cleanup](#phase-6--cleanup)
8. [Rollback Procedures](#rollback-procedures)
9. [Verification Checklist](#verification-checklist)

---

## Prerequisites

- [ ] MySQL 8.0+ running and accessible
- [ ] Root or admin-level MySQL credentials available
- [ ] Full backup of the current `ftgo` database completed
- [ ] All services are on the `feat/microservices-migration-v2` branch (or later)
- [ ] Per-service Flyway migrations exist under each `services/*/src/main/resources/db/migration/`
- [ ] Java 17 configured: `export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64`
- [ ] Maintenance window scheduled (recommended: low-traffic period)

---

## Phase 1 — Preparation

### 1.1 Backup the Shared Database

```bash
# Full logical backup of the shared ftgo database
mysqldump -u root -p --single-transaction --routines --triggers \
  ftgo > ftgo_backup_$(date +%Y%m%d_%H%M%S).sql
```

### 1.2 Record Current State

```sql
-- Record current row counts for post-migration verification
SELECT 'consumers' AS tbl, COUNT(*) AS cnt FROM ftgo.consumers
UNION ALL SELECT 'courier', COUNT(*) FROM ftgo.courier
UNION ALL SELECT 'courier_actions', COUNT(*) FROM ftgo.courier_actions
UNION ALL SELECT 'orders', COUNT(*) FROM ftgo.orders
UNION ALL SELECT 'order_line_items', COUNT(*) FROM ftgo.order_line_items
UNION ALL SELECT 'restaurants', COUNT(*) FROM ftgo.restaurants
UNION ALL SELECT 'restaurant_menu_items', COUNT(*) FROM ftgo.restaurant_menu_items;
```

Save the output — it will be used for verification in Phase 4.

### 1.3 Record Current AUTO_INCREMENT Values

```sql
SELECT 'consumers' AS tbl, MAX(id) AS max_id FROM ftgo.consumers
UNION ALL SELECT 'courier', MAX(id) FROM ftgo.courier
UNION ALL SELECT 'orders', MAX(id) FROM ftgo.orders
UNION ALL SELECT 'restaurants', MAX(id) FROM ftgo.restaurants;
```

### 1.4 Pre-Migration NULL Validation

The target schemas tighten several nullable columns to `NOT NULL`. Run the following checks **before** proceeding to Phase 3. Any non-zero counts must be resolved (update or delete the offending rows) before data migration.

```sql
-- Consumers: first_name and last_name are NOT NULL in target
SELECT 'consumers with NULL names' AS issue, COUNT(*) AS cnt
FROM ftgo.consumers
WHERE first_name IS NULL OR last_name IS NULL;

-- Orders: consumer_id and restaurant_id are NOT NULL in target
SELECT 'orders with NULL consumer_id' AS issue, COUNT(*) AS cnt
FROM ftgo.orders WHERE consumer_id IS NULL
UNION ALL
SELECT 'orders with NULL restaurant_id', COUNT(*)
FROM ftgo.orders WHERE restaurant_id IS NULL;

-- Order line items: menu_item_id is NOT NULL in target
SELECT 'order_line_items with NULL menu_item_id' AS issue, COUNT(*) AS cnt
FROM ftgo.order_line_items WHERE menu_item_id IS NULL;

-- Restaurant menu items: id (mapped to menu_item_id) is NOT NULL in target
SELECT 'restaurant_menu_items with NULL id' AS issue, COUNT(*) AS cnt
FROM ftgo.restaurant_menu_items WHERE id IS NULL;
```

**Expected**: All counts should be zero. If not, clean up the source data before proceeding:
- For consumers: update NULL names to a placeholder or delete incomplete records.
- For orders: investigate orphaned orders missing consumer/restaurant references.
- For line items: investigate and fix or remove rows with missing item IDs.

---

## Phase 2 — Create Per-Service Databases

### 2.1 Create Databases

```sql
CREATE DATABASE IF NOT EXISTS ftgo_consumer_service
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS ftgo_courier_service
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS ftgo_order_service
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS ftgo_restaurant_service
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2.2 Create Service Users (Production)

For production environments, create dedicated MySQL users with least-privilege access:

```sql
-- Consumer Service
CREATE USER IF NOT EXISTS 'ftgo_consumer'@'%' IDENTIFIED BY '<secure-password>';
GRANT ALL PRIVILEGES ON ftgo_consumer_service.* TO 'ftgo_consumer'@'%';

-- Courier Service
CREATE USER IF NOT EXISTS 'ftgo_courier'@'%' IDENTIFIED BY '<secure-password>';
GRANT ALL PRIVILEGES ON ftgo_courier_service.* TO 'ftgo_courier'@'%';

-- Order Service
CREATE USER IF NOT EXISTS 'ftgo_order'@'%' IDENTIFIED BY '<secure-password>';
GRANT ALL PRIVILEGES ON ftgo_order_service.* TO 'ftgo_order'@'%';

-- Restaurant Service
CREATE USER IF NOT EXISTS 'ftgo_restaurant'@'%' IDENTIFIED BY '<secure-password>';
GRANT ALL PRIVILEGES ON ftgo_restaurant_service.* TO 'ftgo_restaurant'@'%';

FLUSH PRIVILEGES;
```

### 2.3 Run Flyway Migrations

Each service runs its own Flyway migration on startup via Spring Boot auto-configuration.  To run migrations manually:

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

# Run migrations for each service individually
# (or start each service — Flyway runs automatically on boot)

# Alternatively, you can use the Gradle Flyway plugin if configured:
# ./gradlew :services:ftgo-order-service:flywayMigrate
```

Or run the SQL migration scripts directly:

```bash
mysql -u root -p ftgo_consumer_service < \
  services/ftgo-consumer-service/src/main/resources/db/migration/V1__create_consumer_service_schema.sql

mysql -u root -p ftgo_courier_service < \
  services/ftgo-courier-service/src/main/resources/db/migration/V1__create_courier_service_schema.sql

mysql -u root -p ftgo_order_service < \
  services/ftgo-order-service/src/main/resources/db/migration/V1__create_order_service_schema.sql

mysql -u root -p ftgo_restaurant_service < \
  services/ftgo-restaurant-service/src/main/resources/db/migration/V1__create_restaurant_service_schema.sql
```

---

## Phase 3 — Migrate Data

### 3.1 Consumer Service Data Migration

```sql
-- Migrate consumers
INSERT INTO ftgo_consumer_service.consumers (id, first_name, last_name)
SELECT id, COALESCE(first_name, ''), COALESCE(last_name, '')
FROM ftgo.consumers;

-- Set AUTO_INCREMENT to continue from the max existing ID
SET @max_id = (SELECT COALESCE(MAX(id), 0) + 1 FROM ftgo_consumer_service.consumers);
SET @sql = CONCAT('ALTER TABLE ftgo_consumer_service.consumers AUTO_INCREMENT = ', @max_id);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
```

### 3.2 Courier Service Data Migration

```sql
-- Migrate couriers (note: source table is singular 'courier', target is plural 'couriers')
INSERT INTO ftgo_courier_service.couriers
  (id, available, first_name, last_name, street1, street2, city, state, zip)
SELECT id, COALESCE(available, 0), first_name, last_name, street1, street2, city, state, zip
FROM ftgo.courier;

-- Migrate courier_actions
INSERT INTO ftgo_courier_service.courier_actions (courier_id, order_id, time, type)
SELECT courier_id, order_id, time, type
FROM ftgo.courier_actions;

-- Set AUTO_INCREMENT counters
SET @max_id = (SELECT COALESCE(MAX(id), 0) + 1 FROM ftgo_courier_service.couriers);
SET @sql = CONCAT('ALTER TABLE ftgo_courier_service.couriers AUTO_INCREMENT = ', @max_id);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
```

### 3.3 Order Service Data Migration

```sql
-- Migrate orders
INSERT INTO ftgo_order_service.orders
  (id, order_state, consumer_id, restaurant_id, assigned_courier_id,
   payment_token, order_minimum,
   delivery_address_street1, delivery_address_street2,
   delivery_address_city, delivery_address_state, delivery_address_zip,
   accept_time, preparing_time, ready_for_pickup_time, picked_up_time,
   delivered_time, delivery_time, ready_by,
   previous_ticket_state, version)
SELECT id, COALESCE(order_state, 'APPROVED'), consumer_id, restaurant_id, assigned_courier_id,
       payment_token, order_minimum,
       delivery_address_street1, delivery_address_street2,
       delivery_address_city, delivery_address_state, delivery_address_zip,
       accept_time, preparing_time, ready_for_pickup_time, picked_up_time,
       delivered_time, delivery_time, ready_by,
       previous_ticket_state, COALESCE(version, 0)
FROM ftgo.orders
WHERE consumer_id IS NOT NULL AND restaurant_id IS NOT NULL;

-- Migrate order_line_items (menu_item_id is NOT NULL in target)
-- Join against orders to exclude line items for orders that were filtered out above
INSERT INTO ftgo_order_service.order_line_items
  (order_id, menu_item_id, name, price, quantity)
SELECT oli.order_id, oli.menu_item_id, oli.name, oli.price, oli.quantity
FROM ftgo.order_line_items oli
JOIN ftgo.orders o ON oli.order_id = o.id
WHERE oli.menu_item_id IS NOT NULL
  AND o.consumer_id IS NOT NULL
  AND o.restaurant_id IS NOT NULL;

-- Set AUTO_INCREMENT counters
SET @max_id = (SELECT COALESCE(MAX(id), 0) + 1 FROM ftgo_order_service.orders);
SET @sql = CONCAT('ALTER TABLE ftgo_order_service.orders AUTO_INCREMENT = ', @max_id);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
```

### 3.4 Restaurant Service Data Migration

```sql
-- Migrate restaurants
INSERT INTO ftgo_restaurant_service.restaurants
  (id, name, street1, street2, city, state, zip)
SELECT id, name, street1, street2, city, state, zip
FROM ftgo.restaurants;

-- Migrate restaurant_menu_items (source 'id' maps to target 'menu_item_id', NOT NULL in target)
INSERT INTO ftgo_restaurant_service.restaurant_menu_items
  (restaurant_id, menu_item_id, name, price)
SELECT restaurant_id, id, name, price
FROM ftgo.restaurant_menu_items
WHERE id IS NOT NULL;

-- Set AUTO_INCREMENT counters
SET @max_id = (SELECT COALESCE(MAX(id), 0) + 1 FROM ftgo_restaurant_service.restaurants);
SET @sql = CONCAT('ALTER TABLE ftgo_restaurant_service.restaurants AUTO_INCREMENT = ', @max_id);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
```

---

## Phase 4 — Dual-Write Verification

Before cutting over, verify data integrity by comparing row counts and spot-checking records.

### 4.1 Row Count Verification

```sql
-- Compare row counts between source and target
SELECT 'consumers' AS entity,
       (SELECT COUNT(*) FROM ftgo.consumers) AS source_count,
       (SELECT COUNT(*) FROM ftgo_consumer_service.consumers) AS target_count
UNION ALL
SELECT 'couriers',
       (SELECT COUNT(*) FROM ftgo.courier),
       (SELECT COUNT(*) FROM ftgo_courier_service.couriers)
UNION ALL
SELECT 'courier_actions',
       (SELECT COUNT(*) FROM ftgo.courier_actions),
       (SELECT COUNT(*) FROM ftgo_courier_service.courier_actions)
UNION ALL
SELECT 'orders',
       (SELECT COUNT(*) FROM ftgo.orders WHERE consumer_id IS NOT NULL AND restaurant_id IS NOT NULL),
       (SELECT COUNT(*) FROM ftgo_order_service.orders)
UNION ALL
SELECT 'order_line_items',
       (SELECT COUNT(*) FROM ftgo.order_line_items oli
        JOIN ftgo.orders o ON oli.order_id = o.id
        WHERE oli.menu_item_id IS NOT NULL
          AND o.consumer_id IS NOT NULL
          AND o.restaurant_id IS NOT NULL),
       (SELECT COUNT(*) FROM ftgo_order_service.order_line_items)
UNION ALL
SELECT 'restaurants',
       (SELECT COUNT(*) FROM ftgo.restaurants),
       (SELECT COUNT(*) FROM ftgo_restaurant_service.restaurants)
UNION ALL
SELECT 'restaurant_menu_items',
       (SELECT COUNT(*) FROM ftgo.restaurant_menu_items WHERE id IS NOT NULL),
       (SELECT COUNT(*) FROM ftgo_restaurant_service.restaurant_menu_items);
```

**Expected**: All `source_count` values must equal `target_count` values.

### 4.2 Spot-Check Records

```sql
-- Verify a sample order exists with correct data in the new database
SELECT o.id, o.order_state, o.consumer_id, o.restaurant_id
FROM ftgo_order_service.orders o
WHERE o.id = (SELECT MIN(id) FROM ftgo.orders);

-- Verify cross-service references are intact
SELECT ca.courier_id, ca.order_id, ca.type
FROM ftgo_courier_service.courier_actions ca
LIMIT 5;
```

### 4.3 Cross-Service Reference Integrity Check

```sql
-- Verify all consumer_ids in orders exist in the consumer service
SELECT o.consumer_id
FROM ftgo_order_service.orders o
LEFT JOIN ftgo_consumer_service.consumers c ON o.consumer_id = c.id
WHERE c.id IS NULL;
-- Expected: empty result set

-- Verify all restaurant_ids in orders exist in the restaurant service
SELECT o.restaurant_id
FROM ftgo_order_service.orders o
LEFT JOIN ftgo_restaurant_service.restaurants r ON o.restaurant_id = r.id
WHERE r.id IS NULL;
-- Expected: empty result set

-- Verify all courier_ids in courier_actions exist in the courier service
SELECT ca.courier_id
FROM ftgo_courier_service.courier_actions ca
LEFT JOIN ftgo_courier_service.couriers c ON ca.courier_id = c.id
WHERE c.id IS NULL;
-- Expected: empty result set
```

---

## Phase 5 — Cutover

### 5.1 Update Service Configuration

Each service's `application.yml` should already point to its own database:

| Service | `spring.datasource.url` |
|---------|------------------------|
| Consumer | `jdbc:mysql://localhost:3306/ftgo_consumer_service` |
| Courier | `jdbc:mysql://localhost:3306/ftgo_courier_service` |
| Order | `jdbc:mysql://localhost:3306/ftgo_order_service` |
| Restaurant | `jdbc:mysql://localhost:3306/ftgo_restaurant_service` |

### 5.2 Update JPA Entity Annotations

The following JPA annotation changes are **required** before deploying against the new per-service schemas:

#### a) ID Generation — switch to `IDENTITY`

Update all entity `@Id` fields to use `GenerationType.IDENTITY`:

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

This replaces any previous use of `GenerationType.TABLE` or `GenerationType.AUTO` that relied on `hibernate_sequence`.

#### b) Table Name Mappings

The following entities need `@Table` annotations added or updated to match the new schema table names:

| Entity | Current Hibernate Default | New Table Name | Required Annotation |
|--------|--------------------------|----------------|--------------------|
| `Courier` | `courier` | `couriers` | `@Table(name = "couriers")` |

The `Courier` entity (`shared/ftgo-domain/.../Courier.java`) currently has no `@Table` annotation, so Hibernate defaults to `courier` (singular). The new schema uses `couriers` (plural). Without this annotation, the service will fail on startup with `ddl-auto: validate`.

#### c) Collection Table Mappings

The `@ElementCollection` / `@CollectionTable` mappings in `Courier` (for `courier_actions`) and `Restaurant` (for `restaurant_menu_items`) may also need updating if the collection table names or join column names changed. Review the `Plan` embeddable and `MenuItem` embeddable against the new schemas.

### 5.3 Deploy Services

1. Deploy updated services one at a time (rolling deployment).
2. Monitor each service's startup logs for successful Flyway migration and JPA schema validation.
3. Verify health endpoints respond: `GET /actuator/health`

### 5.4 Smoke Tests

```bash
# Consumer Service
curl -s http://localhost:8080/consumers/1 | jq .

# Order Service — create a test order
curl -s -X POST http://localhost:8080/orders \
  -H 'Content-Type: application/json' \
  -d '{"consumerId": 1, "restaurantId": 1, "lineItems": [{"menuItemId": "1", "quantity": 2}]}' \
  | jq .

# Courier Service
curl -s http://localhost:8080/couriers/1 | jq .

# Restaurant Service
curl -s http://localhost:8080/restaurants/1 | jq .
```

---

## Phase 6 — Cleanup

After the cutover is stable and verified (recommended: wait at least one business cycle / 7 days):

### 6.1 Remove Shared Database Access

```sql
-- Revoke service access to the shared database (if previously granted)
-- REVOKE ALL ON ftgo.* FROM 'ftgo_consumer'@'%';
-- (repeat for each service user)
```

### 6.2 Archive and Drop the Shared Database

```bash
# Final backup before decommissioning
mysqldump -u root -p --single-transaction ftgo > ftgo_final_archive_$(date +%Y%m%d).sql
```

```sql
-- Only after all services are confirmed stable on per-service databases
-- DROP DATABASE ftgo;
```

### 6.3 Remove Legacy Flyway Module

The `ftgo-flyway/` module and its entry in `settings.gradle` can be removed once no service depends on the shared `ftgo` database.

---

## Rollback Procedures

### Scenario A: Migration Script Failure

If a Flyway migration fails during Phase 2/3:

1. Check the `flyway_schema_history` table in the affected service database for the failed entry.
2. Fix the migration SQL.
3. Run `flyway repair` to remove the failed entry, then re-run.

```bash
# Using Flyway CLI
flyway -url=jdbc:mysql://localhost:3306/ftgo_order_service \
       -user=root -password=rootpassword repair
```

### Scenario B: Data Integrity Issues After Migration

1. Stop the affected service(s).
2. Restore the shared `ftgo` database from the Phase 1 backup.
3. Revert service `application.yml` to point to the shared `ftgo` database.
4. Restart services.
5. Investigate and fix the migration scripts before retrying.

### Scenario C: Full Rollback to Shared Database

1. Stop all microservices.
2. Restore `ftgo` database from backup:
   ```bash
   mysql -u root -p < ftgo_backup_YYYYMMDD_HHMMSS.sql
   ```
3. Revert all service `application.yml` datasource URLs to `jdbc:mysql://localhost:3306/ftgo`.
4. Restart the monolith application (`FtgoApplicationMain`).

---

## Verification Checklist

Use this checklist after completing each phase:

### After Phase 2 (Create Databases)
- [ ] All four service databases created
- [ ] `SHOW DATABASES` lists `ftgo_consumer_service`, `ftgo_courier_service`, `ftgo_order_service`, `ftgo_restaurant_service`
- [ ] Flyway migrations applied — `flyway_schema_history` table exists in each database
- [ ] All expected tables exist in each database

### After Phase 3 (Migrate Data)
- [ ] Row counts match between source and target (Phase 4.1 query)
- [ ] Spot-check queries return expected data (Phase 4.2)
- [ ] Cross-service reference integrity verified (Phase 4.3 query — no orphans)
- [ ] AUTO_INCREMENT counters set correctly

### After Phase 5 (Cutover)
- [ ] All services start successfully with per-service database URLs
- [ ] Health endpoints respond with `UP` status
- [ ] Smoke tests pass for all services
- [ ] No errors in service logs related to database connectivity or schema validation
- [ ] New records can be created and queried in each service
- [ ] Cross-service workflows (e.g., create order → assign courier) function correctly
