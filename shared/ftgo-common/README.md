# ftgo-common-lib

Shared value objects, serialization modules, and utilities for FTGO microservices.

## Coordinates

```
groupId:    com.ftgo
artifactId: ftgo-common-lib
version:    1.0.0
```

## Overview

This library extracts the cross-cutting concerns from the legacy `ftgo-common` monolith module into a standalone, independently versioned artifact. All classes use the `com.ftgo.common` package namespace.

## API Reference

### Value Objects (JPA `@Embeddable`)

| Class | Description |
|-------|-------------|
| `Money` | Wraps `BigDecimal` with arithmetic operations (`add`, `multiply`, `isGreaterThanOrEqual`). Uses `@Embeddable` + `@Access(AccessType.FIELD)`. Custom Jackson serialization via `MoneyModule`. |
| `Address` | 5-field address value object (street1, street2, city, state, zip). `@Embeddable`. |
| `PersonName` | First name / last name value object. `@Embeddable`. |

### Serialization

| Class | Description |
|-------|-------------|
| `MoneyModule` | Jackson `SimpleModule` that serializes `Money` as a plain string (e.g. `"12.34"`) and deserializes it back. Register with `objectMapper.registerModule(new MoneyModule())`. |
| `CommonJsonMapperInitializer` | Spring-aware initializer that registers `MoneyModule` and `JavaTimeModule` on the application's `ObjectMapper`, and disables `WRITE_DATES_AS_TIMESTAMPS`. |
| `CommonConfiguration` | Spring `@Configuration` class that provides an `ObjectMapper` bean and a `CommonJsonMapperInitializer` bean. |

### Exceptions

| Class | Description |
|-------|-------------|
| `UnsupportedStateTransitionException` | Thrown when a domain entity receives a state transition that is not valid for its current state. Accepts an `Enum` representing the current state. |
| `NotYetImplementedException` | Marker exception for unimplemented functionality. |

## Usage

### Gradle Dependency

Add the library as a dependency in your service's `build.gradle`:

```groovy
dependencies {
    implementation "com.ftgo:ftgo-common-lib:${ftgoCommonLibVersion}"
}
```

The version is centrally managed in `gradle.properties`:

```properties
ftgoCommonLibVersion=1.0.0
```

### For Intra-Repo Modules

Modules within this mono-repo can depend on the Gradle project directly:

```groovy
dependencies {
    implementation project(':shared-ftgo-common')
}
```

### Quick Start

```java
import com.ftgo.common.Money;
import com.ftgo.common.Address;
import com.ftgo.common.PersonName;
import com.ftgo.common.MoneyModule;

// Value objects
Money price = new Money("19.99");
Money total = price.multiply(3);           // 59.97
boolean canAfford = total.isGreaterThanOrEqual(new Money(50));  // true

Address addr = new Address("123 Main St", null, "Oakland", "CA", "94612");
PersonName name = new PersonName("Jane", "Doe");

// Jackson serialization
ObjectMapper mapper = new ObjectMapper();
mapper.registerModule(new MoneyModule());
String json = mapper.writeValueAsString(price);  // "19.99"
```

## Publishing

### Local Repository

```bash
./gradlew :shared-ftgo-common:publishMavenJavaPublicationToLocalRepository
```

Artifacts are written to `build/repo/`.

### Maven Local (~/.m2)

```bash
./gradlew :shared-ftgo-common:publishToMavenLocal
```

### Remote Repository

```bash
./gradlew :shared-ftgo-common:publish \
    -PrepoUrl=https://your-repo.example.com/releases \
    -PrepoUsername=user \
    -PrepoPassword=pass
```

## Building & Testing

```bash
# Build and test
./gradlew :shared-ftgo-common:clean :shared-ftgo-common:build :shared-ftgo-common:test

# Verify cross-module compatibility
./gradlew :services-ftgo-order-service:build :services-ftgo-consumer-service:build :shared-ftgo-common:build
```

## Versioning

This library follows [Semantic Versioning](https://semver.org/):

- **MAJOR** — Breaking API changes (removed/renamed classes, changed method signatures)
- **MINOR** — New features, backward-compatible additions
- **PATCH** — Bug fixes, documentation updates

The current version is managed in `gradle.properties` as `ftgoCommonLibVersion`.

## Package Mapping

| Legacy (monolith) | New (shared library) |
|--------------------|----------------------|
| `net.chrisrichardson.ftgo.common` | `com.ftgo.common` |
