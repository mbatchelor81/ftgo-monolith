# FTGO Database-per-Service Migration Strategy

- **Status**: Accepted (EM-29)
- **Scope**: `services/*/src/main/resources/db/`, `ftgo-flyway/`
- **Related**: [ADR-0002](adr/0002-database-per-service.md), CONVENTIONS.md §4

This document is the normative reference for how FTGO migrates from a
single shared MySQL database (`ftgo`) managed by a single Flyway
migration set (`ftgo-flyway/`) to an independent database per service
with its own migration history. It complements
[ADR-0002: Database-per-Service Topology](adr/0002-database-per-service.md),
which records the decision; this document defines the mechanics.

---

## 1. Target Topology

Each bounded context owns exactly one MySQL schema. The schema name
matches the `FTGO_<context>_DB_URL` env var and the default datasource
URLs shipped in each service's `config/application.yml`.

| Bounded Context | Schema            | Default JDBC URL                                        |
|-----------------|-------------------|---------------------------------------------------------|
| Consumer        | `ftgo_consumer`   | `jdbc:mysql://localhost:3306/ftgo_consumer`             |
| Order           | `ftgo_order`      | `jdbc:mysql://localhost:3306/ftgo_order`                |
| Restaurant      | `ftgo_restaurant` | `jdbc:mysql://localhost:3306/ftgo_restaurant`           |
| Courier         | `ftgo_courier`    | `jdbc:mysql://localhost:3306/ftgo_courier`              |

Physical isolation is achieved today by distinct MySQL schemas on the
same server; the same Flyway setup works unchanged if a given schema is
later moved to its own MySQL instance. The only contract is "one
schema == one service."

### 1.1 Schema / table ownership matrix

| Table                      | Owner schema         | Accessed by                                         |
|----------------------------|----------------------|-----------------------------------------------------|
| `consumers`                | `ftgo_consumer`      | consumer-service only                               |
| `orders`                   | `ftgo_order`         | order-service only                                  |
| `order_line_items`         | `ftgo_order`         | order-service only                                  |
| `restaurants`              | `ftgo_restaurant`    | restaurant-service only                             |
| `restaurant_menu_items`    | `ftgo_restaurant`    | restaurant-service only                             |
| `courier`                  | `ftgo_courier`       | courier-service only                                |
| `courier_actions`          | `ftgo_courier`       | courier-service only                                |

**Rule:** A service's datasource user is granted DDL + DML on its own
schema only. Peer-service reads go through REST APIs or the event bus
— **never** through a cross-schema SELECT.

---

## 2. Cross-Service Reference Decoupling

The monolith schema defined six foreign keys. Three are intra-context
and survive; three are cross-context and are replaced by
service-local references.

| Current FK                                            | Direction   | Action          |
|-------------------------------------------------------|-------------|-----------------|
| `order_line_items.order_id` → `orders.id`             | intra-Order | **KEEP**        |
| `restaurant_menu_items.restaurant_id` → `restaurants.id` | intra-Restaurant | **KEEP** |
| `courier_actions.courier_id` → `courier.id`           | intra-Courier | **KEEP**       |
| `orders.restaurant_id` → `restaurants.id`             | Order→Restaurant | **DROP** (store id only)      |
| `orders.assigned_courier_id` → `courier.id`           | Order→Courier    | **DROP** (store id only)      |
| `courier_actions.order_id` → `orders.id`              | Courier→Order    | **DROP** (store id only)      |

Additionally, `orders.consumer_id` — which was already an implicit
reference (no FK in the monolith) — stays as a plain `BIGINT` column.

### 2.1 Indexing after FK removal

Dropping the FK also drops the implicit index MySQL maintained on the
FK column. Any query that previously relied on the FK index now
needs an explicit one. The V1 migrations add:

- `idx_orders_consumer_id`, `idx_orders_restaurant_id`,
  `idx_orders_assigned_courier_id` on `ftgo_order.orders`
- `idx_courier_actions_order_id` on `ftgo_courier.courier_actions`

### 2.2 JPA consequences (out of scope for this ticket, documented here)

