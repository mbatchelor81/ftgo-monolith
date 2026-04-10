# ftgo-common-jpa — Shared JPA Configuration Library

Shared JPA ORM mappings and configuration for FTGO microservices, extracted from the monolith's `ftgo-common-jpa` module.

## Contents

### ORM Mappings (`META-INF/orm.xml`)
Provides JPA ORM mappings for shared value objects:
- `net.chrisrichardson.ftgo.common.Money` — mapped as `@Embeddable` with `amount` column
- `net.chrisrichardson.ftgo.common.Address` — mapped as `@Embeddable` with `street1`, `street2`, `city`, `state`, `zip` columns

### Configuration (`JpaConfiguration`)
Spring `@Configuration` class that enables JPA auto-configuration and imports `CommonConfiguration` from `ftgo-common`.

## Usage

Add as a project dependency in your `build.gradle`:

```groovy
dependencies {
    implementation project(':shared:ftgo-common-jpa')
}
```

Or, once published:

```groovy
dependencies {
    implementation 'net.chrisrichardson.ftgo:ftgo-common-jpa:1.0.0'
}
```

This transitively brings in `ftgo-common` and `spring-boot-starter-data-jpa`.

## Publishing

```bash
./gradlew :shared:ftgo-common-jpa:publish
```

Artifacts are published to the local Maven repository at `build/repo`.
