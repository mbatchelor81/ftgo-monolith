# FTGO Logging Standards

Every FTGO microservice emits logs in the same format, with the same
structure, at the same levels, and with the same sensitive-data
protections. This document is the source of truth. If the code disagrees,
change the code.

Related artifacts:

* Shared Logback config: [`libs/ftgo-logging/src/main/resources/logback-ftgo.xml`](../libs/ftgo-logging/src/main/resources/logback-ftgo.xml)
* Utility classes: [`libs/ftgo-logging/src/main/java/net/chrisrichardson/ftgo/logging/`](../libs/ftgo-logging/src/main/java/net/chrisrichardson/ftgo/logging/)
* Per-service profile overrides: `services/<service>/config/application.yml`

Upstream dependencies: EM-42 (distributed tracing with Micrometer
Tracing + Brave), EM-43 (centralized log aggregation via Fluentd +
Elasticsearch).

---

## 1. Log Levels

Pick the lowest level that still answers "if this statement fires, what
does an operator need to do?" — that is the only meaningful difference
between the levels in production.

| Level | When to use | Examples |
|-------|-------------|----------|
| `TRACE` | Byte-level or loop-body detail. Almost never committed; used ad-hoc during a live debugging session. | Per-row iterator diagnostics; raw wire frames. |
| `DEBUG` | Developer-facing internals that are noisy in production but valuable locally. | Outbound HTTP request/response summaries (without bodies); cache hits/misses; state-machine transitions. |
| `INFO` | Business events that a humans would want to scan in Kibana. Low cardinality, stable message format. | Order created, consumer registered, courier scheduled, JWT issued. Inbound request summary (method + path + status + latency). |
| `WARN` | Something recovered or is about to become a problem. Always actionable. | Retry fired, circuit breaker half-open, deprecated API used, quota 80% consumed. |
| `ERROR` | The request or background task failed and the caller noticed. | Unhandled exception, failed downstream call after retries exhausted, schema migration failure. |

Rules of thumb:

* Do not log successful happy-path CRUD reads at INFO. They create
  100:1 noise ratios without adding information.
* Every `ERROR` must either throw (so a global handler can respond)
  or document the recovery in the same log line.
* Never down-grade an exception to WARN unless recovery is automatic
  and the caller was not affected.

## 2. What to log

**Always:**

* **Service method entry/exit** — handled automatically by
  [`LoggingAspect`](../libs/ftgo-logging/src/main/java/net/chrisrichardson/ftgo/logging/LoggingAspect.java)
  at DEBUG for every Spring `@Service` / `@Repository`. No manual code
  is required.
* **External calls** — log the target service, endpoint, and outcome
  (status + latency). Use INFO for success, WARN/ERROR for failure.
* **Errors** — always log the full exception + stack trace at ERROR.
  Never swallow exceptions.
* **Business events** — the canonical order-lifecycle transitions
  (order created, accepted, ready for pickup, delivered, cancelled)
  must be logged at INFO with the order id in MDC. Ditto for consumer
  registration, courier availability changes, and restaurant menu
  revisions.
* **Auth decisions** — every successful authentication, authorization
  failure, and token issuance gets a single INFO line.

**Never:**

* **PII, credentials, tokens, API keys.** See §4.
* **Full request or response bodies in production.** Log request
  size, method, URI, and status. Bodies are allowed at DEBUG in local
  dev only.
* **Sensitive headers.** `Authorization`, `Cookie`, `Set-Cookie`, and
  `X-Api-Key` must be stripped before logging.
* **Stack traces inside loops.** Log once, outside the loop, with a
  count.
* **One-off debugging prints.** Every log call is forever; treat it
  as a public contract.

## 3. MDC fields

Every log line — JSON or human-readable — carries these keys when the
underlying value is set. Field names are defined in
[`MdcKeys`](../libs/ftgo-logging/src/main/java/net/chrisrichardson/ftgo/logging/MdcKeys.java)
so downstream tooling (Kibana dashboards, Fluentd filters) can rely on
a single schema.

