# FTGO Platform

FTGO ("Food To Go") is a food delivery platform undergoing migration from a Spring Boot monolith to microservices. This repository uses a mono-repo approach where the legacy monolith modules and new microservice modules coexist during the incremental migration.

For the original monolith documentation, see [README.adoc](README.adoc).

## Repository Structure

```
ftgo-monolith/
│
│── services/                               # Microservice modules
│   │
│   ├── ftgo-consumer-service/              # Consumer bounded context
│   │   ├── src/main/java/com/ftgo/consumer/
│   │   │   ├── ConsumerServiceApplication.java
│   │   │   ├── config/                     # Spring @Configuration
│   │   │   ├── domain/                     # Business logic, @Service
│   │   │   ├── web/                        # @RestController endpoints
│   │   │   ├── dto/                        # Request/response DTOs
│   │   │   └── exception/                  # Custom exceptions
│   │   ├── src/main/resources/
│   │   │   └── application.yml
│   │   ├── src/test/java/com/ftgo/consumer/
│   │   ├── docker/Dockerfile
│   │   ├── k8s/deployment.yml
│   │   └── build.gradle
│   ├── ftgo-consumer-service-api/          # Consumer API contracts & DTOs
│   │
│   ├── ftgo-order-service/                 # Order bounded context
│   ├── ftgo-order-service-api/             # Order API contracts & DTOs
│   │
│   ├── ftgo-restaurant-service/            # Restaurant bounded context
│   ├── ftgo-restaurant-service-api/        # Restaurant API contracts & DTOs
│   │
│   ├── ftgo-courier-service/               # Courier bounded context
│   └── ftgo-courier-service-api/           # Courier API contracts & DTOs
│
├── shared/                                 # Shared libraries for microservices
│   ├── ftgo-common/                        # Value objects (Money, Address, PersonName)
│   ├── ftgo-common-jpa/                    # JPA infrastructure utilities
│   └── ftgo-domain/                        # Shared domain entities (transition period)
│
├── docs/                                   # Architecture documentation
│   └── adr/                                # Architecture Decision Records
│       └── 0001-mono-repo-microservices-structure.md
│
├── ftgo-application/                       # LEGACY: Monolith entry point
├── ftgo-common/                            # LEGACY: Shared value objects
├── ftgo-common-jpa/                        # LEGACY: JPA utilities
├── ftgo-domain/                            # LEGACY: Shared entities
├── ftgo-order-service/                     # LEGACY: Order module
├── ftgo-consumer-service/                  # LEGACY: Consumer module
├── ftgo-restaurant-service/                # LEGACY: Restaurant module
├── ftgo-courier-service/                   # LEGACY: Courier module
├── ftgo-flyway/                            # LEGACY: DB migrations
├── ...                                     # Other legacy modules
│
├── settings.gradle                         # Gradle module definitions
├── build.gradle                            # Root build configuration
└── gradle.properties                       # Shared build properties
```

## Bounded Contexts

| Context | Service | API Module | Port |
|---------|---------|-----------|------|
| Consumer | `services/ftgo-consumer-service` | `services/ftgo-consumer-service-api` | 8081 |
| Order | `services/ftgo-order-service` | `services/ftgo-order-service-api` | 8082 |
| Restaurant | `services/ftgo-restaurant-service` | `services/ftgo-restaurant-service-api` | 8083 |
| Courier | `services/ftgo-courier-service` | `services/ftgo-courier-service-api` | 8084 |

## Naming Conventions

### Gradle Module Names

| Type | Pattern | Example |
|------|---------|---------|
| Legacy module | `:<name>` | `:ftgo-order-service` |
| New service | `:services-<name>` | `:services-ftgo-order-service` |
| New service API | `:services-<name>-api` | `:services-ftgo-order-service-api` |
| New shared lib | `:shared-<name>` | `:shared-ftgo-common` |

### Java Package Names

