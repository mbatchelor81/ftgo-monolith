# ftgo-common-lib

Shared, framework-light value objects used by every FTGO microservice:
`Money`, `PersonName`, `Address`, and their Jackson serialization support.

## Contents

**Value objects (JPA `@Embeddable`)**

- `Money` — `BigDecimal`-backed amount with arithmetic helpers and
  `MoneyModule` for Jackson string serialization.
- `Address` — five-field postal address (`street1`, `street2`, `city`,
  `state`, `zip`).
- `PersonName` — `firstName` / `lastName`.

**Utilities**

- `CommonConfiguration` — Spring `@Configuration` exposing an
  `ObjectMapper` pre-wired with the common modules.
- `CommonJsonMapperInitializer` — registers `MoneyModule` and
  `JavaTimeModule` on the shared `ObjectMapper`.
- `MoneyModule` — custom Jackson serializer / deserializer that
  round-trips `Money` as a JSON string.

**Exceptions**

- `UnsupportedStateTransitionException`
- `NotYetImplementedException`

## Coordinates

```
group:    net.chrisrichardson.ftgo
artifact: ftgo-common-lib
version:  1.0.0
```

## Consuming from a microservice

```gradle
dependencies {
    compile "net.chrisrichardson.ftgo:ftgo-common-lib:1.0.0"
}
```

## Build & test

```bash
./gradlew :libs:ftgo-common:build
```

Publishing to the local Maven-style repository under `build/repo`:

```bash
./gradlew :libs:ftgo-common:publish
```

## Migration note

This library was extracted from the legacy root-level `ftgo-common/`
module as part of **EM-32**. The legacy module is still present to keep
the monolith compiling; microservices should depend on
`ftgo-common-lib:1.0.0` instead as they are migrated out.
