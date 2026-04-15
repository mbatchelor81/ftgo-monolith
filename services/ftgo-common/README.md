# ftgo-common — Shared Library

Cross-cutting value objects and utilities for FTGO microservices.

## Version

`1.0.0`

## Package

`com.ftgo.common`

## Contents

### Value Objects (JPA `@Embeddable`)

| Class        | Description                                                                 |
|--------------|-----------------------------------------------------------------------------|
| `Money`      | Wraps `BigDecimal` with arithmetic (`add`, `multiply`, `isGreaterThanOrEqual`). Uses `@Embeddable` + `@Access(AccessType.FIELD)`. |
| `Address`    | 5-field address (street1, street2, city, state, zip). `@Embeddable`.        |
| `PersonName` | First name / last name pair. `@Embeddable`.                                 |

### Jackson Serialization

| Class                         | Description                                                       |
|-------------------------------|-------------------------------------------------------------------|
| `MoneyModule`                 | Custom Jackson `SimpleModule` with serializer/deserializer for `Money`. Serializes as a plain decimal string (e.g. `"12.34"`). |
| `CommonJsonMapperInitializer` | Spring `@PostConstruct` bean that registers `MoneyModule` and `JavaTimeModule` on the application `ObjectMapper`. |

### Configuration

| Class                 | Description                                                            |
|-----------------------|------------------------------------------------------------------------|
| `CommonConfiguration` | Spring `@Configuration` that provides an `ObjectMapper` bean and a `CommonJsonMapperInitializer` bean. |

### Exceptions

| Class                                | Description                                                |
|--------------------------------------|------------------------------------------------------------|
| `UnsupportedStateTransitionException`| Thrown when an invalid state transition is attempted.       |
| `NotYetImplementedException`         | Placeholder for unimplemented features.                    |

## Usage

Add the dependency in your service's `build.gradle`:

```groovy
dependencies {
    implementation project(':ftgo-common')
}
```

Or, when published to a Maven repository:

```groovy
dependencies {
    implementation 'com.ftgo:ftgo-common:1.0.0'
}
```

## Building

```bash
./gradlew :ftgo-common:build
```

## Publishing to Local Repository

```bash
./gradlew :ftgo-common:publishMavenJavaPublicationToLocalRepository
```

The artifact is published to `services/ftgo-common/build/repo/`.

## Testing

```bash
./gradlew :services:ftgo-common:test
```

Tests included:
- `MoneyTest` — Unit tests for `Money` arithmetic operations
- `MoneySerializationTest` — Jackson serialization/deserialization round-trip tests
