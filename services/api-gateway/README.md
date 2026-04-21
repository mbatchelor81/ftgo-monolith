# API Gateway

Edge proxy for the FTGO platform, built on **Spring Cloud Gateway**
(Spring Boot 3.2, reactive WebFlux). It is the single ingress for every
client of the platform and fronts the four bounded-context microservices.

## Responsibilities

- **Routing** — maps `/api/*` prefixes onto downstream services.
- **Authentication** — validates JWT bearer tokens as an OAuth2 Resource
  Server; unauthenticated requests get a 401 before hitting downstream.
- **Rate limiting** — Redis-backed token bucket per authenticated principal
  (falls back to `anonymous` for public routes).
- **Resilience** — Resilience4j circuit breakers, timeouts, and JSON
  fallbacks for every downstream route.
- **CORS + SSL/TLS** — global CORS policy and optional TLS termination
  (or terminate at the Ingress and leave `server.ssl.enabled=false`).
- **Observability** — correlation-ID propagation (`X-Correlation-Id`),
  structured access logs, Prometheus metrics on `/actuator/prometheus`.

## Route table

| Prefix                  | Downstream service            |
|-------------------------|-------------------------------|
| `/api/consumers/**`     | `:services:consumer-service`  |
| `/api/orders/**`        | `:services:order-service`     |
| `/api/restaurants/**`   | `:services:restaurant-service`|
| `/api/couriers/**`      | `:services:courier-service`   |

## Structure

```
api-gateway/
├── build.gradle
├── README.md
├── config/                 # Deployed-environment overrides
├── docker/Dockerfile
├── k8s/                    # Deployment, Service, ConfigMap, HPA, kustomization
└── src/
    ├── main/java/net/chrisrichardson/ftgo/apigateway/
    │   ├── ApiGatewayApplication.java
    │   ├── config/         # Security, CORS, rate-limit KeyResolvers, fallbacks
    │   └── filter/         # Correlation-ID + access-log global filters
    └── main/resources/application.yml
```

## Gradle coordinates

- Project path: `:services:api-gateway`
- Root Java package: `net.chrisrichardson.ftgo.apigateway`
- Docker image: `ftgo/api-gateway`

## Running locally

```bash
./gradlew :services:api-gateway:bootRun
```

Requires a reachable Redis (`REDIS_HOST`, `REDIS_PORT`) and, for real JWT
validation, an IdP (set `FTGO_JWT_ISSUER_URI` or `FTGO_JWT_JWK_SET_URI`).
