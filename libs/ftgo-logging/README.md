# ftgo-logging

Centralized logging support shared by every FTGO microservice.

Provides:

- **Structured JSON output** via `logstash-logback-encoder` so logs ship
  cleanly into Elasticsearch/Fluentd/Kibana (EFK) without brittle regex
  parsing.
- **Async appender** (`ch.qos.logback.classic.AsyncAppender`) wrapping the
  JSON console appender so logging I/O never blocks request threads.
- **Correlation-ID propagation**: a servlet filter
  (`CorrelationIdFilter`) that reads `X-Correlation-ID` /
  `X-Request-ID` from inbound requests (or mints a fresh UUID), places
  the value into SLF4J's MDC, and echoes it back on the response.
- **MDC keys catalog** (`MdcKeys`) so downstream libraries reuse the
  same field names.
- **Base Logback configuration** (`logback-ftgo.xml`) that services
  include from their own `logback-spring.xml`.

## Usage from a microservice

`build.gradle`:

```gradle
dependencies {
    implementation project(':libs:ftgo-logging')
}
```

`src/main/resources/logback-spring.xml`:

```xml
<configuration>
    <include resource="logback-ftgo.xml"/>
</configuration>
```

That's it — the service now emits one JSON object per log line on stdout
(which Fluentd tails from the container stdout stream), with the
correlation ID attached as an MDC field.

## Contents

| File | Purpose |
|---|---|
| `CorrelationIdFilter.java` | Servlet filter: MDC + response header |
| `CorrelationIdConfiguration.java` | Spring `@Configuration` registering the filter |
| `MdcKeys.java` | Shared constants for MDC keys |
| `resources/logback-ftgo.xml` | Base Logback include (JSON + async + MDC) |

## Java package

`net.chrisrichardson.ftgo.logging` — consistent with the rest of the FTGO
codebase so the Logback config's `<logger name="net.chrisrichardson.ftgo">`
rules cover this module too.
