# Shared Gradle Configuration for FTGO Microservices

This document explains how the FTGO project uses a **Gradle version catalog** and
**convention plugins** to standardize build configuration across all microservices.

---

## Overview

| Concern | Mechanism | Location |
|---|---|---|
| Dependency versions | Version catalog | `gradle/libs.versions.toml` |
| Java compilation | Convention plugin | `buildSrc/.../ftgo.java-conventions.gradle` |
| Spring Boot setup | Convention plugin | `buildSrc/.../ftgo.spring-boot-conventions.gradle` |
| Testing (JUnit 5) | Convention plugin | `buildSrc/.../ftgo.testing-conventions.gradle` |
| Docker images (Jib) | Convention plugin | `buildSrc/.../ftgo.docker-conventions.gradle` |
| Maven publishing | Convention plugin | `buildSrc/.../ftgo.publishing-conventions.gradle` |
| All-in-one service | Composite plugin | `buildSrc/.../FtgoMicroservicePlugin.groovy` |

New microservice modules target **Java 17** and **Spring Boot 3.2.x**.
Legacy monolith modules remain on **Java 8** and **Spring Boot 2.0.x**.

---

## Version Catalog (`gradle/libs.versions.toml`)

Gradle 8.5 automatically loads `gradle/libs.versions.toml` as the default `libs`
catalog. No explicit registration in `settings.gradle` is required.

### Using catalog entries in `build.gradle`

```groovy
dependencies {
    // Single library
    implementation libs.spring.boot.starter.web
    implementation libs.micrometer.prometheus

    // Bundle (group of related libraries)
    testImplementation libs.bundles.testing
    testImplementation libs.bundles.rest.assured
}
```

### Key versions

| Library | Version | Catalog key |
|---|---|---|
| Spring Boot | 3.2.5 | `spring-boot` |
| Micrometer | 1.12.4 | `micrometer` |
| JUnit Jupiter | 5.10.2 | `junit-jupiter` |
| Rest-Assured | 5.4.0 | `rest-assured` |
| Flyway | 10.10.0 | `flyway` |
| Jackson | 2.17.0 | `jackson` |
| MySQL Connector | 8.3.0 | `mysql-connector` |

---

## Convention Plugins

Convention plugins live in `buildSrc/src/main/groovy/` as precompiled script
plugins. They are available to every project in the build without any extra
configuration.

### `ftgo.java-conventions`

Base plugin for all new modules. Provides:

- Java 17 source/target compatibility with toolchain
- UTF-8 encoding
- Compiler flags: `-parameters`, `-Xlint:deprecation`, `-Xlint:unchecked`
- `group = 'com.ftgo'`
- Standard JAR manifest attributes

**Apply to:** API modules, shared libraries, any module that needs Java compilation.

```groovy
plugins {
    id 'ftgo.java-conventions'
}
```

### `ftgo.spring-boot-conventions`

Extends `ftgo.java-conventions` with Spring Boot support:

- Applies `org.springframework.boot` and `io.spring.dependency-management` plugins
- Spring Boot dependency management (BOM) — no explicit versions needed for
  Spring Boot starters
- `bootJar` disabled by default (enable in runnable services)
- Adds `spring-boot-starter-validation` and `spring-boot-configuration-processor`

```groovy
plugins {
    id 'ftgo.spring-boot-conventions'
}
```

### `ftgo.testing-conventions`

Extends `ftgo.java-conventions` with testing infrastructure:

- JUnit 5 (`useJUnitPlatform()`)
- `integrationTest` source set and task
- Dependencies: `spring-boot-starter-test`, `junit-jupiter`, Rest-Assured bundle
- Test logging and fail-fast in CI (`CI` env var)

```groovy
plugins {
    id 'ftgo.testing-conventions'
}
```

### `ftgo.docker-conventions`

Extends `ftgo.java-conventions` with Docker image builds via Google Jib:

- Base image: `eclipse-temurin:17-jre-alpine`
- Image naming: `ftgo/${project.name}:latest`
- JVM flags: `-XX:+UseContainerSupport`, `-XX:MaxRAMPercentage=75.0`
- Exposes port 8080
- OCI image format with standard labels

```groovy
plugins {
    id 'ftgo.docker-conventions'
}
```

### `ftgo.publishing-conventions`

Extends `ftgo.java-conventions` with Maven publishing:

- Generates Javadoc and sources JARs
- Publishes to local Maven repository (`build/repo`)
- POM metadata with Apache 2.0 license

