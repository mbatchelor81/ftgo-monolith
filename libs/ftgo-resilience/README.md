# ftgo-resilience

Shared FTGO library that provides production-grade resilience patterns and
health indicators for every deployable microservice.

Added as part of **EM-44 — Configure Health Checks, Service Discovery, and
Resilience Patterns**.

## What it ships

* **Resilience4j auto-configuration** — circuit breakers, retries with
  exponential backoff, bulkheads for thread-pool isolation, and time
  limiters. Services tune the defaults in `application.yml` under the
  `resilience4j.*` / `ftgo.resilience.*` keys.
* **Resilient `WebClient.Builder`** — pre-wires Resilience4j operators onto
  outbound reactive HTTP calls so inter-service communication degrades
  gracefully instead of cascading failures.
* **Kubernetes-native service discovery** — a small `ServiceEndpoints`
  resolver that builds URLs from cluster DNS (`http://<name>.<ns>:<port>`)
  driven by `ftgo.services.<name>.*` config keys. No Eureka or Consul.
* **Custom health indicators** for Spring Boot Actuator:
  * `DependentServiceHealthIndicator` — pings a downstream service's
    `/actuator/health` endpoint and reports status.
  * `BusinessHealthIndicator` (abstract) — base class for per-service
    business health checks (DB reachability is already covered by Boot's
    built-in `DataSourceHealthIndicator`).

## How services consume it

```gradle
plugins {
    id 'ftgo.spring-boot-conventions'
    id 'ftgo.testing-conventions'
    id 'ftgo.docker-conventions'
}

dependencies {
    implementation project(':libs:ftgo-resilience')
}
```

The shared configuration is picked up automatically via
`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
— services only need to declare which downstream services they depend on
under `ftgo.resilience.dependent-services` in `application.yml`.

## References

* [Resilience4j Spring Boot docs](https://resilience4j.readme.io/docs/getting-started-3)
* [Spring Boot Actuator health groups](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.endpoints.health.groups)
* [Kubernetes Services — DNS for Services and Pods](https://kubernetes.io/docs/concepts/services-networking/dns-pod-service/)
