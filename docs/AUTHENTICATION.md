# FTGO Authentication Guide

## Overview

FTGO uses **JWT (JSON Web Token)** based authentication for securing microservice APIs. Each service acts as an **OAuth2 Resource Server**, independently validating JWT tokens without requiring a centralized session store.

The authentication infrastructure lives in `services/ftgo-security-lib` and is automatically applied to all services that depend on it.

## Architecture

```
                                    ┌──────────────────┐
                                    │   API Gateway /   │
  Client ──── Bearer Token ────────▶│   Auth Service    │
                                    │  (issues tokens)  │
                                    └────────┬─────────┘
                                             │
                      ┌──────────────────────┼──────────────────────┐
                      │                      │                      │
               ┌──────▼──────┐       ┌───────▼──────┐      ┌───────▼──────┐
               │   Order     │       │  Consumer    │      │  Restaurant  │
               │  Service    │       │  Service     │      │  Service     │
               │ (validates  │       │ (validates   │      │ (validates   │
               │  JWT locally│       │  JWT locally)│      │  JWT locally)│
               └─────────────┘       └──────────────┘      └──────────────┘
```

Each service:
1. Receives a request with `Authorization: Bearer <token>` header
2. Validates the JWT signature using the shared RSA public key
3. Extracts user identity and roles from token claims
4. Populates `SecurityContextHolder` for downstream use

## Token Structure

### Access Token Claims

| Claim         | Type       | Description                              |
|---------------|------------|------------------------------------------|
| `sub`         | String     | Username / subject                       |
| `iss`         | String     | Token issuer (default: `ftgo-platform`)  |
| `iat`         | Timestamp  | Issued-at time                           |
| `exp`         | Timestamp  | Expiration time                          |
| `jti`         | String     | Unique token ID (UUID)                   |
| `userId`      | String     | FTGO user identifier                     |
| `roles`       | String[]   | User roles (e.g., `ADMIN`, `USER`)       |
| `permissions` | String[]   | Fine-grained permissions (e.g., `order:read`) |
| `type`        | String     | Token type: `access`                     |

### Refresh Token Claims

Refresh tokens carry minimal claims for security:

| Claim    | Type      | Description                       |
|----------|-----------|-----------------------------------|
| `sub`    | String    | Username / subject                |
| `iss`    | String    | Token issuer                      |
| `iat`    | Timestamp | Issued-at time                    |
| `exp`    | Timestamp | Expiration time                   |
| `jti`    | String    | Unique token ID                   |
| `userId` | String    | FTGO user identifier              |
| `type`   | String    | Token type: `refresh`             |

## Token Lifecycle

```
1. LOGIN
   Client sends credentials to Auth Service
        │
        ▼
2. TOKEN GENERATION
   Auth Service validates credentials and generates:
   - Access Token  (short-lived, default 30 min)
   - Refresh Token (long-lived, default 7 days)
        │
        ▼
3. API REQUESTS
   Client includes access token in Authorization header:
   Authorization: Bearer <access_token>
        │
        ▼
4. TOKEN VALIDATION (per-service)
   Each service independently:
   a. Extracts JWT from Bearer header
   b. Validates signature with RSA public key
   c. Checks expiration
   d. Extracts claims (userId, roles, permissions)
   e. Populates SecurityContextHolder
        │
        ▼
5. TOKEN REFRESH
   When access token expires:
   Client sends refresh token to Auth Service
   Auth Service validates refresh token and issues new token pair
        │
        ▼
6. TOKEN EXPIRATION
   Refresh token expires → Client must re-authenticate
```

## Configuration

### Application Properties

Each service configures JWT via `application.yml`:

```yaml
ftgo:
  security:
    jwt:
      issuer: ${JWT_ISSUER:ftgo-platform}
      access-token-expiration: ${JWT_ACCESS_TOKEN_EXPIRATION:30m}
      refresh-token-expiration: ${JWT_REFRESH_TOKEN_EXPIRATION:7d}
```

### RSA Key Configuration

**Development/Testing:** An ephemeral RSA key pair is auto-generated on startup. A warning is logged.

**Production:** Provide RSA keys via properties:

```yaml
ftgo:
  security:
    jwt:
      rsa:
        public-key: classpath:keys/public.pem
        private-key: classpath:keys/private.pem
```

Or via file paths:

```yaml
ftgo:
  security:
    jwt:
      rsa:
        public-key: file:/etc/ftgo/keys/public.pem
        private-key: file:/etc/ftgo/keys/private.pem
```

### Generating RSA Keys

```bash
# Generate private key
openssl genrsa -out private.pem 2048

# Extract public key
openssl rsa -in private.pem -pubout -out public.pem
```

## Accessing User Context in Services

### Via SecurityUtils

```java
import com.ftgo.security.util.SecurityUtils;

// Get current username
Optional<String> username = SecurityUtils.getCurrentUsername();

// Get current user ID from JWT
Optional<String> userId = SecurityUtils.getCurrentUserId();

// Get full user details from JWT
Optional<FtgoUserDetails> details = SecurityUtils.getCurrentUserDetails();

// Check roles
boolean isAdmin = SecurityUtils.hasRole("ADMIN");

// Check permissions
boolean canRead = SecurityUtils.hasAuthority("order:read");
```

### Via Authentication Object (in Controllers)

```java
@GetMapping("/orders")
public List<Order> getOrders(Authentication authentication) {
    // authentication.getName() → username
    // authentication.getAuthorities() → roles + permissions
    JwtAuthenticationToken jwt = (JwtAuthenticationToken) authentication;
    FtgoUserDetails details = (FtgoUserDetails) jwt.getDetails();
    String userId = details.getUserId();
    // ...
}
```

## Security Considerations

1. **Key Management:** Never hardcode RSA keys. Use environment variables or a secrets manager in production.
2. **Token Expiration:** Keep access tokens short-lived (30 min default). Use refresh tokens for session continuity.
3. **HTTPS Required:** Always transmit tokens over TLS. Tokens are bearer credentials.
4. **Token Validation:** Each service validates tokens independently — no cross-service calls needed.
5. **Minimal Refresh Tokens:** Refresh tokens carry only identity claims, not authorization data.

## Library Components

| Class | Purpose |
|-------|---------|
| `JwtProperties` | Configures issuer, access/refresh token expiration |
| `RsaKeyProperties` | RSA public/private key configuration |
| `JwtConfiguration` | Bean definitions for encoder, decoder, token service |
| `JwtTokenService` | Generates access and refresh tokens |
| `JwtClaimsExtractor` | Extracts `FtgoUserDetails` from validated JWTs |
| `JwtAuthenticationConverter` | Converts JWT to Spring Security authentication |
| `FtgoUserDetails` | Immutable user identity extracted from JWT |
| `TokenResponse` | DTO for token endpoint responses |
| `BaseSecurityConfiguration` | Configures OAuth2 Resource Server with JWT |
| `SecurityUtils` | Static helpers for accessing current user context |
