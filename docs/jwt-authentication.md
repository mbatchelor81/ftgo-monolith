# FTGO Platform — JWT Authentication

## Overview

The FTGO platform uses JWT (JSON Web Token) based authentication via the OAuth2
Resource Server pattern. Each microservice validates incoming JWTs independently
using the public keys published by the identity provider (Keycloak).

This replaces the HTTP Basic authentication from the base `ftgo-security-lib`
with a stateless, scalable token-based approach suitable for a distributed
microservices architecture.

## Architecture

```
┌──────────┐     ┌──────────────┐     ┌──────────────────┐
│  Client  │────▶│  API Gateway │────▶│  Microservice    │
│  (SPA)   │     │  (optional)  │     │  (Resource Svr)  │
└──────────┘     └──────────────┘     └──────────────────┘
      │                                       │
      │  1. Authenticate                      │ 3. Validate JWT
      ▼                                       ▼
┌──────────────┐                    ┌──────────────────┐
│  Keycloak    │                    │  JWKS Endpoint   │
│  (IdP)       │───────────────────▶│  (Keycloak)      │
│              │  2. Issue JWT      └──────────────────┘
└──────────────┘
```

### Flow

1. **Client authenticates** with Keycloak using OAuth2 (Authorization Code
   flow, Direct Access Grants for dev/testing, or Client Credentials for
   service-to-service).
2. **Keycloak issues a JWT** containing the user's identity, roles, and
   permissions as claims.
3. **Client sends the JWT** in the `Authorization: Bearer <token>` header
   with each API request.
4. **Each microservice validates the JWT** independently by:
   - Fetching the signing keys from Keycloak's JWKS endpoint
   - Verifying the token signature (RS256)
   - Checking expiration, issuer, and (optionally) audience claims
5. **Claims are extracted** and mapped to Spring Security authorities
   (roles and permissions) by `FtgoJwtAuthenticationConverter`.

## Token Lifecycle

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Login     │───▶│ Access Token│───▶│  Expired?   │
│ (get tokens)│    │  (5 min)    │    │             │
└─────────────┘    └─────────────┘    └──────┬──────┘
                                        yes │    no │
                                            ▼      ▼
                                    ┌───────────┐  Use
                                    │  Refresh  │  token
                                    │  Token    │
                                    │ (30 min)  │
                                    └─────┬─────┘
                                          ▼
                                    New access token
```

| Token          | Default TTL | Purpose                              |
|----------------|-------------|--------------------------------------|
| Access token   | 5 minutes   | API authentication                   |
| Refresh token  | 30 minutes  | Obtain new access tokens             |
| SSO session    | 10 hours    | Browser session with Keycloak        |

## Configuration

### Enabling JWT Authentication

Add to a microservice's `application.yml`:

```yaml
ftgo:
  security:
    jwt:
      enabled: true
      issuer-uri: http://localhost:8180/realms/ftgo
      jwk-set-uri: http://localhost:8180/realms/ftgo/protocol/openid-connect/certs
```

When `ftgo.security.jwt.enabled=true`, the JWT filter chain replaces the
default HTTP Basic filter chain automatically.

### Full Configuration Reference

```yaml
ftgo:
  security:
    jwt:
      # Enable JWT-based authentication (default: false)
      enabled: true

      # OIDC issuer URI — used for token issuer validation
      issuer-uri: http://localhost:8180/realms/ftgo

      # JWKS URI — endpoint to fetch public signing keys
      jwk-set-uri: http://localhost:8180/realms/ftgo/protocol/openid-connect/certs

      # Claim path for role extraction (supports nested paths)
      roles-claim-name: realm_access.roles

      # Claim name for permission extraction
      permissions-claim-name: permissions

      # Claim name for user identity
      user-id-claim-name: sub

      # Prefix added to role names (default: ROLE_)
      role-prefix: ROLE_

      # Token refresh configuration
      token-refresh:
        enabled: false
        refresh-before-expiry-seconds: 60
        token-endpoint: http://localhost:8180/realms/ftgo/protocol/openid-connect/token
        client-id: ftgo-api
