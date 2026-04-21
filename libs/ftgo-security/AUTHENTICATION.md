# FTGO Authentication (EM-40)

This document describes the JWT-based authentication wired into every FTGO
microservice via `libs/ftgo-security`. It is the authoritative reference for
the authentication flow, token contents, token lifecycle, service
configuration, and testing strategy.

## 1. Design at a Glance

FTGO authenticates API requests with **short-lived access tokens** carried
in the `Authorization: Bearer …` header. Tokens are **stateless** — each
service validates them locally with a shared HMAC secret; there is no
central session store, no call-out to an auth server on the hot path, and
no shared database lookup. A **refresh token** with a longer TTL can be
exchanged for a new access + refresh pair without re-presenting user
credentials.

```
  ┌──────────────┐   (1) credentials     ┌─────────────────┐
  │   Client     │──────────────────────▶│  Auth provider  │
  │  (browser /  │◀──────────────────────│  (issues tokens │
  │   mobile /   │   (2) access+refresh  │   via           │
  │   service)   │                       │   JwtTokenService)│
  └──────┬───────┘                       └─────────────────┘
         │  (3) Authorization: Bearer <access>
         ▼
  ┌──────────────────┐  validates signature / iss / aud / exp / token_type
  │ FTGO microservice │─────────────────────────────────────────┐
  │ (Resource Server) │                                         │
  └──────┬───────────┘                                          ▼
         │  SecurityContext populated:                ┌──────────────────┐
         │    userId, username, ROLE_*, PERM_*        │  Domain handler  │
         └───────────────────────────────────────────▶│  (@RestController)│
                                                     └──────────────────┘
```

Key properties:

- **Stateless**: no server-side session; every request is authenticated from
  the token alone.
- **Service-local validation**: each pod validates tokens with a shared
  HMAC secret — no network hop required.
- **Principle of least privilege**: refresh tokens are rejected at the
  resource-server boundary so only access tokens can authenticate API
  calls.
- **Zero regression for legacy clients**: when `ftgo.security.jwt.secret`
  is absent the HTTP Basic path from the Spring Security baseline remains
  intact.

## 2. Token Claims

| Claim          | Type         | Set on access | Set on refresh | Notes                                 |
|----------------|--------------|:-------------:|:--------------:|---------------------------------------|
| `iss`          | string       |      ✓        |       ✓        | `ftgo.security.jwt.issuer`            |
| `aud`          | string[]     |      ✓        |       ✓        | `ftgo.security.jwt.audience`          |
| `iat`          | timestamp    |      ✓        |       ✓        | issued-at                             |
| `exp`          | timestamp    |      ✓        |       ✓        | `iat + access-token-ttl` / refresh-ttl|
| `jti`          | UUID string  |      ✓        |       ✓        | token id (useful for future revocation)|
| `sub`          | string       |      ✓        |       ✓        | FTGO user id                           |
| `token_type`   | `access`/`refresh` | ✓       |       ✓        | resource server rejects `refresh`     |
| `user_id`      | string       |      ✓        |       ✓        | duplicate of `sub` for convenience    |
| `username`     | string       |      ✓        |       ✓        | human-readable username               |
| `roles`        | string[]     |      ✓        |       —        | mapped to `ROLE_*` authorities        |
| `permissions`  | string[]     |      ✓        |       —        | mapped to `PERM_*` authorities        |

Refresh tokens deliberately **omit** `roles` and `permissions` — they
encode just enough identity to mint a fresh pair, not enough to authorize
an API call.

## 3. Token Lifecycle

```
             ┌───────────────────────────────────────────────┐
 login  ─▶   │ issueTokens(userId, username, roles, perms)   │
             └───────────────────────────────────────────────┘
                          │                 │
               access (15m)               refresh (7d)
                          │                 │
                          ▼                 ▼
                ┌──────────────┐   ┌─────────────────────┐
 API call  ─▶   │ Bearer auth  │   │ (held by client     │
                │ SUCCESS ✓    │   │  for later refresh) │
                └──────────────┘   └─────────────────────┘
                          │
                   exp elapses
                          │
                          ▼
                ┌──────────────┐
 API call  ─▶   │ Bearer auth  │───▶ 401 Unauthorized
                │ FAILS  ✗     │
                └──────────────┘
                                        │
                                        ▼
                             ┌──────────────────────────┐
 refresh   ─▶                │ refresh(refreshToken,    │
                             │         roles, perms)    │
                             └──────────────────────────┘
                                        │
                                        ▼
                               fresh access + refresh
```

- **Access tokens** default to **15 minutes** (`access-token-ttl: PT15M`).
- **Refresh tokens** default to **7 days** (`refresh-token-ttl: P7D`).
- **Clock skew tolerance** defaults to **30 seconds** to accommodate mild
  clock drift between the token issuer and resource servers.
- **Refresh rotation**: `JwtTokenService.refresh` issues a *new* access +
  refresh pair each call. The caller is expected to discard the old
  refresh token — a revocation list is a future enhancement.
- **Expiration**: once `exp` has passed (plus skew), the resource server
  returns `401 Unauthorized`; the client must refresh or re-authenticate.

## 4. Service Configuration

Every FTGO service enables JWT by setting a secret, either via
`application.yml` or the `FTGO_JWT_SECRET` environment variable:

```yaml
# services/<name>/config/application.yml
ftgo:
  security:
    jwt:
      secret: ${FTGO_JWT_SECRET}        # minimum 32 bytes (HS256)
      issuer: ftgo-auth
      audience: ftgo-services
      access-token-ttl: PT15M
      refresh-token-ttl: P7D
      clock-skew: PT30S
```

