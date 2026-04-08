# ftgo-security-lib

Shared Spring Security configuration library for FTGO microservices.

## Overview

Provides a consistent security baseline for all FTGO microservices:

- **SecurityFilterChain** — default authentication/authorization rules
- **JWT authentication** — stateless token-based auth for microservices
- **CORS configuration** — externalized via `ftgo.security.cors.*` properties
- **Actuator security** — health/info public, all others secured
- **CSRF disabled** — appropriate for stateless REST APIs
- **JSON error responses** — structured 401/403 responses
- **Security logging filter** — audit request/response metadata
- **Utility classes** — `SecurityContextUtils`, `RequestUtils`

## Usage

Add the dependency in your service's `build.gradle`:

```groovy
dependencies {
    implementation project(":shared:ftgo-security-lib")
}
```

The auto-configuration is picked up automatically via Spring Boot's
`AutoConfiguration.imports` mechanism.

## JWT Authentication

### Enabling JWT

Set `ftgo.security.jwt.enabled=true` and provide a signing secret:

```yaml
ftgo:
  security:
    jwt:
      enabled: true
      secret: ${FTGO_JWT_SECRET}   # min 32 chars, set via env var
      expiration: 1800             # access token TTL in seconds (30 min)
      refresh-expiration: 86400    # refresh token TTL in seconds (24 hours)
      issuer: ftgo-platform
```

### Authentication Flow

```
Client                         Service
  |                               |
  |-- POST /auth/login ---------->|  (credentials validated externally)
  |<-- { accessToken, refresh } --|  (JwtTokenProvider generates tokens)
  |                               |
  |-- GET /api/orders ----------->|  Authorization: Bearer <accessToken>
  |   JwtAuthenticationFilter     |  -> validates token signature + expiry
  |   extracts claims             |  -> sets SecurityContext with FtgoUserDetails
  |<-- 200 OK -------------------|
  |                               |
  |-- GET /api/orders ----------->|  Authorization: Bearer <expiredToken>
  |   JwtAuthenticationFilter     |  -> token expired
  |<-- 401 Unauthorized ----------|
  |                               |
  |-- POST /auth/refresh -------->|  (send refresh token)
  |   JwtTokenProvider.refresh    |  -> validates refresh token
  |<-- { new accessToken } -------|  -> issues new access token
```

### Token Structure

**Access Token Claims:**
- `sub` — user ID
- `iss` — issuer (e.g., `ftgo-platform`)
- `iat` — issued at timestamp
- `exp` — expiration timestamp
- `roles` — list of user roles (e.g., `["ROLE_ADMIN", "ROLE_USER"]`)
- `permissions` — list of fine-grained permissions (e.g., `["order:read"]`)
- `type` — `"access"`

**Refresh Token Claims:**
- `sub` — user ID
- `iss`, `iat`, `exp` — standard claims
- `type` — `"refresh"` (no roles/permissions)

### User Context in Service Layer

Access the authenticated user's details anywhere via `SecurityContextUtils`:

```java
// Get user ID
Optional<String> userId = SecurityContextUtils.getCurrentUserId();

// Get full user details (roles, permissions)
Optional<FtgoUserDetails> details = SecurityContextUtils.getCurrentUserDetails();

// Check specific role or permission
Set<String> roles = SecurityContextUtils.getCurrentRoles();
Set<String> permissions = SecurityContextUtils.getCurrentPermissions();
```

### Security Considerations

- **Signing secret**: Must be at least 256 bits (32 characters) for HMAC-SHA256
- **Never hardcode secrets**: Always use environment variables (`FTGO_JWT_SECRET`)
- **Token types enforced**: Only access tokens authenticate API requests; refresh tokens are rejected
- **Stateless**: No server-side session storage; each service validates tokens independently

## Configuration

See `application-security.yml` for default property values, or
`docs/security.md` for full documentation.
