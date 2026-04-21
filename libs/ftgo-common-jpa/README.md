# ftgo-common-jpa

Shared JPA helpers layered on top of [`ftgo-common`](../ftgo-common).
Ships the `META-INF/orm.xml` mapping descriptor that declares the
framework-light value objects in `ftgo-common` (`Money`, `Address`) as
JPA `@Embeddable`s without forcing those classes to depend on JPA
annotations.

Extracted from the legacy root-level `ftgo-common-jpa/` module as part
of **EM-31**.

## Coordinates

```
group:    net.chrisrichardson.ftgo
artifact: ftgo-common-jpa
version:  1.0.0
```

## Contents

- `META-INF/orm.xml` — embeddable mappings for `Money` and `Address`.
- Transitive `spring-boot-starter-data-jpa` — exposed as `api` so
  consuming services see the same JPA types without re-declaring the
  starter.

## Consuming

```gradle
dependencies {
    implementation "net.chrisrichardson.ftgo:ftgo-common-jpa:1.0.0"
}
```

## Build & publish

```bash
./gradlew :libs:ftgo-common-jpa:build
./gradlew :libs:ftgo-common-jpa:publish
```
