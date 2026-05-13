# Keycloak — FTGO Identity Provider

Local Keycloak instance for JWT token issuance and validation during development.

## Quick Start

```bash
cd infrastructure/keycloak
docker compose up -d
```

Keycloak will be available at **http://localhost:8180**.

- Admin console: http://localhost:8180/admin (admin / admin)
- FTGO realm OIDC discovery: http://localhost:8180/realms/ftgo/.well-known/openid-configuration

## Pre-configured Resources

### Realm: `ftgo`

| Resource     | Details                              |
|--------------|--------------------------------------|
| Realm roles  | `ftgo-consumer`, `ftgo-restaurant`, `ftgo-courier`, `ftgo-admin` |
| Public client| `ftgo-api` (for frontend / API gateway) |
| Confidential | `ftgo-service` (service-to-service)  |

### Test Users

| Username      | Password  | Roles            |
|---------------|-----------|------------------|
| consumer1     | password  | ftgo-consumer    |
| restaurant1   | password  | ftgo-restaurant  |
| courier1      | password  | ftgo-courier     |
| admin         | password  | all roles        |

## Obtaining a Token

```bash
curl -s -X POST http://localhost:8180/realms/ftgo/protocol/openid-connect/token \
  -d "grant_type=password" \
  -d "client_id=ftgo-api" \
  -d "username=consumer1" \
  -d "password=password" | jq .access_token -r
```

## Service Configuration

Add to each microservice's `application.yml`:

```yaml
ftgo:
  security:
    jwt:
      enabled: true
      issuer-uri: http://localhost:8180/realms/ftgo
      jwk-set-uri: http://localhost:8180/realms/ftgo/protocol/openid-connect/certs
```
