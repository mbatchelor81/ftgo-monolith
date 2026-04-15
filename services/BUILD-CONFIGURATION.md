# FTGO Microservices — Build Configuration Guide

This document explains how the shared Gradle build configuration works and how new
microservices consume it.

## Architecture Overview

```
ftgo-monolith/
├── gradle/libs.versions.toml   # Version catalog — single source of truth for dependency versions
├── build-logic/                 # Included build providing convention plugins
│   ├── settings.gradle
│   ├── build.gradle
│   └── src/main/groovy/
│       ├── ftgo.java-conventions.gradle
│       ├── ftgo.spring-boot-conventions.gradle
│       ├── ftgo.library-conventions.gradle
│       ├── ftgo.testing-conventions.gradle
│       ├── ftgo.docker-conventions.gradle
│       └── ftgo.publishing-conventions.gradle
└── services/                    # Microservices root (independent Gradle build)
    ├── settings.gradle          # Wires up build-logic + version catalog
    ├── gradlew                  # Gradle 8.7 wrapper (separate from the monolith's 4.x wrapper)
    ├── ftgo-common/
    ├── ftgo-common-jpa/
    ├── ftgo-consumer-service/
    ├── ftgo-order-service/
    ├── ftgo-restaurant-service/
    ├── ftgo-courier-service/
    └── ftgo-service-template/
```

The monolith root build (`build.gradle`, `settings.gradle`, `buildSrc/`) is **unchanged**
and continues to use Gradle 4.x with Java 8. The services build is a completely separate
Gradle project that uses Gradle 8.7 with Java 17.

## Version Catalog (`gradle/libs.versions.toml`)

All dependency versions are centralized in a single TOML file. Key versions:

| Dependency                | Version    |
|---------------------------|------------|
| Java                      | 17         |
| Spring Boot               | 3.2.5      |
| Spring Dependency Mgmt    | 1.1.5      |
| Flyway                    | 10.12.0    |
| PostgreSQL Driver         | 42.7.3     |
| Micrometer                | 1.12.5     |
| Jackson BOM               | 2.17.0     |
| JUnit 5                   | 5.10.2     |
| Rest-Assured              | 5.4.0      |
| Testcontainers            | 1.19.7     |
| Jib (Docker images)       | 3.4.2      |

To reference a version in a `build.gradle`:

```groovy
dependencies {
    implementation libs.spring.boot.starter.web     // no version needed
    runtimeOnly libs.postgresql                     // version from catalog
    testImplementation libs.rest.assured             // version from catalog
}
```

## Convention Plugins

### `ftgo.java-conventions`

Base Java configuration applied to all projects:
- Java toolchain targeting version from the catalog (currently 17)
- UTF-8 encoding for compilation and Javadoc
- Compiler flags: `-parameters`, `-Xlint:unchecked`, `-Xlint:deprecation`
- Group set to `com.ftgo`, version `0.0.1-SNAPSHOT`
- Maven Central repository

### `ftgo.spring-boot-conventions`

For deployable Spring Boot microservices. Extends `ftgo.java-conventions` and adds:
- `org.springframework.boot` and `io.spring.dependency-management` plugins
- Jackson BOM and Testcontainers BOM imports
- `buildInfo()` for `/actuator/info` endpoint
- `bootJar` disabled by default (placeholder services have no main class yet)

### `ftgo.library-conventions`

For shared libraries (`ftgo-common`, `ftgo-common-jpa`). Extends `ftgo.java-conventions`,
`ftgo.testing-conventions`, `ftgo.publishing-conventions` and adds:
- `java-library` plugin for proper API/implementation separation
- `io.spring.dependency-management` with Spring Boot BOM
- Jackson BOM import

### `ftgo.testing-conventions`

Standardized test configuration:
- JUnit 5, AssertJ, Mockito, H2 (test runtime)
- JUnit Platform enabled for all test tasks
- Integration test source set (`src/integration-test/java`)
- `integrationTest` task wired into the `check` lifecycle

### `ftgo.docker-conventions`

Container image builds via Google Jib:
- Base image: `eclipse-temurin:17-jre-alpine`
- Image name: `ftgo/<project-name>`
- Tags: version + `latest`
- OCI format with container-aware JVM flags

### `ftgo.publishing-conventions`

Maven publishing configuration:
- Publishes Java component as a Maven artifact
- Local repository at `build/repo`
- Optional remote repository via `MAVEN_PUBLISH_URL` environment variable

## Creating a New Microservice

1. Copy the `ftgo-service-template/` directory:

   ```bash
   cp -r services/ftgo-service-template services/ftgo-my-new-service
   ```

2. Register it in `services/settings.gradle`:

   ```groovy
   include 'ftgo-my-new-service'
   ```

3. The template `build.gradle` is ready to use (18 lines):

   ```groovy
   plugins {
       id 'ftgo.spring-boot-conventions'
       id 'ftgo.testing-conventions'
       id 'ftgo.docker-conventions'
   }

   dependencies {
       implementation project(':ftgo-common')

       implementation libs.spring.boot.starter.web
       implementation libs.spring.boot.starter.data.jpa
       implementation libs.spring.boot.starter.actuator
       implementation libs.spring.boot.starter.validation

       runtimeOnly libs.postgresql
       runtimeOnly libs.flyway.database.postgresql
   }
   ```

4. Once you add a `@SpringBootApplication` main class, enable the boot jar:

   ```groovy
   // Add to the service's build.gradle
   tasks.named('bootJar') { enabled = true }
   tasks.named('jar') { enabled = false }
   ```

5. Add service-specific dependencies only. All common configuration (Java version,
   test framework, compiler flags, Docker setup) is inherited from the convention plugins.

## Creating a Shared Library

1. Create a directory under `services/`:

   ```bash
   mkdir -p services/ftgo-my-lib/src/main/java
   ```

2. Add a `build.gradle`:

   ```groovy
   plugins {
       id 'ftgo.library-conventions'
   }

   dependencies {
       api libs.jackson.databind
       // library-specific dependencies
   }
   ```

3. Register in `services/settings.gradle`:

   ```groovy
   include 'ftgo-my-lib'
   ```

## Building

```bash
# Build all services (from services/ directory)
cd services && ./gradlew clean build

# Build a single service
cd services && ./gradlew :ftgo-consumer-service:build

# Build Docker image for a service
cd services && ./gradlew :ftgo-consumer-service:jibDockerBuild

# Run integration tests
cd services && ./gradlew integrationTest

# The monolith build is completely independent (from repo root)
./gradlew clean build -x :ftgo-end-to-end-tests:build \
  -x :ftgo-end-to-end-tests-common:build
```

## Adding a New Dependency Version

1. Add the version to `gradle/libs.versions.toml` under `[versions]`
2. Add the library coordinates under `[libraries]`
3. Reference it in any `build.gradle` as `libs.<name>`

Example — adding Spring Security:

```toml
# Already in the catalog:
[libraries]
spring-boot-starter-security = { module = "org.springframework.boot:spring-boot-starter-security" }
```

```groovy
// In a service build.gradle:
dependencies {
    implementation libs.spring.boot.starter.security
}
```