The Java entities currently model these references as `@ManyToOne`
associations. The code extraction tickets
(`EM-3x` per-service extraction) must replace them with primitive
fields, e.g.:

```java
// BEFORE (monolith)
@ManyToOne(fetch = FetchType.LAZY)
private Restaurant restaurant;

// AFTER (post-EM-29)
@Column(name = "restaurant_id")
private Long restaurantId;
```

Any service that needs the _content_ of the referenced aggregate reads
it from a local read-model populated by events (see §6) or issues a
REST call to the owning service (guarded by Resilience4j per the
existing `ftgo.resilience.*` config).

---

## 3. Per-Service Flyway Layout

```
services/<context>-service/
└── src/main/resources/db/migration/
    ├── V1__create_<context>_schema.sql     # owned by this ticket
    ├── V2__<descriptive_snake_case>.sql    # future change
    └── …
```

Each service has its own `flyway_schema_history` table, auto-created by
Flyway on first run inside that service's schema. There is **no shared
history** across services.

### 3.1 Naming convention

- **File:** `V<major>[.<minor>…]__<snake_case_description>.sql`
  (Flyway 6.0.0 default parser; matches the rule already codified in
  [CONVENTIONS.md §4](../CONVENTIONS.md)).
- **Version numbers are per-service, not global.** Order-service's
  `V7` and consumer-service's `V7` are unrelated and may ship in any
  order.
- **Description** should be a short imperative summary of the schema
  change (`create_initial_schema`, `add_payment_token_column`,
  `drop_assigned_courier_fk`). Avoid ticket IDs in file names — put
  them in the SQL header comment instead.
- **Repeatable migrations** (views, seed data) use the `R__` prefix
  and live alongside versioned scripts. They are not used in V1 but
  are allowed for future per-service read-model refreshes.

### 3.2 Flyway configuration

