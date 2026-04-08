# FTGO Service Template

This is a template for creating new FTGO microservices. Follow the steps below to scaffold a new service.

## How to Use This Template

### 1. Copy the template directories

```bash
# Replace <service-name> with your service name (e.g., "payment")
cp -r services/_service-template services/ftgo-<service-name>-service
cp -r services/_service-template-api services/ftgo-<service-name>-service-api
```

### 2. Rename packages

Rename the `com.ftgo.template_` package directories to `com.ftgo.<servicename>`:

```bash
# In the service module
mv services/ftgo-<service-name>-service/src/main/java/com/ftgo/template_ \
   services/ftgo-<service-name>-service/src/main/java/com/ftgo/<servicename>

# In the API module
mv services/ftgo-<service-name>-service-api/src/main/java/com/ftgo/template_ \
   services/ftgo-<service-name>-service-api/src/main/java/com/ftgo/<servicename>
```

### 3. Replace placeholders

Search and replace `CHANGEME` with your service name in:
- `build.gradle` (both service and API modules)
- `docker/Dockerfile`
- `k8s/deployment.yaml`
- `src/main/resources/application.yml`
- `config/application-local.yml`

### 4. Register in settings.gradle

Add to the root `settings.gradle`:

```groovy
include "services:ftgo-<service-name>-service"
include "services:ftgo-<service-name>-service-api"
```

### 5. Set up the database

Create a MySQL database for your service:

```sql
CREATE DATABASE ftgo_<service-name>_service;
```

Add Flyway migration scripts under `src/main/resources/db/migration/`.

## Directory Structure

```
ftgo-<service-name>-service/
├── build.gradle                    # Gradle build configuration
├── config/
│   └── application-local.yml       # Local dev profile overrides
├── docker/
│   └── Dockerfile                  # Container image definition
├── k8s/
│   └── deployment.yaml             # Kubernetes Deployment + Service
└── src/
    ├── main/
    │   ├── java/com/ftgo/<svc>/
    │   │   ├── config/             # @Configuration classes
    │   │   ├── domain/             # JPA @Entity classes
    │   │   ├── messaging/          # Event publishers/consumers
    │   │   ├── repository/         # Spring Data @Repository interfaces
    │   │   ├── service/            # @Service business logic
    │   │   └── web/                # @RestController endpoints
    │   └── resources/
    │       └── application.yml     # Spring Boot configuration
    ├── test/
    │   ├── java/com/ftgo/<svc>/    # Unit tests (mirrors main structure)
    │   └── resources/
    └── integration-test/
        ├── java/com/ftgo/<svc>/    # Integration tests
        └── resources/
```

## Conventions

See [docs/adr/0001-repository-structure-and-naming-conventions.md](../../docs/adr/0001-repository-structure-and-naming-conventions.md) for full conventions.