```groovy
plugins {
    id 'ftgo.publishing-conventions'
}
```

### `FtgoMicroservicePlugin` (composite)

Combines the three most common plugins for a runnable microservice:

- `ftgo.spring-boot-conventions`
- `ftgo.testing-conventions`
- `ftgo.docker-conventions`

```groovy
apply plugin: FtgoMicroservicePlugin
```

---

## Creating a New Microservice

### 1. Service module (`services/ftgo-<name>-service/build.gradle`)

```groovy
// ftgo-<name>-service — Microservice build configuration

apply plugin: FtgoMicroservicePlugin

dependencies {
    implementation project(":services:ftgo-<name>-service-api")
    implementation project(":shared:ftgo-common")
    implementation project(":shared:ftgo-domain")

    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation libs.micrometer.prometheus

    testImplementation project(":shared:ftgo-test-util")
}
```

> **Target: < 30 lines.** Only service-specific dependencies appear here.

### 2. API module (`services/ftgo-<name>-service-api/build.gradle`)

```groovy
// ftgo-<name>-service-api — API contracts and DTOs

plugins {
    id 'ftgo.java-conventions'
    id 'ftgo.publishing-conventions'
}

dependencies {
    implementation project(":shared:ftgo-common")
}
```

### 3. Register in `settings.gradle`

```groovy
include 'services:ftgo-<name>-service'
include 'services:ftgo-<name>-service-api'
```

---

## Shared Library Modules

Shared libraries under `shared/` apply convention plugins directly:

```groovy
// shared/ftgo-common/build.gradle
plugins {
    id 'ftgo.spring-boot-conventions'
    id 'ftgo.testing-conventions'
    id 'ftgo.publishing-conventions'
}

dependencies {
    api libs.jackson.core
    api libs.jackson.databind
    // ...
}
```

---

## Legacy vs. New Modules

The root `build.gradle` separates configuration for legacy and new modules:

- **Legacy modules** (root-level, e.g., `ftgo-order-service`, `ftgo-common`):
  `java-library` plugin, Java 8, Spring Boot 2.x via `gradle.properties` versions.
- **New modules** (under `services/` and `shared/`): Convention plugins,
  Java 17, Spring Boot 3.x via version catalog.

Legacy modules are not modified during the migration. They will be removed
once all services are extracted.

---

## Build Commands

```bash
# Compile all modules (legacy + new)
./gradlew testClasses

# Build a specific new service
./gradlew :services:ftgo-order-service:build

# Run tests for a new service
./gradlew :services:ftgo-order-service:test

# Run integration tests
./gradlew :services:ftgo-order-service:integrationTest

# Build Docker image for a service
./gradlew :services:ftgo-order-service:jibDockerBuild

# Publish a shared library to local Maven repo
./gradlew :shared:ftgo-common:publishToMavenLocal
```

---

## Directory Structure

```
ftgo-monolith/
├── gradle/
│   └── libs.versions.toml          # Version catalog (auto-loaded by Gradle 8.5)
├── buildSrc/
│   ├── build.gradle                 # Plugin dependencies (Spring Boot, Jib, etc.)
│   └── src/main/groovy/
│       ├── ftgo.java-conventions.gradle
│       ├── ftgo.spring-boot-conventions.gradle
│       ├── ftgo.testing-conventions.gradle
│       ├── ftgo.docker-conventions.gradle
│       ├── ftgo.publishing-conventions.gradle
│       ├── FtgoMicroservicePlugin.groovy   # Composite plugin for services
│       ├── FtgoServicePlugin.groovy        # Legacy monolith plugin
│       ├── IntegrationTestsPlugin.groovy   # Legacy integration test plugin
│       └── WaitForMySqlPlugin.groovy       # Legacy MySQL wait plugin
├── services/
│   ├── ftgo-order-service/
│   ├── ftgo-order-service-api/
│   ├── ftgo-consumer-service/
│   ├── ftgo-consumer-service-api/
│   ├── ftgo-restaurant-service/
│   ├── ftgo-restaurant-service-api/
│   ├── ftgo-courier-service/
│   ├── ftgo-courier-service-api/
│   └── _service-template/
├── shared/
│   ├── ftgo-common/
│   ├── ftgo-common-jpa/
│   ├── ftgo-domain/
│   ├── ftgo-test-util/
│   └── common-swagger/
└── docs/
    └── gradle-shared-config.md      # This file
```
