# FTGO Build Conventions

## Overview

The FTGO platform uses a **dual build system** during the migration from monolith to microservices:

| Concern | Legacy Monolith (`ftgo-*`) | New Microservices (`services/*`) |
|---------|---------------------------|----------------------------------|
| Java version | 8 | 17+ |
| Spring Boot | 2.0.3.RELEASE | 3.2.x |
| Build plugins | `buildSrc/` (Groovy) | `build-logic/` (convention plugins) |
| Dependency versions | `gradle.properties` | `gradle/libs.versions.toml` |
| Test framework | JUnit 4 | JUnit 5 (Jupiter) |

## Version Catalog

All dependency versions for new microservices are centralized in
[`gradle/libs.versions.toml`](../../gradle/libs.versions.toml).

### Using the catalog in `build.gradle`

```groovy
plugins {
    id 'ftgo.spring-boot-conventions'
    id 'ftgo.testing-conventions'
}

dependencies {
    implementation libs.spring.boot.starter.web
    implementation libs.spring.boot.starter.data.jpa
    implementation libs.micrometer.registry.prometheus

    testImplementation libs.bundles.testing
}
```

### Using bundled dependencies

The catalog defines convenience bundles:

| Bundle | Contents |
|--------|----------|
| `libs.bundles.testing` | JUnit 5, Rest-Assured, Mockito, AssertJ |
| `libs.bundles.spring.boot.service` | Web, JPA, Actuator, Validation starters |

## Convention Plugins

Convention plugins live in [`build-logic/`](../../build-logic/) and are included
via `includeBuild('build-logic')` in `settings.gradle`.

### Available Plugins

| Plugin ID | Purpose | Typical consumer |
|-----------|---------|-----------------|
| `ftgo.java-conventions` | Java 17, UTF-8, `-parameters`, `-Xlint` | All new modules |
| `ftgo.spring-boot-conventions` | Spring Boot plugin + dependency management | Deployable service apps |
| `ftgo.testing-conventions` | JUnit 5, Rest-Assured, integration test source set | All new modules |
| `ftgo.docker-conventions` | Jib image build (extends spring-boot-conventions) | Deployable services |
| `ftgo.publishing-conventions` | Maven publishing with sources & javadoc JARs | Shared libraries / API modules |

### Plugin Dependency Graph

```
ftgo.docker-conventions
  └── ftgo.spring-boot-conventions
        └── ftgo.java-conventions

ftgo.testing-conventions
  └── ftgo.java-conventions

ftgo.publishing-conventions
  └── ftgo.java-conventions
```

## Creating a New Microservice

### 1. Create the directory structure

```
services/
  my-service/
    my-service-api/
      build.gradle
      src/main/java/...
    my-service-app/
      build.gradle
      src/main/java/...
```

### 2. Register in `settings.gradle`

```groovy
// My Service
include "my-service-api"
project(":my-service-api").projectDir = file("services/my-service/my-service-api")
include "my-service-app"
project(":my-service-app").projectDir = file("services/my-service/my-service-app")
```

### 3. API module `build.gradle` (< 10 lines)

```groovy
plugins {
    id 'ftgo.java-conventions'
    id 'ftgo.publishing-conventions'
}

dependencies {
    api project(":ftgo-common")
}
```

### 4. App module `build.gradle` (< 30 lines)

```groovy
plugins {
    id 'ftgo.spring-boot-conventions'
    id 'ftgo.testing-conventions'
    id 'ftgo.docker-conventions'
}

dependencies {
    implementation project(":my-service-api")
    implementation project(":ftgo-common")
    implementation project(":ftgo-common-jpa")

    implementation libs.bundles.spring.boot.service
    implementation libs.micrometer.registry.prometheus
    implementation libs.flyway.core
    implementation libs.flyway.mysql
    implementation libs.mysql.connector

    testImplementation libs.spring.boot.starter.test
    testImplementation libs.bundles.testing
}
```

### 5. Build and test

```bash
# Build only your service
./gradlew :my-service-app:build

# Build Docker image (no Docker daemon required)
./gradlew :my-service-app:jibDockerBuild

# Run integration tests
./gradlew :my-service-app:integrationTest
```

## Legacy Modules

Legacy `ftgo-*` modules continue to use:
- `buildSrc/` plugins (`FtgoServicePlugin`, `IntegrationTestsPlugin`, `WaitForMySqlPlugin`)
- Version properties from `gradle.properties`
- Java 8 source/target compatibility

These modules are **not affected** by the convention plugins and will be migrated
incrementally in future iterations.

## Adding a New Dependency

1. Add the version to `[versions]` in `gradle/libs.versions.toml`
2. Add the library alias to `[libraries]`
3. Optionally add it to a `[bundles]` group
4. Reference it as `libs.<alias>` in your `build.gradle`

Example:

```toml
# In gradle/libs.versions.toml
[versions]
caffeine = "3.1.8"

[libraries]
caffeine = { module = "com.github.ben-manes.caffeine:caffeine", version.ref = "caffeine" }
```

```groovy
// In build.gradle
dependencies {
    implementation libs.caffeine
}
```
