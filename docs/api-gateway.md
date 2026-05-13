# API Gateway

The API Gateway is the single entry point for all FTGO platform microservices.
It is built on [Spring Cloud Gateway](https://docs.spring.io/spring-cloud-gateway/reference/spring-cloud-gateway.html)
and runs as a reactive (WebFlux) application on port **8090**.

## Architecture

```
                 ┌──────────────────────────────────────────┐
  Clients ──────►│            API Gateway (:8090)           │
                 │                                          │
                 │  ┌─────────────┐  ┌───────────────────┐  │
                 │  │ JWT Authn   │  │ Rate Limiting     │  │
                 │  │ (OAuth2 RS) │  │ (Redis)           │  │
                 │  └─────────────┘  └───────────────────┘  │
                 │  ┌─────────────┐  ┌───────────────────┐  │
                 │  │ Circuit     │  │ Request/Response   │  │
                 │  │ Breaker     │  │ Logging           │  │
                 │  └─────────────┘  └───────────────────┘  │
                 └──────┬──────┬──────┬──────┬──────────────┘
                        │      │      │      │
                 ┌──────▼──┐ ┌─▼────┐ ▼────┐ ▼──────────┐
                 │ Order   │ │Consu-│ │Rest-│ │ Courier  │
                 │ Service │ │mer   │ │aura-│ │ Service  │
                 └─────────┘ └──────┘ │nt   │ └──────────┘
                                      └─────┘
```

## Route Configuration

| Route Path          | Target Service       | Circuit Breaker     |
|---------------------|----------------------|---------------------|
| `/api/orders/**`    | `lb://order-service` | `orderServiceCB`    |
| `/api/consumers/**` | `lb://consumer-service` | `consumerServiceCB` |
| `/api/restaurants/**` | `lb://restaurant-service` | `restaurantServiceCB` |
| `/api/couriers/**`  | `lb://courier-service` | `courierServiceCB`  |

All routes strip the `/api` prefix before forwarding (e.g., `/api/orders/123` → `/orders/123`).

## Security — JWT Validation

Authentication uses OAuth2 Resource Server with JWT tokens, delegating to `ftgo-security-lib` for
claims extraction (roles, permissions, user ID).

| Property | Default | Description |
|----------|---------|-------------|
| `ftgo.security.jwt.issuer-uri` | `http://localhost:8180/realms/ftgo` | JWT issuer URI |
| `ftgo.security.jwt.jwk-set-uri` | `http://localhost:8180/realms/ftgo/protocol/openid-connect/certs` | JWK Set URI for key verification |
| `ftgo.security.jwt.roles-claim-name` | `realm_access.roles` | JWT claim containing user roles |
| `ftgo.security.jwt.permissions-claim-name` | `permissions` | JWT claim for permissions |

Public (unauthenticated) endpoints:
- `/actuator/health`, `/actuator/health/**`, `/actuator/info`, `/actuator/prometheus`

## Rate Limiting

Rate limiting uses Redis via `RedisRateLimiter`. Requests are keyed by authenticated principal
or, for unauthenticated requests, by client IP address.

| Parameter | Default | Description |
|-----------|---------|-------------|
| Replenish rate | 10 | Tokens added per second |
| Burst capacity | 20 | Maximum tokens in the bucket |
| Requested tokens | 1 | Tokens consumed per request |

**Configuration:**

| Property | Default |
|----------|---------|
| `spring.data.redis.host` | `localhost` |
| `spring.data.redis.port` | `6379` |
| `ftgo.gateway.rate-limiting.enabled` | `true` |

Set `ftgo.gateway.rate-limiting.enabled=false` to disable rate limiting (e.g., in test environments).

## Circuit Breaker

Each downstream route has a dedicated Resilience4J circuit breaker with the following defaults:

| Parameter | Value |
|-----------|-------|
| Failure rate threshold | 50% |
| Wait duration in open state | 30 seconds |
| Sliding window size | 10 calls |
| Minimum number of calls | 5 |
| Permitted calls in half-open | 3 |
| Timeout | 5 seconds |

When a circuit breaker trips, requests are forwarded to the `/fallback` endpoint which returns:

```json
{
  "status": 503,
  "error": "Service Unavailable",
  "message": "The downstream service is temporarily unavailable. Please try again later.",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

## Request/Response Logging

A global filter logs every request and response:

```
>>> GET /api/orders/123 from=10.0.0.1 requestId=abc-123
<<< GET /api/orders/123 status=200 duration=45ms requestId=abc-123
```

If the client sends an `X-Request-Id` header, it is propagated; otherwise a UUID is generated.

## Running Locally

```bash
# Start Redis (required for rate limiting)
docker run -d --name redis -p 6379:6379 redis:7-alpine

# Start the gateway
./gradlew :api-gateway:bootRun

# Or, without rate limiting (no Redis needed)
FTGO_GATEWAY_RATE_LIMITING_ENABLED=false ./gradlew :api-gateway:bootRun
```

The gateway starts on **http://localhost:8090**.

## Actuator Endpoints

| Endpoint | Auth Required | Description |
|----------|---------------|-------------|
| `/actuator/health` | No | Health status |
| `/actuator/info` | No | Build info |
| `/actuator/prometheus` | No | Prometheus metrics |
| `/actuator/gateway` | Yes | Gateway route information |

## Module Dependencies

```
api-gateway
├── spring-cloud-starter-gateway           (routing, filters)
├── spring-boot-starter-security           (security framework)
├── spring-boot-starter-oauth2-resource-server  (JWT validation)
├── spring-cloud-starter-circuitbreaker-reactor-resilience4j
├── spring-boot-starter-data-redis-reactive (rate limiting)
├── micrometer-registry-prometheus          (metrics)
└── ftgo-security-lib                      (JWT properties, claims extraction)
```
