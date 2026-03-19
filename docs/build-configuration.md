# FTGO Shared Build Configuration

This document describes the shared Gradle build configuration that standardises
dependency management, build settings, and plugin versions across all new
microservice modules.

## Architecture Overview

```
ftgo-monolith/
├── build-logic/                        # Included build — convention plugins
│   ├── settings.gradle                 # Imports the version catalog
│   ├── build.gradle                    # Plugin dependencies
│   └── src/main/groovy/
│       ├── ftgo.java-conventions.gradle
│       ├── ftgo.spring-boot-conventions.gradle
│       ├── ftgo.testing-conventions.gradle
│       ├── ftgo.docker-conventions.gradle
│       └── ftgo.publishing-conventions.gradle
├── gradle/
│   └── libs.versions.toml             # Version catalog
├── buildSrc/                           # Legacy plugins (WaitForMySql, etc.)
├── build.gradle                        # Root — legacy + new module config
├── gradle.properties                   # Legacy version properties
└── settings.gradle                     # Includes build-logic
```

## Version Catalog (`gradle/libs.versions.toml`)

All dependency versions for new microservice modules are centralised in the
[Gradle Version Catalog](https://docs.gradle.org/current/userguide/platforms.html).

| Category | Key Libraries | Version |
|---|---|---|
| Spring Boot | `spring-boot-starter-*` | 3.2.5 |
| Java | Target / Toolchain | 17 |
| Jackson | `jackson-bom` | 2.17.0 |
| Flyway | `flyway-core`, `flyway-mysql` | 9.22.3 |
| MySQL | `mysql-connector-j` | 8.3.0 |
| Micrometer | `micrometer-registry-prometheus` | 1.12.5 |
| JUnit | `junit-jupiter` | 5.10.2 |
| Rest-Assured | `rest-assured`, `spring-mock-mvc` | 5.4.0 |
| Mockito | `mockito-core`, `mockito-junit-jupiter` | 5.11.0 |
| AssertJ | `assertj-core` | 3.25.3 |
| Jib (Docker) | `jib-gradle-plugin` | 3.4.1 |

### Using Version Catalog References

In module `build.gradle` files, reference libraries via the `libs` accessor:

```groovy
dependencies {
    implementation libs.bundles.spring.boot.service   // web, jpa, actuator, validation
    implementation libs.bundles.jackson                // core, databind, jsr310
    runtimeOnly    libs.mysql.connector
    testImplementation libs.bundles.testing            // spring-boot-test, junit5, mockito, assertj
}
```

## Convention Plugins

Convention plugins live in `build-logic/` and are applied via the `plugins` block
in each module's `build.gradle`.

### `ftgo.java-conventions`

Base plugin for all new modules. Provides:

- Java 17 source/target compatibility and toolchain
- UTF-8 encoding for compilation and Javadoc
- `-parameters`, `-Xlint:unchecked`, `-Xlint:deprecation` compiler args
- Spring Boot BOM + Jackson BOM dependency management

### `ftgo.spring-boot-conventions`

For Spring Boot service modules (executable JARs). Extends `ftgo.java-conventions`
and adds:

- `org.springframework.boot` plugin (bootJar, bootRun tasks)

### `ftgo.testing-conventions`

Adds JUnit 5 test platform configuration and common test dependencies:

- JUnit Jupiter, Mockito, AssertJ, Rest-Assured
- `useJUnitPlatform()`
- Test logging with pass/skip/fail events
- ZGC and 512 MB heap for test JVMs

### `ftgo.docker-conventions`

Configures Docker image building via [Google Jib](https://github.com/GoogleContainerTools/jib):

- Base image: `eclipse-temurin:17-jre-alpine`
- Image name: `ftgo/<project-name>`
- Tags: `<version>` + `latest`
- OCI format, 8080 port exposed

### `ftgo.publishing-conventions`

For shared library modules. Extends `java-library` and adds:

- Maven publishing with sources and javadoc JARs
- Local repository publication to `build/repo`
- POM metadata (name, description, url)

## How to Create a New Microservice

### 1. Create the module directory

```bash
mkdir -p services/ftgo-my-service/src/main/java/com/ftgo/myservice
mkdir -p services/ftgo-my-service/src/test/java/com/ftgo/myservice
```

### 2. Add `build.gradle`

```groovy
plugins {
    id 'ftgo.spring-boot-conventions'
    id 'ftgo.testing-conventions'
    id 'ftgo.docker-conventions'
}

version = '0.0.1-SNAPSHOT'

dependencies {
    implementation project(':shared-ftgo-common')
    implementation project(':shared-ftgo-domain')

    implementation libs.bundles.spring.boot.service
    runtimeOnly libs.mysql.connector
}
```

This is typically **< 20 lines** — only service-specific dependencies and
inter-project references are needed.

### 3. Register in `settings.gradle`

```groovy
include "services-ftgo-my-service"
project(":services-ftgo-my-service").projectDir = file("services/ftgo-my-service")
```

### 4. Build

```bash
./gradlew :services-ftgo-my-service:build
```

## How to Create a New Shared Library

### 1. Create the module directory

```bash
mkdir -p shared/ftgo-my-lib/src/main/java/com/ftgo/mylib
```

### 2. Add `build.gradle`

```groovy
plugins {
    id 'ftgo.java-conventions'
    id 'ftgo.publishing-conventions'
}

version = '0.0.1-SNAPSHOT'

dependencies {
    api project(':shared-ftgo-common')
}
```

### 3. Register in `settings.gradle`

```groovy
include "shared-ftgo-my-lib"
project(":shared-ftgo-my-lib").projectDir = file("shared/ftgo-my-lib")
```

## How to Add a New Dependency

1. Add the version to `gradle/libs.versions.toml` under `[versions]`
2. Add the library under `[libraries]` with a `version.ref`
3. Optionally add it to a `[bundles]` group
4. Reference it in module `build.gradle` files via `libs.<alias>`

Example — adding Spring Security:

```toml
# In libs.versions.toml
[versions]
springSecurity = "6.2.4"

[libraries]
spring-security-web = { module = "org.springframework.security:spring-security-web", version.ref = "springSecurity" }
```

```groovy
// In a service build.gradle
dependencies {
    implementation libs.spring.security.web
}
```

## Legacy Module Compatibility

Legacy monolith modules (`ftgo-application`, `ftgo-order-service`, etc.) are
**not** affected by the convention plugins. They continue to:

- Use Java 1.8
- Use Spring Boot 2.0.3.RELEASE dependencies
- Use the `compile` / `testCompile` / `runtime` configurations (re-registered
  for backward compatibility in the root `build.gradle`)

The Gradle wrapper was upgraded to **8.5** (from 4.10.2) to support version
catalogs. The Spring Boot Gradle plugin version in the buildscript classpath
was upgraded to 2.7.18 for Gradle 8 compatibility, but legacy modules still
resolve dependencies against the 2.0.3 BOM.
