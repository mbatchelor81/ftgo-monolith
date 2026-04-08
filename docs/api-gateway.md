# FTGO API Gateway

> **Jira**: EM-38 — Configure API Gateway with Security, Routing, and Rate Limiting

## Overview

The FTGO API Gateway is the single entry point for all client traffic into the
microservices platform. It is built on **Spring Cloud Gateway** (reactive,
Netty-based) and provides:

- Centralised routing to downstream services
- Redis-backed request rate limiting
- Resilience4j circuit breakers with fallback responses
- CORS configuration at the edge
- Request/response logging with correlation ID propagation

## Architecture

```
                   ┌─────────────────────┐
                   │   ftgo-api-gateway   │
  Clients ────────>│  (Spring Cloud GW)  │
                   └────┬───┬───┬───┬────┘
                        │   │   │   │
          ┌─────────────┘   │   │   └─────────────┐
          v                 v   v                 v
  ┌──────────────┐ ┌────────────────┐ ┌────────────────┐ ┌──────────────┐
  │ Order Service│ │Consumer Service│ │Restaurant Svc  │ │Courier Svc   │
  └──────────────┘ └────────────────┘ └────────────────┘ └──────────────┘
```

## Route Configuration

| Route Pattern         | Downstream Service     | Default URL                              |
|-----------------------|------------------------|------------------------------------------|
| `/api/orders/**`      | Order Service          | `http://ftgo-order-service:8080`         |
| `/api/consumers/**`   | Consumer Service       | `http://ftgo-consumer-service:8080`      |
| `/api/restaurants/**` | Restaurant Service     | `http://ftgo-restaurant-service:8080`    |
| `/api/couriers/**`    | Courier Service        | `http://ftgo-courier-service:8080`       |

Downstream URLs are configurable via environment variables
(`FTGO_ORDER_SERVICE_URL`, etc.) and default to Kubernetes service DNS names.

## Rate Limiting

Rate limiting uses **Spring Cloud Gateway's `RequestRateLimiter` filter**
backed by Redis (via the Token Bucket algorithm).

| Parameter           | Default Value | Description                          |
|---------------------|---------------|--------------------------------------|
| `replenishRate`     | 20            | Tokens added per second              |
| `burstCapacity`     | 40            | Maximum tokens in the bucket         |
| `requestedTokens`   | 1             | Tokens consumed per request          |

The rate limiter key is the client IP address (resolved by `ipKeyResolver`).

### Redis dependency

When Redis is unavailable the gateway allows all traffic through (open
by default). Provide `REDIS_HOST` and `REDIS_PORT` environment variables
in production.

## Circuit Breakers

Each route has a dedicated **Resilience4j** circuit breaker. When a
downstream service becomes unhealthy the circuit opens and requests are
redirected to a fallback endpoint that returns `503 Service Unavailable`.

| Parameter                                       | Value    |
|-------------------------------------------------|----------|
| Failure rate threshold                          | 50 %     |
| Wait duration in open state                     | 10 s     |
| Sliding window size                             | 10 calls |
| Minimum number of calls before evaluation       | 5        |
| Permitted calls in half-open state              | 3        |
| Automatic transition from open to half-open     | true     |
| Time limiter timeout                            | 4 s      |

Fallback endpoints:
- `GET /fallback/orders`
- `GET /fallback/consumers`
- `GET /fallback/restaurants`
- `GET /fallback/couriers`

## CORS

CORS is configured at the gateway level so downstream services do not
need their own CORS setup. The current configuration allows all origins
(`*`), all methods, and all headers with credentials support. In
production you should restrict `allowedOriginPattern` to your domain(s).

## Logging and Correlation IDs

Two global filters run on every request:

1. **CorrelationIdFilter** (highest precedence) — ensures every request
   has an `X-Correlation-Id` header. If the header is missing a new UUID
   is generated. The ID is forwarded to downstream services and included
   in the response.

2. **RequestLoggingFilter** — logs the HTTP method, URI, response status
   code, and elapsed time for every request.

## Actuator Endpoints

| Endpoint                       | Description                    |
|--------------------------------|--------------------------------|
| `/actuator/health`             | Health check (liveness + readiness) |
| `/actuator/health/liveness`    | Kubernetes liveness probe      |
| `/actuator/health/readiness`   | Kubernetes readiness probe     |
| `/actuator/metrics`            | Micrometer metrics             |
| `/actuator/prometheus`         | Prometheus scrape endpoint     |
| `/actuator/gateway/routes`     | List configured gateway routes |

## Kubernetes Deployment

Manifests are located at `k8s/base/ftgo-api-gateway/`:

```
k8s/base/ftgo-api-gateway/
├── configmap.yaml      # Environment variables (service URLs, Redis)
├── secret.yaml         # Secrets (provided via overlay / external-secrets)
├── deployment.yaml     # 2 replicas, rolling update, probes
├── service.yaml        # ClusterIP on port 8080
└── kustomization.yaml
```

The gateway is included in the base Kustomization at `k8s/base/kustomization.yaml`.

## Local Development

```bash
# Build the gateway module
./gradlew :services:ftgo-api-gateway:build

# Run the gateway (requires Redis on localhost:6379 for rate limiting)
./gradlew :services:ftgo-api-gateway:bootRun

# Run without Redis (rate limiting is skipped when Redis is unavailable)
./gradlew :services:ftgo-api-gateway:bootRun
```

Override downstream service URLs for local development:

```bash
FTGO_ORDER_SERVICE_URL=http://localhost:8081 \
FTGO_CONSUMER_SERVICE_URL=http://localhost:8082 \
FTGO_RESTAURANT_SERVICE_URL=http://localhost:8083 \
FTGO_COURIER_SERVICE_URL=http://localhost:8084 \
./gradlew :services:ftgo-api-gateway:bootRun
```

## Build

The gateway module uses the standard FTGO convention plugins:

- `ftgo.spring-boot-conventions` — Java 17, Spring Boot 3.2.x
- `ftgo.testing-conventions` — JUnit 5, integration tests
- `ftgo.docker-conventions` — Jib container image builds

```bash
# Build Docker image
./gradlew :services:ftgo-api-gateway:jibDockerBuild
```