Per-service Flyway runs as part of Spring Boot startup — each service's
`application.yml` already has:

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
```

No explicit datasource override is required — Flyway reuses
`spring.datasource` which points at the service's own schema.

The legacy `ftgo-flyway/` Gradle module remains on `master` and on this
branch for the transition period; its `V1__create_ftgo_db.sql` stays in
place so existing monolith integration tests keep running. Once
`ftgo-application` retires (tracked by the per-service extraction
tickets), `ftgo-flyway/` is deleted. See
[`ftgo-flyway/README.md`](../ftgo-flyway/README.md) for the deprecation
timeline.

---

## 4. ID Generation (Replacing `hibernate_sequence`)

The monolith uses a single table-backed Hibernate sequence
(`hibernate_sequence`) shared across **every** JPA entity in the
process. That row is globally contested, couples services via shared
write lock on one sequence row, and blocks per-service deploys.

### 4.1 Decision

- Every per-service schema owns its **own** identity strategy.
- Default: native MySQL `AUTO_INCREMENT` on each primary key.
- JPA entities use `@GeneratedValue(strategy = GenerationType.IDENTITY)`.
- The shared `hibernate_sequence` table is **not** created in any
  per-service schema.

### 4.2 Rationale (alternatives considered)

- **Per-entity table sequence** (one row per entity, Hibernate
  `TABLE` strategy): works but reintroduces the "sequence row is a
  hot lock" problem inside each service. Rejected unless a later
  bounded context needs pre-allocated batches.
- **UUID primary keys**: strictly better for cross-service merges
  (no collisions on a future sharded setup) but invalidates every
  existing numeric ID the monolith has in flight. Out of scope for
  the EM-29 schema split; revisit as a separate ticket when rolling
  out the data migration (§7).
- **Application-generated snowflake IDs**: avoids DB round-trips on
  insert but adds a new runtime dependency (ID service). Overkill
  for our insert rates; revisit if IDENTITY becomes a throughput
  ceiling.

### 4.3 Migration notes per entity

| Entity     | Monolith strategy            | New strategy        |
|------------|------------------------------|---------------------|
| `Consumer` | `@GeneratedValue` (AUTO → TABLE via `hibernate_sequence`) | `IDENTITY` on `consumers.id AUTO_INCREMENT` |
| `Order`      | `IDENTITY` on `orders.id`  | unchanged           |
| `Courier`    | `IDENTITY` on `courier.id` | unchanged           |
| `Restaurant` | `IDENTITY` on `restaurants.id` | unchanged       |

Only the `Consumer` entity requires a Java change; this happens in the
consumer-service extraction ticket. Until that change lands, the
consumer-service must not write to the V1 schema in production — the
V1 migration is deployable, but the entity annotation must flip in the
same release.

---

## 5. Rollback and Forward-Fix Strategy

Flyway migrations in this repo are **forward-only**. We explicitly do
**not** rely on Flyway's `U__` undo migrations — they require Flyway
Teams (paid) and do not cover data drift after the failed migration
anyway. Instead:

### 5.1 Principles

1. **Never edit a migration that has been applied in any environment.**
   A migration that has run is part of the history. Changes go in a
   new `V(n+1)__` file.
2. **Every destructive change ships as two migrations**: an expanding
   change and a contracting change (the expand–contract / parallel
   change pattern). The code between the two releases reads old +
   new, writes both.
3. **Every DDL migration is preceded by a backup of the affected
   tables** in non-prod environments; production DDL runs from the
   application startup path and relies on point-in-time recovery of
   the MySQL instance (RDS/Aurora snapshot, binlog replay).
4. **`flyway baseline`** is used exactly once per environment — when
   the new per-service schema is bootstrapped on top of an existing
   monolith DB (see §7).

### 5.2 Failure playbook

| Failure mode                                         | Remediation                                                                                       |
|------------------------------------------------------|----------------------------------------------------------------------------------------------------|
| Migration fails mid-script (DDL aborts)              | MySQL auto-rolls back the DDL; Flyway marks the migration `FAILED`. Re-deploy fixed `V(n)` after `flyway repair`. |
| Migration succeeds but app is broken by schema change | Ship a **forward-fix** migration (`V(n+1)__revert_...sql`) that undoes the change. Re-deploy.     |
| Data corrupted by migration                          | Restore from MySQL backup (point-in-time), re-apply Flyway up to the last known-good version.     |
| Migration applied to wrong schema                    | Stop — do not run more migrations. Page the platform on-call; restore from backup.               |

### 5.3 Release sequencing

- **Schema migration first, then code**: when expanding (adding a
  column, adding a table), deploy the migration, wait for it to apply
  cluster-wide, then deploy the code that reads/writes the new
  structure.
- **Code first, then schema contraction**: when contracting (dropping
  a column, dropping a table), roll out a code version that no longer
  touches the old structure, wait one full release cycle, then deploy
  the dropping migration.

---

## 6. Data Synchronization Between Services

After the split, each service sees only its own tables. Cross-service
data needs (e.g., the order service rendering a restaurant name and
menu item price on an order summary) are served through three
complementary mechanisms:

### 6.1 Domain events (primary)

Services publish state-change events to Kafka topics keyed by
aggregate ID. Consumers materialize local read-models inside their
own schema.

| Topic                     | Publisher              | Example events                                         |
|---------------------------|------------------------|--------------------------------------------------------|
| `ftgo.consumer.events`    | consumer-service       | `ConsumerCreated`                                      |
| `ftgo.order.events`       | order-service          | `OrderCreated`, `OrderAccepted`, `OrderDelivered`, `OrderCancelled`, `OrderRevised` |
| `ftgo.restaurant.events`  | restaurant-service     | `RestaurantCreated`, `RestaurantMenuRevised`           |
| `ftgo.courier.events`     | courier-service        | `CourierCreated`, `CourierAvailabilityChanged`         |

Event publication uses the **transactional outbox** pattern (outbox
table co-located with the aggregate, drained by a Debezium connector
on the MySQL binlog). This keeps the state change and the event
emission atomic even without an XA transaction.

### 6.2 Change Data Capture (bootstrap + safety net)

- **Bootstrap**: seeding new services from the monolith DB (§7) uses
  Debezium MySQL source connectors snapshotting each legacy table into
  the new schema-per-service Kafka topics.
- **Ongoing safety net**: CDC topics are retained as a compacted log
  so a late-joining consumer can rebuild its read-model from scratch
  without re-publishing from the source service.

### 6.3 Synchronous REST (last resort)

Where a service needs authoritative, read-after-write data from a
peer (e.g., validating an order against the current restaurant menu
at creation time), it calls the peer's REST API synchronously. These
calls are mandatory circuit-broken and retried via the already-wired
Resilience4j instances (see each service's `application.yml` under
`resilience4j.*`). REST is never the primary consistency mechanism —
it serves as a tie-breaker when the event stream may be lagging.

### 6.4 Consistency guarantees

- **Within a service**: strong consistency (single MySQL schema).
- **Across services**: eventual consistency, bounded by event lag
  (target < 1s p95 in steady state).
- **Idempotency**: every consumer processes events keyed by event ID
  and aggregate version; duplicate delivery is a no-op.
- **Ordering**: per-aggregate ordering guaranteed by partitioning the
  topic on aggregate ID.

---

## 7. Bootstrapping Data from the Monolith

The V1 migrations create empty tables. Existing monolith data must be
split into the four new schemas in a one-time "cutover" bootstrap:

1. **Stop writes to the monolith** (maintenance window or read-only
   flip on the monolith `FtgoApplicationMain`).
2. For each new schema, run `flyway migrate` against an empty DB. The
   V1 migration creates the tables.
3. Run the cutover pipeline (one Debezium snapshot job per bounded
   context) to copy rows from `ftgo.<table>` into
   `ftgo_<context>.<table>`:
   - `ftgo.consumers`               → `ftgo_consumer.consumers`
   - `ftgo.orders`                  → `ftgo_order.orders`
   - `ftgo.order_line_items`        → `ftgo_order.order_line_items`
   - `ftgo.restaurants`             → `ftgo_restaurant.restaurants`
   - `ftgo.restaurant_menu_items`   → `ftgo_restaurant.restaurant_menu_items`
   - `ftgo.courier`                 → `ftgo_courier.courier`
   - `ftgo.courier_actions`         → `ftgo_courier.courier_actions`
4. After the copy completes, reseed each table's `AUTO_INCREMENT`
   counter to `MAX(id) + 1` so no new row collides with a bootstrapped
   row (`ALTER TABLE <t> AUTO_INCREMENT = <n>`).
5. Start the four microservices pointed at the new schemas. Start the
   outbox/CDC connectors (§6.1).
6. Stop the monolith. Keep the `ftgo` schema read-only as a rollback
   snapshot for one full release cycle, then drop it.

For non-prod environments (CI, local dev), step 3 is replaced by a
seed script that inserts a canned dataset directly into each per-service
schema — the monolith is not involved.

---

## 8. Operational Checklist

Before merging a new migration:

- [ ] File named `V<n>__<snake_case>.sql`, lives under the correct
      service's `db/migration/` folder.
- [ ] Uses the expand–contract pattern if destructive.
- [ ] Does not reference another service's schema.
- [ ] Does not use cross-schema foreign keys.
- [ ] Adds indexes for any column that used to be covered by a dropped FK.
- [ ] SQL runs clean on a fresh MySQL 8.0 instance (`./gradlew
      :services:<x>-service:bootRun` or the per-service Docker Compose
      stack).
- [ ] CODEOWNERS review from the service's owning team.

---

## 9. Open Questions / Future Work

- **Sharded / multi-instance MySQL**: current plan places all four
  schemas on the same instance for simplicity. Moving a given schema
  to its own instance is a configuration change (per-service
  `spring.datasource.url`) and requires no migration changes.
- **Schema registry for events**: event payloads in §6 are
  Avro/Protobuf-serializable but no registry is wired up today. Track
  under EM-42.
- **Long-tail ID collisions during bootstrap**: §7 step 4 guards
  against future collisions; existing IDs remain unique because the
  monolith already used globally-unique numeric PKs.