| Key | Source | Example |
|-----|--------|---------|
| `service` | `spring.application.name` | `order-service` |
| `traceId` | Micrometer Tracing / Brave | `65f2a1b4c3d2e1f0` |
| `spanId` | Micrometer Tracing / Brave | `7e8d9c0b1a2b3c4d` |
| `correlationId` | `CorrelationIdFilter` (inbound `X-Correlation-ID` / `X-Request-ID` header, else newly generated) | `0f8b0e08-b32e-4c42-8c31-3f0b1f7f9e44` |
| `requestId` | `CorrelationIdFilter` (per-service request scope) | `9b5f04f0-…` |
| `userId` | `LogContext.withUserId(...)` in the security filter or service layer | `user-1234` |

Setting a key by hand:

```java
try (var ignored = LogContext.withUserId(principal.getId())) {
    orderService.placeOrder(request);
}
```

Always use
[`LogContext`](../libs/ftgo-logging/src/main/java/net/chrisrichardson/ftgo/logging/LogContext.java)
rather than `MDC.put` directly — it restores the previous value on
close so nested scopes don't clobber each other.

## 4. Sensitive-data masking

All log lines pass through
[`SensitiveDataMaskingConverter`](../libs/ftgo-logging/src/main/java/net/chrisrichardson/ftgo/logging/SensitiveDataMaskingConverter.java)
(registered as `%maskedMsg` in `logback-ftgo.xml`). The converter is on
in every environment — including local dev — so developers see the same
redacted output as production.

Redacted automatically:

| Category | Rule | Example output |
|----------|------|----------------|
| Credit card numbers | 13–19 consecutive digits (hyphens/spaces optional) | `**** **** **** 1111` |
| `key=value` secrets | Keys matching `password`, `passwd`, `pwd`, `secret`, `token`, `api-key`, `access-key`, `authorization`, `auth-token`, `bearer` (case-insensitive) | `password=***REDACTED***` |
| JSON-style secrets | `"password": "..."`, `"token": "..."`, etc. | `"token": "***REDACTED***"` |
| Bare JWTs | Three base64url segments joined by `.` | `***REDACTED***` |

The masker is a defense-in-depth net, not a substitute for discipline.
**Still do not construct log messages that include sensitive fields in
the first place.** If a new category of sensitive data (e.g. SSN, phone
number) needs automatic redaction, extend the converter — do not
scatter one-off scrubbing around the codebase.

## 5. Output format

Format is chosen by Spring profile in `logback-ftgo.xml`:

| Profile | Appenders | Purpose |
|---------|-----------|---------|
| `local`, `default` | Human-readable console **+** rolling file (`logs/<service>.log`, 50 MB / file, 30-day history, 1 GB cap, gzip) | IDE-friendly output; file sink keeps history when the terminal scrolls away. |
| `dev`, `staging` | JSON on stdout | Fluentd tails container stdout and ships to Elasticsearch. |
| `prod` | JSON on stdout | Same as dev/staging. Framework loggers at ERROR (see §6). |
| _any other_ | JSON on stdout | Safe default for CLI utilities. |

The JSON encoder emits one object per line with this shape:

```json
{
  "@timestamp": "2026-04-21T14:03:55.912Z",
  "level": "INFO",
  "logger": "com.ftgo.order.web.OrderController",
  "thread": "http-nio-8080-exec-3",
  "message": "Order 4711 accepted by restaurant 42",
  "service": "order-service",
  "traceId": "65f2a1b4c3d2e1f0",
  "spanId": "7e8d9c0b1a2b3c4d",
  "correlationId": "0f8b0e08-b32e-4c42-8c31-3f0b1f7f9e44",
  "requestId": "9b5f04f0-…",
  "userId": "user-1234"
}
```

