# consumer-service Flyway migrations

This directory owns every schema change to the **`ftgo_consumer`**
MySQL database. Flyway runs these scripts automatically on Spring
Boot startup (`spring.flyway.enabled=true`,
`spring.flyway.locations=classpath:db/migration` — see
`services/consumer-service/config/application.yml`).

## Conventions

- File names: `V<n>__<snake_case_description>.sql`
  (see [CONVENTIONS.md §4](../../../../../../../CONVENTIONS.md)).
- Versions are **per-service** and do not coordinate with other
  services' migration numbers.
- Migrations are forward-only. Destructive changes ship as expand →
  contract, never in-place edits to an applied migration.
- No cross-schema references.

See [`docs/database-migration-strategy.md`](../../../../../../../docs/database-migration-strategy.md)
for the full strategy.
