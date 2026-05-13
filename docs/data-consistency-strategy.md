# Data Consistency and Synchronization Strategy

## Overview

Moving from a single shared database to database-per-service means that
cross-service data integrity can no longer be enforced by relational foreign
keys. This document defines how the FTGO platform maintains data consistency,
handles cross-service references, and synchronizes data across bounded
contexts after the database decomposition.

## Consistency Model

### Monolith (Current)

- **Strong consistency** via MySQL foreign keys and single-database
  transactions.
- A single `BEGIN … COMMIT` can span tables owned by different services.
- Referential integrity is enforced at the database level.

### Microservices (Target)

- **Eventual consistency** across service boundaries.
- Each service owns a single database and uses local ACID transactions.
- Cross-service operations are coordinated through domain events or sagas.
- Referential integrity between services is enforced at the application
  layer, not the database layer.

## Cross-Service Reference Strategy

When a service needs to reference an entity owned by another service, it
stores only the foreign entity's ID as a plain column (no FK constraint).

| Local Service | Column | References | Validation Approach |
|--------------|--------|-----------|-------------------|
| Order Service | `orders.consumer_id` | Consumer Service | Validate via Consumer API on order creation |
| Order Service | `orders.restaurant_id` | Restaurant Service | Validate via Restaurant API on order creation |
| Order Service | `orders.assigned_courier_id` | Courier Service | Validate via Courier API on courier assignment |
| Courier Service | `courier_actions.order_id` | Order Service | Validate via Order API on action scheduling |

### Handling Invalid References

If a referenced entity is deleted or becomes unavailable:

1. **Soft deletes**: Services should prefer soft deletes (e.g., a `deleted`
   flag or `status = INACTIVE`) over hard deletes to avoid dangling
   references.
2. **Graceful degradation**: If a cross-service lookup returns 404, the
   calling service should handle it gracefully (e.g., display
   "Consumer unavailable" instead of failing).
3. **Compensating events**: When an entity is retired, the owning service
   publishes a domain event (e.g., `ConsumerDeactivated`) so that
   dependent services can update their local state.

## Data Synchronization Approaches

Three patterns are available for cross-service data synchronization. The
recommended default is **Domain Events**.

### 1. Domain Events (Recommended)

Services publish events when their state changes. Other services subscribe
to relevant events and update their local read models or caches.

```
┌─────────────────┐    RestaurantCreated     ┌─────────────────┐
│   Restaurant    │ ──────────────────────►  │   Order         │
│   Service       │                          │   Service       │
│                 │    RestaurantMenuRevised  │                 │
│                 │ ──────────────────────►  │  (local cache   │
│                 │                          │   of menu data) │
└─────────────────┘                          └─────────────────┘
```

**Implementation options**:
- **Transactional outbox**: Write events to an `outbox` table in the same
  local transaction as the state change. A poller or CDC connector
  publishes them to the message broker.
- **Direct publishing**: Publish to a message broker (e.g., Apache Kafka,
  RabbitMQ) after the local transaction commits. Risk of lost events if
  the service crashes between commit and publish.

**Recommended broker**: Apache Kafka for durability, ordering guarantees,
and replay capability.

**Event schema conventions**:
- Events are named as past-tense domain facts: `OrderCreated`,
  `CourierAssigned`, `RestaurantMenuRevised`.
- Events include the aggregate ID, a timestamp, and the minimal set of
  changed fields.
- Events are versioned (e.g., `OrderCreated.v1`) to support schema
  evolution.

### 2. Change Data Capture (CDC)

A CDC connector (e.g., Debezium) monitors the MySQL binlog and publishes
row-level changes to Kafka topics. Consuming services process these
change events to maintain local projections.

**When to use**: Useful during the migration period when the monolith
still writes to the shared `ftgo` database and new services need to
receive updates without modifying monolith code.

**Tradeoffs**:
- (+) No changes to the producing service's code.
- (+) Captures all changes, including those made by direct SQL.
- (-) Exposes internal schema details to consumers (tight coupling).
- (-) Requires Kafka Connect infrastructure.

### 3. API Calls (Synchronous)

A service calls another service's REST API to fetch or validate data
on demand.

