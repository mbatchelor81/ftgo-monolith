# ftgo-flyway (legacy)

This module owns the single shared Flyway migration set for the
monolithic `ftgo` MySQL database. It is **being retired** as part of
the microservices migration (EM-29).

Each bounded context now owns its own migration history:

- [`services/consumer-service/src/main/resources/db/migration/`](../services/consumer-service/src/main/resources/db/migration/)
- [`services/order-service/src/main/resources/db/migration/`](../services/order-service/src/main/resources/db/migration/)
- [`services/restaurant-service/src/main/resources/db/migration/`](../services/restaurant-service/src/main/resources/db/migration/)
- [`services/courier-service/src/main/resources/db/migration/`](../services/courier-service/src/main/resources/db/migration/)

See [`docs/database-migration-strategy.md`](../docs/database-migration-strategy.md)
and [ADR-0002](../docs/adr/0002-database-per-service.md) for the
target topology and cutover plan.

## Deprecation timeline

1. **While the monolith still deploys** (`ftgo-application` is live),
   `V1__create_ftgo_db.sql` remains authoritative. Do **not** add new
   migrations here — ship them in the owning service's
   `db/migration/` folder instead.
2. Once every bounded context's service has taken over writes to its
   own `ftgo_<context>` schema, the legacy `ftgo` schema is flipped
   read-only and kept as a rollback snapshot for one full release
   cycle (see the strategy doc §7).
3. After the rollback window closes, this module and the `ftgo`
   schema are both deleted.

No new changes should land in `src/main/resources/db/migration/` of
this module.