All appenders are wrapped in `AsyncAppender` with `neverBlock=true`, so
a stalled log destination drops events rather than blocking request
threads.

## 6. Per-environment log levels

Acceptance criteria (EM-49) map to `logback-ftgo.xml` and each service's
`application.yml`:

| Environment | Application code | Spring / Hibernate / Hikari |
|-------------|------------------|-----------------------------|
| Local (default profile) | `DEBUG` | `INFO` |
| Dev / Staging | `INFO` | `WARN` |
| Production | `INFO` | `ERROR` |

Operators can crank verbosity at runtime without a rebuild via the
`LOG_LEVEL_ROOT`, `LOG_LEVEL_FTGO`, and `LOG_LEVEL_FRAMEWORK`
environment variables (all honored by `logback-ftgo.xml`). Flipping a
single service to DEBUG for a triage window is preferred over a
fleet-wide change.

## 7. Async logging

`ASYNC_JSON` (deployed) and `ASYNC_LOCAL_*` (local) appenders sit in
front of every sink. Tuning parameters:

| Parameter | Value | Rationale |
|-----------|-------|-----------|
| `queueSize` | 8192 (JSON) / 4096 (local) | Enough headroom for a 1-second spike at 2k req/s. |
| `discardingThreshold` | 0 | Never drop INFO when queue is 80% full. |
| `neverBlock` | `true` | Prefer dropped events over stalled request threads. |
| `includeCallerData` | `false` | Caller-data lookup is expensive and not worth the hit. |

The async wrapper means `log.info(...)` is non-blocking in production.
Do not reach for synchronous sinks to "make sure" a log line is emitted
before the process exits — Spring Boot's graceful shutdown drains the
AsyncAppenders automatically.

## 8. Method entry / exit aspect

[`LoggingAspectConfiguration`](../libs/ftgo-logging/src/main/java/net/chrisrichardson/ftgo/logging/LoggingAspectConfiguration.java)
registers [`LoggingAspect`](../libs/ftgo-logging/src/main/java/net/chrisrichardson/ftgo/logging/LoggingAspect.java)
whenever AspectJ is on the classpath (true for every service applying
`ftgo.observability-conventions`). It logs:

* `-> <Class>.<method>` at DEBUG on entry
* `<- <Class>.<method> (<ms> ms)` at DEBUG on successful return
* `!! <Class>.<method> failed after <ms> ms: <exception>` at ERROR on
  throw

The aspect **never logs method arguments** — they frequently contain
the PII/credentials that §4 bans — and early-returns when the target
logger is disabled, so the cost is a single `isDebugEnabled()` check
in production.

Disable per-service with `ftgo.logging.aspect.enabled: false` in
`application.yml` when rolling out to latency-sensitive services.

Tune package scope with standard Spring Boot logger configuration:

```yaml
logging:
  level:
    com.ftgo.order.domain: DEBUG  # Entry/exit visible
    com.ftgo.order.web: INFO       # Entry/exit suppressed
```

## 9. Adopting in a new service

1. Depend on `libs:ftgo-logging` (already done for every service on
   `ftgo.observability-conventions`).
2. Add a `src/main/resources/logback-spring.xml` that includes the
   shared config:
   ```xml
   <configuration>
       <include resource="logback-ftgo.xml"/>
       <!-- service-specific overrides below this line -->
   </configuration>
   ```
3. Set `spring.application.name` in `application.yml` so the `service`
   MDC field renders correctly.
4. (Optional) Populate `userId` via `LogContext.withUserId(...)` in the
   security filter or handler where the principal becomes known.

## 10. Changing the standard

Every change to this document, `logback-ftgo.xml`, `MdcKeys`, or
`SensitiveDataMaskingConverter` is a cross-cutting platform change.
Open a PR targeting `feat/microservices-migration`, loop in the
platform reviewers from `CODEOWNERS`, and flag the change on the
`#ftgo-platform` channel before merging.
