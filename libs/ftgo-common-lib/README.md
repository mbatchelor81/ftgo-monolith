# ftgo-common-lib

Standalone, versioned shared library extracted from the `ftgo-common` monolith module. Provides cross-cutting value objects, Jackson serialization utilities, Spring configuration, and common exceptions used across FTGO microservices.

## Version

Current version: **1.0.0** (managed in `gradle.properties` as `ftgoCommonLibVersion`)

## Contents

### Value Objects (`@Embeddable`)

| Class | Description |
|-------|-------------|
| `Money` | Wraps `BigDecimal` with arithmetic (`add`, `multiply`, `isGreaterThanOrEqual`). JPA `@Embeddable` with field access. |
| `Address` | 5-field address (street1, street2, city, state, zip). JPA `@Embeddable`. |
| `PersonName` | First/last name. JPA `@Embeddable`. |

### Jackson Serialization

| Class | Description |
|-------|-------------|
| `MoneyModule` | Custom Jackson `SimpleModule` with serializer/deserializer for `Money` (string representation). |

### Spring Configuration

| Class | Description |
|-------|-------------|
| `CommonConfiguration` | `@Configuration` class providing `ObjectMapper` and `CommonJsonMapperInitializer` beans. |
| `CommonJsonMapperInitializer` | Registers `MoneyModule` and `JavaTimeModule` on the `ObjectMapper` at startup. |

### Exceptions

| Class | Description |
|-------|-------------|
| `UnsupportedStateTransitionException` | Thrown when an invalid state transition is attempted. |
| `NotYetImplementedException` | Placeholder for unimplemented functionality. |

## Usage

### Gradle Dependency

```groovy
// In a microservice build.gradle
dependencies {
    implementation project(':ftgo-common-lib')
}
```

Once published to a Maven repository:

```groovy
dependencies {
    implementation 'net.chrisrichardson.ftgo:ftgo-common-lib:1.0.0'
}
```

### Publishing

Publish to the local project repository:

```bash
./gradlew :ftgo-common-lib:publish
```

Publish to Maven Local (`~/.m2/repository`):

```bash
./gradlew :ftgo-common-lib:publishToMavenLocal
```

## Dependencies

- `spring-boot-starter-data-jpa` (JPA annotations)
- `jackson-core`, `jackson-databind`, `jackson-datatype-jsr310` (JSON serialization)
- `commons-lang` 2.6 (`EqualsBuilder`, `HashCodeBuilder`, `ToStringBuilder`)

## Tests

Run unit tests:

```bash
./gradlew :ftgo-common-lib:test
```

Tests included:
- `MoneyTest` — Arithmetic operations, string conversion, comparison
- `MoneySerializationTest` — Jackson serialization/deserialization roundtrip
