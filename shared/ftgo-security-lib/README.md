# ftgo-security-lib

Shared Spring Security configuration library for FTGO microservices.

## Overview

Provides a consistent security baseline for all FTGO microservices:

- **SecurityFilterChain** — default authentication/authorization rules
- **CORS configuration** — externalized via `ftgo.security.cors.*` properties
- **Actuator security** — health/info public, all others secured
- **CSRF disabled** — appropriate for stateless REST APIs
- **JSON error responses** — structured 401/403 responses
- **Security logging filter** — audit request/response metadata
- **Utility classes** — `SecurityContextUtils`, `RequestUtils`

## Usage

Add the dependency in your service's `build.gradle`:

```groovy
dependencies {
    implementation project(":shared:ftgo-security-lib")
}
```

The auto-configuration is picked up automatically via Spring Boot's
`AutoConfiguration.imports` mechanism.

## Configuration

See `application-security.yml` for default property values, or
`docs/security.md` for full documentation.