**When to use**: For infrequent, low-latency lookups where eventual
consistency is not acceptable (e.g., validating a consumer exists before
creating an order).

**Tradeoffs**:
- (+) Simple to implement — no additional infrastructure.
- (-) Creates runtime coupling (caller fails if callee is down).
- (-) Increases latency for each cross-service hop.

**Mitigation**: Use a circuit breaker (e.g., Resilience4j) and cache
results locally with a TTL.

## Saga Pattern for Distributed Transactions

Operations that span multiple services use the **Saga pattern** —
a sequence of local transactions coordinated by either choreography
(event-driven) or orchestration (central coordinator).

### Example: Create Order Saga

```
1. Order Service    → Create order (state: PENDING)
2. Consumer Service → Validate consumer exists
3. Restaurant Service → Validate menu items and prices
4. Order Service    → Approve order (state: APPROVED)
```

If step 2 or 3 fails, a **compensating transaction** is executed:

```
Order Service → Cancel order (state: CANCELLED)
```

### Saga Design Principles

1. Each step is a local ACID transaction within a single service database.
2. Compensating transactions undo the effect of prior steps on failure.
3. Sagas are idempotent — re-executing a step produces the same result.
4. Use correlation IDs to trace saga execution across services.

## Rollback Strategy

### Per-Service Flyway Rollback

Flyway versioned migrations are forward-only by design. To roll back a
schema change:

1. **Create a new forward migration** that reverses the change.
   Example: If `V3__add_email_column.sql` added a column, create
   `V4__remove_email_column.sql` to drop it.
2. **Never edit or delete** an already-applied migration file — this
   breaks Flyway's checksum validation.
3. **Use `flyway repair`** only to fix the `flyway_schema_history` table
   after a failed migration, never to skip migrations.

### Data Migration Rollback

During the dual-write migration period:

1. **Forward**: Monolith writes to both old (`ftgo`) and new
   (per-service) databases.
2. **Rollback**: Stop writing to the new database and revert to
   monolith-only writes. No data loss because the monolith database
   was always kept up to date.
3. **Validation**: Before cutover, run consistency checks comparing
   record counts and checksums between old and new databases.

### Full Rollback to Monolith

If the microservices deployment fails catastrophically:

1. Stop all microservice instances.
2. Re-deploy the monolith application (`ftgo-application`).
3. The monolith database (`ftgo`) is intact because it was maintained
   during the dual-write period.
4. No Flyway rollback is needed for the monolith — its migration history
   is unchanged.

### Rollback Decision Matrix

| Scenario | Action | Data Impact |
|----------|--------|-------------|
| Bad migration in one service | Deploy `V(n+1)` reverse migration | None — local to service |
| Service outage | Restart service; Flyway auto-repairs | None |
| Data inconsistency detected | Pause writes, run consistency check, replay events | Temporary inconsistency |
| Full microservices failure | Revert to monolith deployment | None — dual-write kept monolith DB current |

## Monitoring and Observability

### Consistency Checks

1. **Record count comparison**: Periodic job compares entity counts
   between the monolith database and per-service databases during
   dual-write.
2. **Event lag monitoring**: Track the delay between event publication
   and consumption. Alert if lag exceeds a configurable threshold.
3. **Dead letter queue**: Events that fail processing are routed to a
   DLQ for manual inspection.

### Metrics to Track

| Metric | Source | Alert Threshold |
|--------|--------|----------------|
| Event publication lag | Kafka consumer group lag | > 5 minutes |
| Cross-service API error rate | Circuit breaker metrics | > 5% |
| Saga failure rate | Saga orchestrator logs | > 1% |
| Schema migration failures | Flyway `flyway_schema_history` | Any `success = false` |

## Migration Timeline

| Phase | Duration | Activities |
|-------|----------|-----------|
| Phase 1 (current) | Sprint 1 | Define per-service schemas, create Flyway migrations |
| Phase 2 | Sprint 2-3 | Deploy per-service DBs, enable dual-write |
| Phase 3 | Sprint 4 | Cutover services to own databases |
| Phase 4 | Sprint 5 | Decommission monolith DB, remove dual-write code |
