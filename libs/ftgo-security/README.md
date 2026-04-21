# libs/ftgo-security

Shared Spring Security foundation for every FTGO microservice.

## What it provides

- `BaseSecurityConfiguration` — default `SecurityFilterChain` beans with stateless session
  management, CSRF disabled (stateless REST APIs), CORS enabled, permissive access to
  `/actuator/health` and `/actuator/info`, and authentication required for everything else.
- `SecurityExceptionHandler` — unified JSON error body for 401 (authentication required)
  and 403 (access denied) responses.
- `SecurityUtils` — helper for reading the current authentication from
  `SecurityContextHolder` without scattering the context lookup throughout the code base.
- `FtgoSecurityAutoConfiguration` — Spring Boot auto-configuration entry point so
  services pick up the defaults simply by declaring `implementation project(':libs:ftgo-security')`.

## How to consume

```groovy
dependencies {
    implementation project(':libs:ftgo-security')
}
```

Services can override any bean by defining a `SecurityFilterChain` of the same name in
their own `net.chrisrichardson.ftgo.<service>.security` package.

See `EM-39` for migration context.
