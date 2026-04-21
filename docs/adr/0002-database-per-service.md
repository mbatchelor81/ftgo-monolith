# ADR-0002: Database-per-Service Topology

- **Status**: Accepted
- **Date**: 2026-04-21
- **Deciders**: FTGO Platform / Architecture team
- **Related**: EM-29 (this ADR), ADR-0001, EM-3x per-service extraction tickets

## Context

The FTGO monolith ships today with a single MySQL database named
`ftgo`. Every JPA entity (`Consumer`, `Order`, `Restaurant`, `Courier`,
plus the embedded collections `order_line_items`, `restaurant_menu_items`,
`courier_actions`) lives in that one schema. A single Flyway module
(`ftgo-flyway/`) owns the migration history.

Three cross-service foreign keys exist in the current schema
(`orders.restaurant_id`, `orders.assigned_courier_id`,
`courier_actions.order_id`) and every entity shares a single
`hibernate_sequence` row for ID allocation.

The microservices migration (tracked under EM-28 … EM-49) requires
each service to be independently deployable, independently scalable,
and independently recoverable. A shared database violates all three
properties: a bad DDL migration owned by one team can break every
other service's deploy; JPA schema drift in one entity forces a
coordinated rebuild of every service's image; and a single failure
domain (the MySQL instance) becomes the entire platform's availability
ceiling.

Forces in play:

1. **Deployment independence** — each service must be able to evolve
   its schema on its own cadence.
2. **Strong intra-context consistency** — within a bounded context
   (e.g. an order and its line items) we still want FK-enforced
   referential integrity.
3. **Tolerable cross-context latency** — users accept that
   "the restaurant name on my order summary" may lag a menu rename
   by a second.
4. **Incremental cutover** — we cannot stop-the-world; the monolith
   must keep running while each service's DB carves out.
5. **Tool reuse** — Flyway, MySQL, and the existing Spring Boot
   datasource plumbing are known and working.

## Decision

We adopt a **database-per-service** topology. Each bounded context
owns exactly one MySQL schema named `ftgo_<context>`
(`ftgo_consumer`, `ftgo_order`, `ftgo_restaurant`, `ftgo_courier`),
with its own Flyway migration history table, its own ID generation
strategy, and its own datasource user. Cross-service references are
stored as plain `BIGINT` columns without database-level foreign keys;
cross-service reads go through domain events or REST APIs.

The physical topology is a policy decision orthogonal to this ADR —
co-locating all four schemas on one MySQL instance is fine today and
splitting to per-service instances later is a configuration change
with no migration impact.

The mechanics (migration layout, naming, bootstrap, rollback, data
sync) are specified in
[`docs/database-migration-strategy.md`](../database-migration-strategy.md).

## Consequences

### Positive

- **Deployment independence.** A failing migration in order-service
  no longer blocks courier-service's release train.
- **Smaller blast radius per DDL change.** A column drop locks one
  schema, not the whole platform.
- **Clear ownership.** Each team owns its schema end-to-end and can
  tune indexes, engine parameters, and collation for its workload.
- **Path to per-service tuning.** A service with high write volume
  (order-service) can be moved to its own MySQL replica set without
  migrating any other team.

### Negative

- **Lost referential integrity across contexts.** The cross-service
  FKs are replaced by application-level conventions — stale `order_id`
  values on a `courier_actions` row are now possible if the owning
  order is hard-deleted without a compensating event. Mitigated by
  (a) soft deletes in the order aggregate, (b) events consumed by
  the courier service that mark the associated actions as orphaned.
- **Operational complexity.** Four Flyway histories, four datasource
  users, four backup/restore runbooks instead of one. Tooling
  (docker-compose, k8s configmaps, env vars) is updated once; per-service
  migrations stay simple.
- **Eventual consistency for cross-context reads.** Sub-second
  domain-event lag is still visible in the UI for cross-service
  projections. Acceptable for FTGO's workload; explicitly called out
  in the UX.

### Neutral

- Existing JPA entities need to drop `@ManyToOne` associations
  across context boundaries and replace them with `Long` ID fields.
  This is a code change owned by each per-service extraction ticket
  (EM-3x), not by EM-29.

## Alternatives Considered

### A. Single shared schema, service-level "logical ownership"

- **Pros**: Zero migration cost; FKs keep working; single backup.
- **Cons**: The whole point of decomposition is lost — every team
  still coordinates on one migration history, one MySQL failure
  domain, one ID sequence. Rejected: this is the status quo we want
  to leave.

### B. Schema-per-service on one MySQL instance **with** cross-schema FKs

- **Pros**: Still get MySQL-enforced referential integrity.
- **Cons**: Cross-schema FKs require cross-schema privileges on the
  datasource users, which leaks ownership: the order-service's user
  has to `REFERENCES` on `ftgo_restaurant.restaurants`. Also
  permanently couples the two schemas to the same MySQL instance —
  we can never move one to its own database. Rejected.

### C. Database-per-service with global IDs (UUID or snowflake)

- **Pros**: IDs are globally unique, trivially mergeable across
  schemas or databases; CDC/event payloads carry IDs that never
  collide.
- **Cons**: Invalidates every existing numeric primary key and
  requires re-keying the entire monolith dataset during cutover. Not
  justified today; the decision can be revisited as a separate
  ticket (§4.2 of the strategy doc).

### D. Polyglot persistence (e.g., Postgres for order, DynamoDB for courier)

- **Pros**: Right tool for each workload.
- **Cons**: Multiplies operational surface area (four Flyway runbooks
  would become "Flyway, Liquibase, Alembic, DynamoDB migrations…").
  Deferred to individual service tickets if a specific workload
  outgrows MySQL.

## Follow-up

- **EM-29 (this ticket)**: scaffold the per-service Flyway migrations
  and document the strategy (this ADR + strategy doc).
- **Per-service extraction tickets (EM-3x)**: flip JPA entities from
  `@ManyToOne` cross-context associations to plain `Long` IDs; flip
  `Consumer.id` generation to `IDENTITY`; wire each service's
  datasource to its own schema.
- **Platform ticket**: provision `ftgo_consumer`, `ftgo_order`,
  `ftgo_restaurant`, `ftgo_courier` schemas and per-schema datasource
  users in every environment; add them to the MySQL backup rotation.
- **Cutover ticket**: Debezium-based one-time split of the existing
  `ftgo` dataset into the four new schemas (strategy doc §7).
- **Post-cutover**: retire `ftgo-flyway/` and the `ftgo` schema; drop
  the legacy monolith from `FtgoApplicationMain`.
