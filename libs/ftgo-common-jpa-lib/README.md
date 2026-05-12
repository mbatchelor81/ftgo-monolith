# ftgo-common-jpa-lib

Standalone, versioned shared library extracted from the `ftgo-common-jpa` monolith module. Provides JPA ORM mappings for shared value objects (`Money`, `Address`) used across FTGO services.

## Version

Current version: **1.0.0** (managed in `gradle.properties` as `ftgoCommonJpaLibVersion`)

## Contents

### JPA ORM Mappings (`META-INF/orm.xml`)

| Embeddable | Description |
|------------|-------------|
| `Money` | Maps `BigDecimal amount` field to a `amount` column. |
| `Address` | Maps `street1`, `street2`, `city`, `state`, `zip` fields to their respective columns. |

These XML mappings allow the `Money` and `Address` value objects from `ftgo-common-lib` to be persisted as JPA `@Embeddable` types without requiring JPA annotations directly on the value object classes.

## Usage

### Gradle Dependency

```groovy
// In a microservice build.gradle
dependencies {
    implementation project(':ftgo-common-jpa-lib')
}
```

Once published to a Maven repository:

```groovy
dependencies {
    implementation 'net.chrisrichardson.ftgo:ftgo-common-jpa-lib:1.0.0'
}
```

### Publishing

Publish to the local project repository:

```bash
./gradlew :ftgo-common-jpa-lib:publish
```

Publish to Maven Local (`~/.m2/repository`):

```bash
./gradlew :ftgo-common-jpa-lib:publishToMavenLocal
```

## Dependencies

- `ftgo-common-lib` (value objects: `Money`, `Address`, `PersonName`)
- `spring-boot-starter-data-jpa` (JPA/Hibernate runtime)

## Relationship to `ftgo-common-jpa`

This library is a versioned extraction of the legacy `ftgo-common-jpa` module. The original module remains in place for backward compatibility with existing monolith modules. New microservices under `services/` should depend on `ftgo-common-jpa-lib` instead.
