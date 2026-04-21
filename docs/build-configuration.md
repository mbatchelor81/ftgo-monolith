# FTGO Build Configuration

> **Audience:** service owners adding a new FTGO microservice or shared
> library, or bumping a dependency version.

This document explains how the shared Gradle configuration introduced in
EM-28 is laid out and how to consume it from a new module. It is the
complement to [`CONVENTIONS.md`](../CONVENTIONS.md) — that file covers
the *what* (directory layout, naming, dependency rules); this one covers
the *how* (Gradle plugins, version catalog, bumping dependencies).

## 1. Moving parts

| File / directory                                 | Purpose                                                                 |
| ------------------------------------------------ | ----------------------------------------------------------------------- |
| [`gradle/libs.versions.toml`](../gradle/libs.versions.toml) | Gradle [version catalog] — single source of truth for dependency, plugin, and bundle versions. |
| [`build-logic/`](../build-logic)                 | Included composite build hosting the five `ftgo.*` convention plugins.  |
| [`build.gradle`](../build.gradle)                | Root build script — only cross-cutting settings and the legacy shim.    |
| [`settings.gradle`](../settings.gradle)          | Registers every subproject and `includeBuild 'build-logic'`.            |
| [`buildSrc/`](../buildSrc)                       | **Legacy.** Contains `WaitForMySqlPlugin` + friends; not used by new services. |

[version catalog]: https://docs.gradle.org/current/userguide/platforms.html

## 2. Convention plugins

Five plugins live under `build-logic/convention/src/main/groovy/`. Each
plugin's filename (minus `.gradle`) is its ID.

| Plugin ID                          | Applies to                       | What it configures                                                                 |
| ---------------------------------- | -------------------------------- | ---------------------------------------------------------------------------------- |
| `ftgo.java-conventions`            | Every JVM module                 | Java toolchain (17), UTF-8 encoding, `-parameters`, `maven-central` repos, `com.ftgo` group. |
| `ftgo.spring-boot-conventions`     | Deployable services only         | Applies Spring Boot + dependency-management plugins; imports `spring-boot-dependencies` BOM. Composes `ftgo.java-conventions`. |
| `ftgo.testing-conventions`         | Any module with `src/test/`      | JUnit 5 Jupiter, Rest-Assured, Mockito, AssertJ; `useJUnitPlatform()`; UTF-8 + UTC system props. |
| `ftgo.docker-conventions`          | Deployable services only         | [Jib] image build. Image name defaults to `ftgo/<project-name>`; container runs with 75 % max RAM percentage. |
| `ftgo.publishing-conventions`      | Shared libraries (`libs/*`, `*-service-api`) | `maven-publish` with source jar + POM metadata; repo via `FTGO_PUBLISH_URL` env var. |

[Jib]: https://github.com/GoogleContainerTools/jib

### Composition contract

Services apply **three** plugins in this exact order:

```groovy
plugins {
    id 'ftgo.spring-boot-conventions'   // → pulls in ftgo.java-conventions
    id 'ftgo.testing-conventions'
    id 'ftgo.docker-conventions'
}
```

Shared libraries apply **three** plugins:

```groovy
plugins {
    id 'ftgo.java-conventions'
    id 'ftgo.testing-conventions'
    id 'ftgo.publishing-conventions'
}
```

## 3. Adding a new microservice

1. `cp -r templates/service-template services/<name>-service` — the
   template already applies the canonical plugin stack (see
   [`templates/service-template/build.gradle`](../templates/service-template/build.gradle)).
2. In `services/<name>-service/build.gradle`:
   * Replace the `description`.
   * Add service-specific libraries using the version catalog (see §4).
3. In `settings.gradle`, add `include 'services:<name>-service'`.
4. Follow steps 2-6 in [`CONVENTIONS.md §7`](../CONVENTIONS.md).

**Goal:** a new service's `build.gradle` should be fewer than 30
non-comment lines. If it grows past that, either roll the common bits
into a convention plugin or justify the divergence with an ADR under
`docs/adr/`.

## 4. Using the version catalog

Inside any consuming `build.gradle` the catalog is exposed as the `libs`
accessor:

```groovy
dependencies {
    // Single library — dotted path matches the catalog entry with `-`→`.`.
    implementation libs.spring.boot.starter.web

    // Bundle — multiple libraries applied together.
    implementation libs.bundles.spring.boot.web

    // Plugin — used via `alias()` in a `plugins {}` block.
    // plugins { alias libs.plugins.jib }
}
```

Inside a **convention plugin** (precompiled script plugin), the catalog
must be looked up through the extension API because the `libs` accessor
is not auto-generated:

```groovy
def catalog = extensions.getByType(VersionCatalogsExtension).named('libs')
def version = catalog.findVersion('spring-boot').get().requiredVersion
dependencies {
    testImplementation catalog.findLibrary('junit-jupiter-api').get()
}
```

See [`ftgo.testing-conventions.gradle`](../build-logic/convention/src/main/groovy/ftgo.testing-conventions.gradle)
for the live example.

## 5. Bumping a dependency version

1. Edit the relevant entry in [`gradle/libs.versions.toml`](../gradle/libs.versions.toml).
2. Run `./gradlew dependencies --configuration runtimeClasspath` for any
   downstream service to confirm resolution.
3. If a plugin version changed (e.g. Spring Boot), also verify
   `./gradlew build-logic:convention:build` still resolves.

Do **not** hardcode versions inside an individual `build.gradle`. If a
service legitimately needs a divergent version, add it to the catalog
with an explicit name (e.g. `jackson-core-legacy`) rather than
overriding inline.

## 6. Legacy compatibility notes

The pre-migration `ftgo-*` modules pre-date the version catalog. They
remain in place with their own dependency declarations and are
configured by a filtered `subprojects { ... }` block in the root
`build.gradle`. Legacy modules:

* Keep `sourceCompatibility = 1.8` (the microservices migration does
  *not* upgrade legacy code to Java 17 in place).
* Do NOT apply any `ftgo.*` convention plugin.
* Will be retired module-by-module as each bounded context is extracted
  into `services/<name>-service/` under the EM-3x tickets.

The `WaitForMySqlPlugin` and `IntegrationTestsPlugin` in `buildSrc/`
were retained for legacy builds — new services must not apply them.

## 7. Acceptance checklist (EM-28)

| Acceptance criterion                                                | Evidence                                                            |
| ------------------------------------------------------------------- | ------------------------------------------------------------------- |
| Version catalog centralizes Spring Boot, Micrometer, JUnit 5, Rest-Assured, Flyway, Jackson | [`gradle/libs.versions.toml`](../gradle/libs.versions.toml)        |
| Convention plugins compile and can be applied by any microservice   | [`build-logic/convention/src/main/groovy/*.gradle`](../build-logic/convention/src/main/groovy) |
| Spring Boot 3.x with Java 17+ target                                | `spring-boot = "3.2.5"`, `java = "17"` in the catalog               |
| Each microservice `build.gradle` < 30 non-comment lines             | e.g. [`services/order-service/build.gradle`](../services/order-service/build.gradle) — 15 non-comment lines |
| Build configuration documented with examples                        | This file                                                           |
