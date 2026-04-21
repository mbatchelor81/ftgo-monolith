# FTGO API Versioning Strategy

> **Status**: Accepted — EM-45
> **Companion**: [`rest-api-standards.md`](rest-api-standards.md)

## TL;DR

> **Every FTGO REST endpoint is versioned in the URL path as `/api/v{major}/…`**.
> Breaking changes require a new major version; additive changes ship within
> the existing one.

---

## 1. Goals

1. **Predictability** — clients can pin to a major version and trust that
   their integration will not break without a formal deprecation cycle.
2. **Co-existence** — the old and new versions of an endpoint **MUST** be
   servable side-by-side during the deprecation window.
3. **Low ceremony** — versioning decisions should be obvious from the URL, so
   reviewers and external consumers alike can tell "this is v1" at a glance.

## 2. Versioning scheme

| Component     | Format                  | Example               |
|---------------|-------------------------|-----------------------|
| Path prefix   | `/api/v{major}/…`       | `/api/v1/orders/{id}` |
| OpenAPI `info.version` | [semver][semver] string | `1.4.0`         |

- **Major** → incremented on any breaking change (removing a field, changing
  a field's type, changing the status code semantics of an endpoint,
  renaming a path segment).
- **Minor** → incremented on additive changes (new optional request fields,
  new endpoints, new enum values).
- **Patch** → incremented for documentation-only or wire-compatible fixes.

The HTTP surface exposes only the **major** number in the URL. Minor and
patch numbers live in the OpenAPI document's `info.version` field so
tooling can reason about them.

[semver]: https://semver.org/

## 3. Why path-based versioning?

We evaluated four approaches:

| Approach               | Verdict | Reason                                                      |
|------------------------|---------|-------------------------------------------------------------|
| Path: `/api/v1/orders` | **Chosen**  | Obvious from curl/logs; trivial to route in Spring MVC; Swagger UI renders it naturally. |
| Header: `Accept: application/vnd.ftgo.v1+json` | Rejected | Easy to omit; hostile for ad-hoc testing; awkward in CDN cache keys. |
| Query param: `?version=1` | Rejected | Clutters every URL; poor semantic fit for "version of the API shape". |
| Subdomain: `v1.api.ftgo.example.com` | Rejected | Requires DNS + TLS ops per version; overkill for an internal platform. |

## 4. Rules for services

1. Every new or migrated controller **MUST** mount under `/api/v{n}/…`:

   ```java
   @RestController
   @RequestMapping("/api/v1/orders")
   public class OrderController { … }
   ```

2. The version segment is part of the **controller-level** `@RequestMapping`,
   not repeated on every method, so upgrades are a one-line change.
3. A service **MAY** host multiple major versions of the same resource by
   declaring two controllers:

   ```java
   @RestController @RequestMapping("/api/v1/orders") class OrderControllerV1 { … }
   @RestController @RequestMapping("/api/v2/orders") class OrderControllerV2 { … }
   ```

   The two controllers delegate to the same service layer where possible so
   business logic does not fork.
4. The SpringDoc `OpenAPI.info.version` — provided via the
   `ftgo.openapi.version` configuration property — tracks the **full semver
   string** of the highest version exposed by that service.

## 5. Deprecation policy

1. A version becomes **deprecated** the day its successor ships.
2. Deprecated endpoints continue to respond normally but **MUST** include a
   [`Deprecation` header][deprecation-header]:

   ```
   Deprecation: true
   Sunset: Wed, 01 Jul 2026 00:00:00 GMT
   Link: </api/v2/orders>; rel="successor-version"
   ```

3. A deprecated major version is supported for **at least 6 months** after
   its sunset date is announced.
4. The Swagger UI landing page links to both the deprecated and current
   versions so consumers can diff them.

[deprecation-header]: https://datatracker.ietf.org/doc/html/draft-ietf-httpapi-deprecation-header

## 6. What counts as a breaking change?

| Change                                                   | Breaking? |
|----------------------------------------------------------|:---------:|
| Remove a response field                                  |   Yes     |
| Rename a response field                                  |   Yes     |
| Change a response field's type (e.g. `int` → `string`)   |   Yes     |
| Tighten a validation rule on a request field             |   Yes     |
| Change an endpoint's success status code                 |   Yes     |
| Remove a supported enum value from a request             |   Yes     |
| Add a new **required** request field                     |   Yes     |
| Add a new response field                                 |    No     |
| Add a new **optional** request field                     |    No     |
| Add a new endpoint                                       |    No     |
| Add a new enum value in a response (clients must tolerate) | No, but document it prominently |
| Loosen a validation rule (wider `@Size`, more enum values in request) | No |

## 7. Legacy endpoints (pre-EM-45)

The legacy monolith exposes a number of unversioned endpoints
(`/orders/…`, `/consumers/…`, `/couriers/…`, `/restaurants/…`). Those
remain unversioned until each is migrated into the `services/*` layout.

When a legacy endpoint is migrated:

1. Replicate it under `/api/v1/…` in the new service.
2. Add an HTTP `308 Permanent Redirect` from the legacy path to the new one.
3. Leave the legacy path in place for one release cycle (≥ 3 months) with a
   `Deprecation: true` header, then remove it.

## 8. Enforcement

- CI runs a Spectral ruleset (added in a follow-up ticket) that fails if an
  `@RequestMapping` base path does not match `^/api/v[0-9]+(/|$)`.
- Swagger UI links are reviewed on every PR that adds or changes endpoints.
