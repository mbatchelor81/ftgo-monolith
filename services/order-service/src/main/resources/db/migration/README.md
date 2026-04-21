# order-service Flyway migrations

This directory owns every schema change to the **`ftgo_order`**
MySQL database. Flyway runs these scripts automatically on Spring
Boot startup (`spring.flyway.enabled=true`,
`spring.flyway.locations=classpath:db/migration` — see
`services/order-service/config/application.yml`).

## Conventions

- File names: `V<n>__<snake_case_description>.sql`
  (see [CONVENTIONS.md §4](../../../../../../../CONVENTIONS.md)).
- Versions are **per-service** and do not coordinate with other
  services' migration numbers.
- Migrations are forward-only. Destructive changes ship as expand →
  contract, never in-place edits to an applied migration.
- `orders.consumer_id`, `orders.restaurant_id`, and
  `orders.assigned_courier_id` are plain `BIGINT` columns — **no
  cross-schema foreign keys**. Cross-service reads go through events
  or REST APIs.

See [`docs/database-migration-strategy.md`](../../../../../../../docs/database-migration-strategy.md)
for the full strategy.
