# FTGO Platform — Security Architecture

## Overview

The `ftgo-security-lib` provides a shared Spring Security foundation for all
FTGO platform microservices. It ships as a Spring Boot auto-configuration
library that services include as a Gradle dependency.

## Module Location

```
libs/ftgo-security-lib/
├── build.gradle
└── src/
    ├── main/java/com/ftgo/security/
    │   ├── config/
    │   │   ├── FtgoSecurityAutoConfiguration.java   # SecurityFilterChain bean
    │   │   ├── FtgoSecurityProperties.java          # Externalized config
    │   │   └── CorsFilterConfiguration.java         # CORS source bean
    │   ├── exception/
    │   │   └── SecurityExceptionHandlers.java        # JSON 401/403 responses
    │   └── util/
    │       └── SecurityUtils.java                    # Context helpers
    ├── main/resources/META-INF/spring/
    │   └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
    └── test/
```

## Quick Start

Add the dependency to a microservice's `build.gradle`:

```groovy
dependencies {
    implementation project(':ftgo-security-lib')
}
```

That's it — the auto-configuration activates automatically.

## Default Behavior

| Aspect | Default |
|--------|---------|
| Session management | Stateless (`STATELESS`) |
| CSRF | Disabled (stateless APIs) |
| CORS | All origins, standard HTTP methods |
| `/actuator/health` and `/actuator/health/**` | Public (includes K8s liveness/readiness probes) |
| `/actuator/info` | Public |
| `/actuator/**` (other) | Authenticated |
| All other endpoints | Authenticated |
| Authentication scheme | HTTP Basic |
| Error responses | JSON (timestamp, status, error, message, path) |

## Configuration Properties

All properties live under the `ftgo.security` prefix.

```yaml
ftgo:
  security:
    # Disable security entirely (useful for local development)
    enabled: true

    # Endpoints that do not require authentication
    public-paths:
      - /actuator/health
      - /actuator/info
      - /api/public/**

    cors:
      allowed-origins:
        - https://app.ftgo.com
      allowed-methods:
        - GET
        - POST
        - PUT
        - DELETE
        - PATCH
        - OPTIONS
      allowed-headers:
        - "*"
      exposed-headers: []
      allow-credentials: false
      max-age: 3600
```

### Per-Service Overrides

Each service can override the defaults in its own `application.yml`.
For example, to expose an additional public path:

```yaml
ftgo:
  security:
    public-paths:
      - /actuator/health
      - /actuator/health/**
      - /actuator/info
      - /api/v1/restaurants   # service-specific public endpoint
```

### Disabling Security

Set `ftgo.security.enabled=false` to replace the secured filter chain with
a permit-all chain (all endpoints open, CSRF disabled). This is intended
**only** for local development and testing:

```yaml
# application-local.yml
ftgo:
  security:
    enabled: false
```

## Overriding Beans

Any service can replace the library's beans by declaring its own:

```java
@Configuration
public class CustomSecurityConfig {

    @Bean
    public SecurityFilterChain customFilterChain(HttpSecurity http) throws Exception {
        // service-specific security rules
        return http.build();
    }
}
```

Because the library uses `@ConditionalOnMissingBean`, the service's bean
takes precedence.

## Security Profiles

Recommended profile strategy:

| Profile | Purpose |
|---------|---------|
| `local` | `ftgo.security.enabled=false` — open access for development |
| `dev` | Security enabled, relaxed CORS (all origins) |
| `staging` | Security enabled, restricted CORS |
| `prod` | Security enabled, strict CORS, credentials allowed |

Example activation:

```bash
java -jar service.jar --spring.profiles.active=prod
```

## Error Responses

Authentication and authorization failures return structured JSON:

```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "path": "/api/orders"
}
```

## Utility Classes

`SecurityUtils` provides static helpers for querying the security context:

```java
Optional<String> user = SecurityUtils.currentUsername();
boolean isAdmin = SecurityUtils.hasRole("ADMIN");
boolean authed = SecurityUtils.isAuthenticated();
```

## Version Catalog

Spring Security dependencies are declared in `gradle/libs.versions.toml`:

```toml
[versions]
spring-security = "6.2.4"

[libraries]
spring-boot-starter-security = { module = "org.springframework.boot:spring-boot-starter-security" }
spring-security-test = { module = "org.springframework.security:spring-security-test", version.ref = "spring-security" }

[bundles]
testing-security = ["spring-security-test", "spring-boot-starter-test"]
```

## Future Work

- JWT token authentication (replace HTTP Basic)
- Role-based access control annotations
- Service-to-service authentication (mTLS / API keys)
- Rate limiting integration
- Audit logging for security events
