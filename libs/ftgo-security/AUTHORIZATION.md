# FTGO Role-Based Access Control (RBAC)

> Source-of-truth for the FTGO authorization model delivered as part of
> [EM-37](https://ftgo.atlassian.net/browse/EM-37). If you add a new role,
> permission, or endpoint, update this document in the same PR.

FTGO's RBAC model layers two orthogonal concepts on top of the JWT
authentication baseline from [EM-40](https://ftgo.atlassian.net/browse/EM-40):

1. **Roles** — coarse, human-friendly labels (`CUSTOMER`, `RESTAURANT_OWNER`,
   `COURIER`, `ADMIN`) that describe *who* the caller is. Roles form a
   hierarchy (`ADMIN` implies everything, etc.) so most endpoints can be
   gated by a single `hasRole(...)` check.
2. **Permissions** — fine-grained verbs scoped to a bounded context
   (`order:read`, `restaurant:menu:manage`, …) that describe *what* the
   caller is allowed to do. Permissions give the auth service room to
   grant a sub-set of a role's capabilities to a specific user without
   minting a new role.

Both roles and permissions travel in the access token as JWT claims and
are converted into Spring Security `GrantedAuthority` instances by
[`FtgoJwtAuthenticationConverter`](src/main/java/net/chrisrichardson/ftgo/security/jwt/FtgoJwtAuthenticationConverter.java).

## Runtime pieces

| Component | Location | Responsibility |
| :--- | :--- | :--- |
| `Role` enum | `authorization/Role.java` | Canonical list of roles + JWT / authority helpers. |
| `Permissions` constants | `authorization/Permissions.java` | Permission identifier catalog. |
| `MethodSecurityConfiguration` | `authorization/MethodSecurityConfiguration.java` | Enables `@EnableMethodSecurity`, wires the role hierarchy + permission evaluator into the expression handler. |
| `ResourceOwnershipPermissionEvaluator` | `authorization/ResourceOwnershipPermissionEvaluator.java` | Implements `hasPermission(target, 'own')` for per-user resource access. |
| `OwnedResource` interface | `authorization/OwnedResource.java` | Marker that entities / DTOs implement so the evaluator can look up their owner. |
| `FtgoSecurityAutoConfiguration` | `FtgoSecurityAutoConfiguration.java` | Imports `MethodSecurityConfiguration` for every consumer service. |

Services that declare `implementation project(':libs:ftgo-security')`
inherit the full stack — `@PreAuthorize` / `@PostAuthorize` annotations
work out of the box with no additional wiring.

## JWT claim shape

```jsonc
{
  "iss":          "ftgo-auth",
  "aud":          ["ftgo-services"],
  "sub":          "user-42",
  "user_id":      "user-42",
  "username":     "alice",
  "token_type":   "access",
  "roles":        ["RESTAURANT_OWNER"],
  "permissions":  ["restaurant:read", "restaurant:menu:manage", "order:fulfill"]
}
```

`Role` names are written verbatim; the converter prefixes them with
`ROLE_` before handing them to Spring Security. Permissions are
prefixed with `PERM_`, e.g. `order:admin` → `PERM_order:admin`.

## Role hierarchy

```
            ROLE_ADMIN
            /        \
ROLE_RESTAURANT_OWNER  ROLE_COURIER
            \        /
           ROLE_CUSTOMER
```

- `ADMIN` inherits every other role — hence the EM-37 acceptance
  criterion "ADMIN inherits all permissions" is satisfied without
  hand-listing permissions on every endpoint.
- `RESTAURANT_OWNER` and `COURIER` each inherit `CUSTOMER` because
  restaurant staff and couriers are still allowed to place orders as
  themselves.
- `CUSTOMER` is the baseline — it does not inherit any other role.

Defined centrally in
[`MethodSecurityConfiguration.ROLE_HIERARCHY`](src/main/java/net/chrisrichardson/ftgo/security/authorization/MethodSecurityConfiguration.java).

## Permission matrix

Which default permissions the auth service grants to each role. Specific
tokens may carry a subset (step-down) or superset (admin escalation) of
this list.

| Permission | CUSTOMER | RESTAURANT_OWNER | COURIER | ADMIN |
| :--- | :---: | :---: | :---: | :---: |
| `consumer:read`              | own | — | — | ✓ |
| `consumer:write`             | own | — | — | ✓ |
| `consumer:admin`             | — | — | — | ✓ |
| `order:read`                 | own | restaurant's orders | assigned deliveries | ✓ |
| `order:create`               | ✓ | — | — | ✓ |
| `order:cancel`               | own | — | — | ✓ |
| `order:revise`               | own | — | — | ✓ |
| `order:fulfill`              | — | ✓ | ✓ | ✓ |
| `order:admin`                | — | — | — | ✓ |
| `restaurant:read`            | ✓ | ✓ | ✓ | ✓ |
| `restaurant:write`           | — | own | — | ✓ |
| `restaurant:menu:manage`     | — | own | — | ✓ |
| `restaurant:admin`           | — | — | — | ✓ |
| `courier:read`               | — | — | own | ✓ |
| `courier:write`              | — | — | own | ✓ |
| `courier:availability:update`| — | — | own | ✓ |
| `courier:admin`              | — | — | — | ✓ |

**Legend:** `✓` = unrestricted, `own` = limited to the caller's own
resources (enforced by `ResourceOwnershipPermissionEvaluator`), `—` =
not granted.

## Applying the policy on an endpoint

```java
// Any authenticated caller — used for metadata endpoints like /api/v1/service-info.
@PreAuthorize("isAuthenticated()")

// Role check — the hierarchy lets ADMIN, RESTAURANT_OWNER, COURIER through as well.
@PreAuthorize("hasRole('CUSTOMER')")

// Permission check — the auth service decides which roles get this permission.
@PreAuthorize("hasAuthority('PERM_order:admin')")

// Resource ownership by target id (e.g. a consumer id passed as a path variable).
@PreAuthorize("hasPermission(#consumerId, 'consumer', 'own')")
ConsumerDto findById(String consumerId) { ... }

// Resource ownership by return value — the method returns an OwnedResource.
@PostAuthorize("hasPermission(returnObject, 'own')")
OrderDto getOrder(String id) { ... }
```

`hasPermission(..., 'own')` short-circuits to `true` for any caller
holding `ROLE_ADMIN`, so admin overrides never require extra wiring.
Anonymous or unauthenticated requests fall through to the standard
401 JSON body from
[`SecurityExceptionHandler`](src/main/java/net/chrisrichardson/ftgo/security/SecurityExceptionHandler.java);
authenticated callers who fail an authorization check receive the
403 body from the same handler.

## Extending the model

- **New role** — add an enum constant to `Role`, update the hierarchy
  literal in `MethodSecurityConfiguration.ROLE_HIERARCHY`, and update
  the matrix above.
- **New permission** — add a constant to `Permissions` using the
  `<bounded-context>:<verb>[:<sub-resource>]` convention, add a row to
  the matrix above, and update the auth service's role→permission
  seeder.
- **New ownership-gated resource** — make the entity / DTO implement
  `OwnedResource` and return the canonical `user_id` of the owner.
  Services can annotate the fetching method with
  `@PostAuthorize("hasPermission(returnObject, 'own')")` without any
  other plumbing.

## Test surface

- `RoleTest`, `RoleHierarchyTest`, `ResourceOwnershipPermissionEvaluatorTest`
  — unit tests covering the enum, hierarchy, and evaluator edge cases.
- `MethodSecurityIntegrationTest` — `@SpringBootTest` + `MockMvc`
  integration test that spins up the full auto-configuration and
  checks 200/401/403 outcomes across role, permission, and ownership
  gated endpoints. Covers the EM-37 acceptance criteria
  "Unauthorized access returns 403 Forbidden" and "Role hierarchy
  implemented (ADMIN inherits all permissions)".