```

## Claims Mapping

### Keycloak Token Example

```json
{
  "sub": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "preferred_username": "consumer1",
  "email": "consumer1@ftgo.example",
  "realm_access": {
    "roles": ["ftgo-consumer"]
  },
  "permissions": ["order:create", "order:read"]
}
```

### Mapped Spring Security Authorities

| JWT Claim                         | Spring Authority      |
|-----------------------------------|-----------------------|
| `realm_access.roles: ftgo-consumer` | `ROLE_ftgo-consumer` |
| `realm_access.roles: ftgo-admin`    | `ROLE_ftgo-admin`    |
| `permissions: order:create`         | `order:create`       |

### FTGO Realm Roles

| Role              | Description                           |
|-------------------|---------------------------------------|
| `ftgo-consumer`   | Can place and manage orders           |
| `ftgo-restaurant` | Can accept and prepare orders         |
| `ftgo-courier`    | Can accept and deliver orders         |
| `ftgo-admin`      | Full platform access                  |

## Security Utilities

The `SecurityUtils` class provides JWT-aware helpers:

```java
// Get the current JWT token
Optional<Jwt> jwt = SecurityUtils.currentJwt();

// Get a specific claim
Optional<String> email = SecurityUtils.jwtClaim("email");

// Check roles (works with both JWT and non-JWT auth)
boolean isAdmin = SecurityUtils.hasRole("ftgo-admin");

// Check permissions
boolean canRead = SecurityUtils.hasAuthority("order:read");
```

The `JwtClaimsExtractor` provides typed extraction from JWT tokens:

```java
@Autowired
private JwtClaimsExtractor claimsExtractor;

// In a controller or service
public void handleRequest(JwtAuthenticationToken auth) {
    Jwt jwt = auth.getToken();
    String userId = claimsExtractor.extractUserId(jwt);
    List<String> roles = claimsExtractor.extractRoles(jwt);
    Optional<String> email = claimsExtractor.extractEmail(jwt);
}
```

## Keycloak Setup

### Local Development

```bash
cd infrastructure/keycloak
docker compose up -d
```

See `infrastructure/keycloak/README.md` for test users and token
acquisition instructions.

### Production

For production deployments:

1. Deploy Keycloak (or compatible OIDC provider) with HTTPS
2. Configure the `ftgo` realm with appropriate client settings
3. Set `publicClient: false` and configure client secrets
4. Update `issuer-uri` and `jwk-set-uri` to the production URLs
5. Restrict `redirectUris` and `webOrigins` to production domains

## Backward Compatibility

- When `ftgo.security.jwt.enabled` is `false` (default), the base HTTP Basic
  authentication from `FtgoSecurityAutoConfiguration` remains active.
- Existing services that do not set `ftgo.security.jwt.enabled=true` are
  unaffected.
- The `SecurityUtils` methods (`currentUsername()`, `hasRole()`, etc.) work
  with both authentication schemes.

## Module Structure

```
libs/ftgo-security-lib/
└── src/main/java/com/ftgo/security/
    ├── config/                                # Base security (HTTP Basic)
    │   ├── FtgoSecurityAutoConfiguration.java
    │   └── FtgoSecurityProperties.java
    ├── jwt/                                   # JWT authentication (this PR)
    │   ├── FtgoJwtAutoConfiguration.java      # OAuth2 Resource Server config
    │   ├── FtgoJwtProperties.java             # JWT configuration properties
    │   ├── FtgoJwtAuthenticationConverter.java # Claims → authorities mapping
    │   ├── JwtClaimsExtractor.java            # Typed claim extraction
    │   └── TokenRefreshService.java           # Token refresh mechanism
    ├── exception/
    │   └── SecurityExceptionHandlers.java     # JSON error responses
    └── util/
        └── SecurityUtils.java                 # Context helpers (JWT-aware)
```