| Component | Pattern | Example |
|-----------|---------|---------|
| Legacy code | `net.chrisrichardson.ftgo.*` | `net.chrisrichardson.ftgo.orderservice` |
| New services | `com.ftgo.<context>.<layer>` | `com.ftgo.order.domain` |
| Shared libs | `com.ftgo.common.*` | `com.ftgo.common` |
| API contracts | `com.ftgo.<context>.api` | `com.ftgo.order.api` |

### Service Directory Layout

Every microservice follows the same internal structure:

```
services/ftgo-<name>-service/
├── src/
│   ├── main/
│   │   ├── java/com/ftgo/<context>/
│   │   │   ├── <Context>ServiceApplication.java   # Spring Boot entry point
│   │   │   ├── config/                             # @Configuration classes
│   │   │   ├── domain/                             # @Service, business logic
│   │   │   ├── web/                                # @RestController endpoints
│   │   │   ├── dto/                                # Request/response objects
│   │   │   └── exception/                          # Custom exceptions
│   │   └── resources/
│   │       └── application.yml
│   └── test/
│       ├── java/com/ftgo/<context>/
│       └── resources/
├── docker/
│   └── Dockerfile
├── k8s/
│   └── deployment.yml
├── build.gradle
└── README.md                                       # (optional) service-specific docs
```

## Building

### Full Build (Legacy + New Modules)

```bash
./gradlew clean build
```

### Build Only New Microservice Modules

```bash
# Build a specific service
./gradlew :services-ftgo-order-service:build

# Build all shared libraries
./gradlew :shared-ftgo-common:build :shared-ftgo-common-jpa:build :shared-ftgo-domain:build
```

### List Available Tasks

```bash
./gradlew tasks
```

## Creating a New Service

To add a new microservice, follow the template structure:

1. Create the directory under `services/`:
   ```
   services/ftgo-<name>-service/
   services/ftgo-<name>-service-api/
   ```

2. Add the standard directory layout (see above).

3. Create `build.gradle` for the service:
   ```groovy
   plugins {
       id 'org.springframework.boot'
       id 'io.spring.dependency-management'
       id 'java'
   }

   group = 'com.ftgo'
   version = '0.0.1-SNAPSHOT'
   sourceCompatibility = '1.8'

   dependencies {
       implementation project(':shared-ftgo-common')
       implementation project(':shared-ftgo-domain')
       implementation project(':services-ftgo-<name>-service-api')

       implementation 'org.springframework.boot:spring-boot-starter-web'
       implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
       implementation 'org.springframework.boot:spring-boot-starter-actuator'
       implementation 'org.springframework.boot:spring-boot-starter-validation'

       runtimeOnly 'mysql:mysql-connector-java'

       testImplementation 'org.springframework.boot:spring-boot-starter-test'
   }

   test {
       useJUnitPlatform()
   }
   ```

4. Create `build.gradle` for the API module:
   ```groovy
   plugins {
       id 'java-library'
   }

   group = 'com.ftgo'
   version = '0.0.1-SNAPSHOT'
   sourceCompatibility = '1.8'

   dependencies {
       api project(':shared-ftgo-common')
   }
   ```

5. Register in `settings.gradle`:
   ```groovy
   include "services-ftgo-<name>-service"
   project(":services-ftgo-<name>-service").projectDir = file("services/ftgo-<name>-service")

   include "services-ftgo-<name>-service-api"
   project(":services-ftgo-<name>-service-api").projectDir = file("services/ftgo-<name>-service-api")
   ```

## Architecture Decision Records

ADRs are stored in `docs/adr/` and follow the numbering convention `NNNN-<slug>.md`.

| ADR | Title | Status |
|-----|-------|--------|
| [0001](docs/adr/0001-mono-repo-microservices-structure.md) | Mono-Repo Structure for Microservices Migration | Accepted |

## Learn More

- [Microservices Patterns](https://microservices.io/book) by Chris Richardson
- [Original FTGO Application](https://github.com/microservices-patterns/ftgo-application)
- [Refactoring to Microservices](https://microservices.io/refactoring/index.html)
