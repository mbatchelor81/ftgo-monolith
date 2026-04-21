# courier-service Flyway migrations

This directory owns every schema change to the **`ftgo_courier`**
MySQL database. Flyway runs these scripts automatically on Spring
Boot startup (`spring.flyway.enabled=true`,
`spring.flyway.locations=classpath:db/migration` — see
`services/courier-service/config/application.yml`).

## Conventions

- File names: `V<n>__<snake_case_description>.sql`
  (see [CONVENTIONS.md §4](../../../../../../../CONVENTIONS.md)).
- Versions are **per-service** and do not coordinate with other
  services' migration numbers.
- Migrations are forward-only. Destructive changes ship as expand →
  contract, never in-place edits to an applied migration.
- `courier_actions.order_id` is a plain `BIGINT` — **no cross-schema
  foreign key**. Cross-service reads go through events or REST APIs.

See [`docs/database-migration-strategy.md`](../../../../../../../docs/database-migration-strategy.md)
for the full strategy.
