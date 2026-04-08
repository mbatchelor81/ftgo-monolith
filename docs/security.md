# FTGO Security Architecture

## Overview

The FTGO security layer is implemented as a shared library (`shared/ftgo-security-lib`) that provides a consistent Spring Security foundation for all microservices. It uses Spring Security 6.x patterns with `SecurityFilterChain` beans — not the deprecated `WebSecurityConfigurerAdapter`.

## Module Structure

```
shared/ftgo-security-lib/
├── build.gradle                          # Convention plugins + security deps
├── src/main/java/com/ftgo/security/
│   ├── config/
│   │   ├── FtgoSecurityAutoConfiguration.java   # Auto-config entry point
│   │   ├── SecurityFilterChainConfig.java        # Default SecurityFilterChain
│   │   ├── ActuatorSecurityConfig.java           # Actuator endpoint security
│   │   ├── CorsConfig.java                       # CORS configuration
│   │   ├── SecurityExceptionHandler.java         # JSON error responses
│   │   ├── SecurityProperties.java               # @ConfigurationProperties
│   │   └── ServiceSecurityConfigurer.java        # Per-service customization
│   ├── exception/
│   │   └── SecurityConfigurationException.java   # Config error type
│   ├── filter/
│   │   └── SecurityLoggingFilter.java            # Security audit filter
│   └── util/
│       ├── SecurityContextUtils.java             # Auth context helpers
│       └── RequestUtils.java                     # HTTP request helpers
├── src/main/resources/
│   ├── META-INF/spring/
│   │   └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
│   ├── application-security.yml                  # Default security profile
│   └── application-security-prod.yml             # Production overrides
└── src/test/ & src/integration-test/             # Test suites
```

## Security Filter Chains

### Default Filter Chain (Order: default)

Applies to all non-actuator requests:

| URL Pattern | Access |
|---|---|
| `/actuator/health` | Public |
| `/actuator/info` | Public |
| `/v3/api-docs/**` | Public |
| `/swagger-ui/**` | Public |
| `OPTIONS /**` | Public (CORS preflight) |
| All other paths | Authenticated |

### Actuator Filter Chain (Order: 1 — highest precedence)

Applies only to actuator endpoints:

| Endpoint | Access |
|---|---|
| `/actuator/health` | Public |
| `/actuator/info` | Public |
| `/actuator/metrics` | Authenticated |
| `/actuator/env` | Authenticated |
| All other actuator | Authenticated |

## CSRF Protection

CSRF protection is **disabled** for all microservices because:

1. APIs are stateless — no server-side sessions or cookies
2. Authentication uses HTTP headers (Basic/Bearer tokens)
3. No browser-based form submissions target these APIs directly

This is the recommended pattern for stateless REST APIs per Spring Security documentation.

## Session Management

Session creation is set to `STATELESS`:

- No `JSESSIONID` cookies are created
- No server-side HTTP sessions are maintained
- Each request must carry its own authentication credentials

## CORS Configuration

CORS is configurable via application properties:

```yaml
ftgo:
  security:
    cors:
      allowed-origins: "https://ftgo.example.com"
      allowed-methods: GET,POST,PUT,DELETE,PATCH,OPTIONS
      allowed-headers: Authorization,Content-Type,X-Requested-With,Accept,Origin
      exposed-headers: ""
      allow-credentials: true
      max-age: 3600
```

**Development default:** `allowed-origins: *` (all origins)
**Production:** Must be restricted to specific domains via `application-security-prod.yml`

## Error Responses

Security exceptions return structured JSON instead of HTML:

### 401 Unauthorized
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication required",
  "path": "/api/orders"
}
```

### 403 Forbidden
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied",
  "path": "/api/orders/admin"
}
```

## Per-Service Customization

Services can customize security by:

### 1. Implementing `ServiceSecurityConfigurer`

The interface provides two extension points:
- `configureAuthorization(auth)` — add request matcher rules (called *before* the `anyRequest().authenticated()` catch-all)
- `configureHttpSecurity(http)` — other `HttpSecurity` customizations such as adding filters or OAuth2 (called after the base config but before `http.build()`)

Both methods have default no-op implementations, so services only override what they need.

```java
@Component
public class OrderServiceSecurityConfigurer implements ServiceSecurityConfigurer {

    @Override
    public void configureAuthorization(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>
                .AuthorizationManagerRequestMatcherRegistry auth) {
        auth.requestMatchers("/api/orders/public/**").permitAll();
    }

    @Override
    public String serviceName() {
        return "order-service";
    }
}
```

### 2. Declaring an additional `SecurityFilterChain` bean

The default chain runs at `@Order(2)` and the actuator chain at `@Order(1)`. Services can add their own filter chains at different orders to handle specific request patterns before the default chain:

```java
@Configuration
public class CustomSecurityConfig {

    @Bean
    @Order(3)
    public SecurityFilterChain customFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/public/**")
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );
        return http.build();
    }
}
```

## Configuration Properties

All properties are under the `ftgo.security` prefix:

| Property | Default | Description |
|---|---|---|
| `ftgo.security.public-paths` | `/actuator/health, /actuator/info, /v3/api-docs/**, /swagger-ui/**, /swagger-ui.html` | URL patterns accessible without auth |
| `ftgo.security.cors.allowed-origins` | `*` | Allowed CORS origins |
| `ftgo.security.cors.allowed-methods` | `GET,POST,PUT,DELETE,PATCH,OPTIONS` | Allowed HTTP methods |
| `ftgo.security.cors.allowed-headers` | `Authorization,Content-Type,X-Requested-With,Accept,Origin` | Allowed request headers |
| `ftgo.security.cors.exposed-headers` | (empty) | Headers exposed to the client |
| `ftgo.security.cors.allow-credentials` | `false` | Whether to allow credentials |
| `ftgo.security.cors.max-age` | `3600` | Preflight response cache duration (seconds) |
| `ftgo.security.logging.enabled` | `true` | Enable/disable security audit logging |

## Profiles

| Profile | Purpose |
|---|---|
| `security` | Default security settings (activate via `spring.profiles.active=security`) |
| `security-prod` | Production overrides — restricted CORS, env-based credentials |

## Adding Security to a Service

1. Add the dependency:
   ```groovy
   implementation project(":shared:ftgo-security-lib")
   ```

2. Optionally activate the security profile:
   ```yaml
   spring:
     profiles:
       active: security
   ```

3. Override properties as needed in the service's `application.yml`.

## Utility Classes

### `SecurityContextUtils`

Static utility methods for accessing the Spring Security context:

- `getCurrentAuthentication()` — returns `Optional<Authentication>`
- `getCurrentUsername()` — returns `Optional<String>`
- `isAuthenticated()` — returns `boolean`
- `getCurrentAuthorities()` — returns authority collection
- `hasAuthority(String)` — checks for a specific authority

### `RequestUtils`

HTTP request utility methods:

- `extractBearerToken(HttpServletRequest)` — extracts Bearer token from Authorization header
- `getClientIpAddress(HttpServletRequest)` — resolves client IP (supports `X-Forwarded-For`)

## Future Enhancements

- **JWT authentication** (EM-40): Will integrate with `ftgo-security-lib` via a JWT filter
- **RBAC** (EM-37): Role-based access control will use the `ServiceSecurityConfigurer` extension point
- **OAuth2 Resource Server**: Can be layered on top of the base configuration