The secret is bound by `JwtProperties` (`prefix = ftgo.security.jwt`).
`JwtAuthenticationConfiguration` is gated behind
`@ConditionalOnProperty(prefix = "ftgo.security.jwt", name = "secret")`:

- **With a secret** → HS256 `JwtEncoder` / `JwtDecoder` /
  `FtgoJwtAuthenticationConverter` / `JwtTokenService` beans are wired
  and the OAuth2 Resource Server filter is attached to the API chain.
- **Without a secret** → only the pre-existing HTTP Basic baseline from
  `BaseSecurityConfiguration` is active. This keeps the library
  backward-compatible for early-migration phases.

Spring Security configuration lives in `BaseSecurityConfiguration`:

- `actuatorSecurityFilterChain` (`@Order(1)`) — `/actuator/health(/*)`
  and `/actuator/info` are public; every other actuator endpoint requires
  auth.
- `apiSecurityFilterChain` (`@Order(2)`) — everything else requires auth;
  HTTP Basic is kept as a fallback, OAuth2 Resource Server (JWT) is
  attached via `BaseSecurityConfiguration.configureJwt(...)` when a
  `JwtDecoder` is present.
- Sessions are `STATELESS`; CSRF is disabled because the API is token-
  or Basic-authenticated, never cookie-authenticated.
- `SecurityExceptionHandler` returns a JSON body for both 401 and 403.

## 5. Claim → Authority Mapping

`FtgoJwtAuthenticationConverter` turns the JWT into a
`JwtAuthenticationToken` whose principal is an `FtgoJwtPrincipal` and
whose authorities are:

| Claim          | Authority prefix |
|----------------|------------------|
| `roles`        | `ROLE_<value>`   |
| `permissions`  | `PERM_<value>`   |

Controllers and services can therefore use familiar Spring Security
expressions:

```java
@PreAuthorize("hasRole('CONSUMER') and hasAuthority('PERM_order:write')")
```

`SecurityUtils` exposes convenience lookups populated for every request:

```java
Optional<String>     userId       = SecurityUtils.getCurrentUserId();
Optional<String>     username     = SecurityUtils.getCurrentUsername();
Collection<String>   roles        = SecurityUtils.getCurrentRoles();
Collection<String>   permissions  = SecurityUtils.getCurrentPermissions();
```

## 6. Error Semantics

| Situation                                    | HTTP | Body field `error`        |
|----------------------------------------------|------|---------------------------|
| Missing `Authorization` header               | 401  | `Unauthorized`            |
| Malformed JWT                                | 401  | `Unauthorized`            |
| Signature mismatch (wrong key / tampered)    | 401  | `Unauthorized`            |
| Expired token (`exp` < now − skew)           | 401  | `Unauthorized`            |
| Wrong `iss` or `aud`                         | 401  | `Unauthorized`            |
| Refresh token presented to API endpoint      | 401  | `Unauthorized`            |
| Authenticated but insufficient authority     | 403  | `Forbidden`               |

All 401/403 responses are JSON (`application/json`) via
`SecurityExceptionHandler` — never HTML error pages.

## 7. Issuing Tokens

`JwtTokenService` is the single entry point for minting tokens. It does
**not** authenticate users — the caller (e.g. a future login controller)
is expected to have already verified credentials.

```java
TokenPair pair = jwtTokenService.issueTokens(
        "user-42", "alice",
        List.of("CONSUMER"),
        List.of("order:read", "order:write"));

// Rotate a refresh token for a new pair (e.g. on /auth/refresh):
TokenPair rotated = jwtTokenService.refresh(
        pair.refreshToken().value(),
        List.of("CONSUMER"),
        List.of("order:read", "order:write"));
```

`refresh` re-decodes the supplied token, verifies `token_type == refresh`,
and re-mints both halves. Callers should discard the old refresh token.

## 8. Testing

The library ships three layers of tests (see
`libs/ftgo-security/src/test/java/.../jwt/`):

- **`JwtTokenServiceTest`** — unit tests for encoder/decoder wiring, TTLs,
  refresh happy path, tamper detection, and rejecting access tokens sent
  to `refresh()`.
- **`FtgoJwtAuthenticationConverterTest`** — role/permission authority
  mapping, username fallback, and refresh-token authority stripping.
- **`JwtResourceServerIntegrationTest`** — end-to-end MockMvc test against
  a minimal Spring Boot app. Covers: valid access token, missing token,
  malformed token, expired token, wrong issuer, wrong audience, tampered
  signature, and refresh-token-sent-to-API.

Total: **23 tests** — all passing.

## 9. Security Notes

- **HS256 (HMAC-SHA256)** is used for signing. The shared secret must be
  **≥ 32 bytes**; `JwtAuthenticationConfiguration` fails fast on shorter
  secrets.
- **Secrets are never hard-coded**. Production injects `FTGO_JWT_SECRET`
  via environment variable / Kubernetes secret. Test YAMLs contain only
  deterministic non-production values clearly labelled as CI-only.
- **No clear-text logging of tokens** — `FtgoJwtPrincipal` is the only
  exposure surface and it does not contain the raw JWT.
- **Refresh-token theft mitigation**: short access-token TTL limits the
  window of abuse for a leaked access token; refresh-token rotation
  (reissue on every `refresh()`) is implemented — revocation list is a
  future enhancement.

## 10. Future Work (explicitly out of scope for EM-40)

- Login / user-credential verification service that *calls* `JwtTokenService`.
- Refresh-token revocation / replay detection.
- Rotating signing keys (JWK set with `kid`).
- Asymmetric signing (RS256) so a dedicated auth service holds the private
  key and every resource server only needs the public key.
