# FTGO Service Template

This is the template directory for creating new FTGO microservices. Copy this
directory to bootstrap a new service, then search-and-replace `SERVICENAME`
with your actual service name (e.g., `payment-service`).

## Quick Start

```bash
# 1. Copy the template
cp -r services/ftgo-service-template services/ftgo-<your-service>

# 2. Rename placeholder packages
find services/ftgo-<your-service> -type d -name SERVICENAME | \
  while read d; do mv "$d" "$(dirname "$d")/<yourservice>"; done

# 3. Search and replace SERVICENAME in all files
find services/ftgo-<your-service> -type f -exec \
  sed -i 's/SERVICENAME/<yourservice>/g' {} +

# 4. Register the new module in settings.gradle
echo 'include "services:ftgo-<your-service>"' >> settings.gradle
```

## Directory Layout

```
ftgo-<service>/
├── build.gradle                        # Gradle build with Spring Boot plugin
├── docker/
│   └── Dockerfile                      # Container image definition
├── k8s/
│   └── deployment.yaml                 # Kubernetes Deployment + Service
├── src/
│   ├── main/
│   │   ├── java/com/ftgo/<service>/
│   │   │   ├── config/                 # Spring @Configuration classes
│   │   │   ├── controller/             # @RestController endpoints
│   │   │   ├── service/                # @Service business logic
│   │   │   ├── repository/             # Spring Data @Repository interfaces
│   │   │   ├── model/                  # JPA @Entity classes
│   │   │   ├── dto/                    # Request/response DTOs
│   │   │   └── exception/             # Custom exceptions + @ControllerAdvice
│   │   └── resources/
│   │       ├── application.yml         # Service configuration
│   │       └── db/migration/           # Flyway SQL migrations
│   └── test/
│       ├── java/com/ftgo/<service>/
│       │   ├── controller/             # Controller integration tests
│       │   ├── service/                # Service unit tests
│       │   └── repository/             # Repository tests
│       └── resources/
│           └── application-test.yml    # Test configuration overrides
└── README.md                           # Service-specific documentation
```

## Conventions

- **Package root**: `com.ftgo.<servicename>` (no hyphens)
- **Gradle module name**: `ftgo-<service-name>` (hyphenated)
- **Docker image**: `ftgo/<service-name>:latest`
- **Database**: `ftgo_<service_name>` (underscored)
- **Kubernetes labels**: `app: ftgo-<service-name>`

See `docs/adr/0001-microservices-repository-structure.md` for the full ADR.
