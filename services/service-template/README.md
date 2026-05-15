# Service Template (Archetype)

Use this template to bootstrap a new FTGO microservice. Copy the entire
`service-template/` directory and rename it for your bounded context.

## Quick Start

```bash
# 1. Copy the template
cp -r services/service-template services/<context>-service

# 2. Rename directories
mv services/<context>-service/service-template-api services/<context>-service/<context>-service-api
mv services/<context>-service/service-template-app services/<context>-service/<context>-service-app

# 3. Rename packages
#    Replace com.ftgo.template with com.ftgo.<context> in all Java files
find services/<context>-service -name '*.java' -exec sed -i 's/com\.ftgo\.template/com.ftgo.<context>/g' {} +

# 4. Register in settings.gradle (see the microservices section)
#    include '<context>-service-app'
#    project(':<context>-service-app').projectDir = file('services/<context>-service/<context>-service-app')
#    include '<context>-service-api'
#    project(':<context>-service-api').projectDir = file('services/<context>-service/<context>-service-api')

# 5. Update build.gradle files with correct dependencies
```

## Directory Layout

```
<context>-service/
├── <context>-service-api/              # API contracts shared with other services
│   ├── src/main/java/com/ftgo/<context>/api/
│   │   ├── Create<Context>Request.java
│   │   ├── Create<Context>Response.java
│   │   └── <Context>Event.java
│   └── build.gradle
├── <context>-service-app/              # Service implementation
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/ftgo/<context>/
│   │   │   │   ├── config/            # @Configuration classes
│   │   │   │   ├── controller/        # @RestController endpoints
│   │   │   │   ├── domain/            # @Entity classes, aggregates
│   │   │   │   ├── repository/        # Spring Data repositories
│   │   │   │   ├── service/           # Business logic
│   │   │   │   └── <Context>ServiceApplication.java
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── db/migration/      # Flyway migrations (service-specific)
│   │   └── test/
│   │       └── java/com/ftgo/<context>/
│   ├── docker/
│   │   └── Dockerfile
│   ├── k8s/
│   │   ├── deployment.yaml
│   │   └── service.yaml
│   └── build.gradle
└── README.md
```

## Migration Notes

During the transitional period, services being extracted from the monolith may
need to depend on `:ftgo-domain` to access shared JPA entities that have not
yet been duplicated into the service's own `domain` package. Add this to your
`build.gradle` as needed:

```groovy
compile project(":ftgo-domain")
```

Once the service's own domain entities are complete, remove the `:ftgo-domain`
dependency to achieve full decoupling.

## Conventions

- See `docs/conventions/package-naming.md` for package and class naming rules.
- See `docs/adr/0001-mono-repo-with-service-boundaries.md` for the repo
  structure rationale.
