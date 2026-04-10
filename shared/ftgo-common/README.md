# ftgo-common-lib

Standalone shared library extracted from the FTGO monolith's `ftgo-common` module. Provides cross-cutting value objects, Jackson serialization support, and common exception types used by all FTGO microservices.

## Version

**Current version:** `1.0.0`

Follows [Semantic Versioning](https://semver.org/). The version is managed centrally in `gradle.properties` via the `ftgoCommonLibVersion` property.

## Contents

### Value Objects (`@Embeddable`)

| Class | Description |
|-------|-------------|
| `Money` | Wraps `BigDecimal` with arithmetic operations (`add`, `multiply`, `isGreaterThanOrEqual`). JPA `@Embeddable`. |
| `Address` | 5-field address (street1, street2, city, state, zip). JPA `@Embeddable`. |
| `PersonName` | First name / last name. JPA `@Embeddable`. |

### Jackson Serialization

| Class | Description |
|-------|-------------|
| `MoneyModule` | Custom Jackson `SimpleModule` providing serializer/deserializer for `Money` (serializes as plain string). |
| `CommonJsonMapperInitializer` | Spring-managed bean that registers `MoneyModule` and `JavaTimeModule` on the application `ObjectMapper`. |

### Spring Configuration

| Class | Description |
|-------|-------------|
| `CommonConfiguration` | Spring `@Configuration` that provides an `ObjectMapper` bean and initializes JSON mapping. |

### Exceptions

| Class | Description |
|-------|-------------|
| `UnsupportedStateTransitionException` | Thrown when an invalid state transition is attempted (e.g., order lifecycle). |
| `NotYetImplementedException` | Marker exception for unimplemented features. |

## Usage

### Gradle Dependency

Add the following to your microservice's `build.gradle`:

```groovy
dependencies {
    compile "net.chrisrichardson.ftgo:ftgo-common-lib:${ftgoCommonLibVersion}"
}
```

Or reference the shared module directly within the multi-project build:

```groovy
dependencies {
    compile project(':shared:ftgo-common')
}
```

### Example: Using Money

```java
import net.chrisrichardson.ftgo.common.Money;

Money price = new Money("12.99");
Money tax = new Money("1.04");
Money total = price.add(tax);               // 14.03
boolean canAfford = total.isGreaterThanOrEqual(new Money(10)); // true
String display = total.asString();           // "14.03"
```

### Example: Jackson Serialization

```java
import net.chrisrichardson.ftgo.common.MoneyModule;

ObjectMapper mapper = new ObjectMapper();
mapper.registerModule(new MoneyModule());

String json = mapper.writeValueAsString(new Money("9.99")); // "9.99"
Money money = mapper.readValue("\"9.99\"", Money.class);
```

## Building

```bash
./gradlew :shared:ftgo-common:build
```

## Publishing

Publish to the local Maven repository:

```bash
./gradlew :shared:ftgo-common:publish
```

The artifact is published to `build/repo` with coordinates:

```
net.chrisrichardson.ftgo:ftgo-common-lib:1.0.0
```

## Testing

```bash
./gradlew :shared:ftgo-common:test
```

Included tests:
- `MoneyTest` — Unit tests for Money arithmetic (add, multiply, compare, asString)
- `MoneySerializationTest` — Jackson serialization/deserialization round-trip tests

## Package Structure

```
net.chrisrichardson.ftgo.common
├── Address.java
├── CommonConfiguration.java
├── CommonJsonMapperInitializer.java
├── Money.java
├── MoneyModule.java
├── NotYetImplementedException.java
├── PersonName.java
└── UnsupportedStateTransitionException.java
```
