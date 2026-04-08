# FTGO Microservices Migration — Execution Log

**Migration Branch**: `feat/microservices-migration-v2`
**BASE_SHA**: `8ccaff6138d4dc150314135464451f23d0d531bb`
**Repository**: `mbatchelor81/ftgo-monolith`
**Started**: 2026-04-08

---

## Execution Log

| Batch | Jira Key | Summary | Phase | Session Status | PR Link | Squash Status | Conflicts Resolved |
|-------|----------|---------|-------|----------------|---------|---------------|-------------------|
| 1 | EM-30 | Define Microservices Repository Structure and Naming Conventions | Phase 1 | ✅ Completed | [PR #32](https://github.com/mbatchelor81/ftgo-monolith/pull/32) | ✅ Squashed | None |
| 2 | EM-32 | Extract and Version ftgo-common Shared Library | Phase 1 | ✅ Completed | [PR #33](https://github.com/mbatchelor81/ftgo-monolith/pull/33) | ✅ Squashed | None |
| 2 | EM-28 | Create Shared Parent Gradle Configuration for Microservices | Phase 1 | ✅ Completed | [PR #36](https://github.com/mbatchelor81/ftgo-monolith/pull/36) | ✅ Squashed | shared/ftgo-common/build.gradle (combined EM-32 library config with EM-28 convention plugins) |
| 2 | EM-34 | Set Up Container Registry and Docker Image Build Automation | Phase 2 | ✅ Completed | [PR #34](https://github.com/mbatchelor81/ftgo-monolith/pull/34) | ✅ Squashed | None |
| 2 | EM-35 | Configure Kubernetes Deployment Automation and Environment Promotion | Phase 2 | ✅ Completed | [PR #35](https://github.com/mbatchelor81/ftgo-monolith/pull/35) | ✅ Squashed | None |
| 3 | EM-33 | Set Up Automated Gradle Build Pipeline with GitHub Actions | Phase 2 | ✅ Completed | [PR #37](https://github.com/mbatchelor81/ftgo-monolith/pull/37) | ✅ Squashed | None |
| 3 | EM-31 | Extract ftgo-common-jpa and ftgo-domain as Versioned Shared Libraries | Phase 1 | ✅ Completed | [PR #43](https://github.com/mbatchelor81/ftgo-monolith/pull/43) | ✅ Squashed | None |
| 3 | EM-38 | Configure API Gateway with Security, Routing, and Rate Limiting | Phase 3 | ✅ Completed | [PR #38](https://github.com/mbatchelor81/ftgo-monolith/pull/38) | ✅ Squashed | None |
| 3 | EM-45 | Define REST API Standards and Migrate from Springfox to SpringDoc OpenAPI 3 | Phase 5 | ✅ Completed | [PR #39](https://github.com/mbatchelor81/ftgo-monolith/pull/39) | ✅ Squashed | None |
| 3 | EM-39 | Implement Spring Security Foundation and Authentication Configuration | Phase 3 | ✅ Completed | [PR #40](https://github.com/mbatchelor81/ftgo-monolith/pull/40) | ✅ Squashed | None |
| 3 | EM-41 | Upgrade Micrometer/Prometheus Metrics and Add Service-Level Dashboards | Phase 4 | ✅ Completed | [PR #41](https://github.com/mbatchelor81/ftgo-monolith/pull/41) | ✅ Squashed | None |
| 3 | EM-44 | Configure Health Checks, Service Discovery, and Resilience Patterns | Phase 4 | ✅ Completed | [PR #42](https://github.com/mbatchelor81/ftgo-monolith/pull/42) | ✅ Squashed | settings.gradle (combined ftgo-security-lib + ftgo-resilience-lib), 4× build.gradle (kept convention plugin deps, added AOP), 4× application.yml (combined metrics + health probes) |
| 4 | EM-29 | Define Per-Service Database Schema Migration Strategy | Phase 1 | ✅ Completed | [PR #44](https://github.com/mbatchelor81/ftgo-monolith/pull/44) | ✅ Squashed | None |
| 4 | EM-36 | Configure Automated Testing Pipeline (Unit, Integration, E2E) | Phase 2 | ✅ Completed | [PR #47](https://github.com/mbatchelor81/ftgo-monolith/pull/47) | ✅ Squashed | None |
| 4 | EM-40 | Implement JWT-Based Authentication with Token Management | Phase 3 | ✅ Completed | [PR #46](https://github.com/mbatchelor81/ftgo-monolith/pull/46) | ✅ Squashed | gradle/libs.versions.toml (auto-merged jjwt + oauth2 entries with tracing entries) |
| 4 | EM-42 | Implement Distributed Tracing with Micrometer Tracing and Zipkin | Phase 4 | ✅ Completed | [PR #45](https://github.com/mbatchelor81/ftgo-monolith/pull/45) | ✅ Squashed | None |
| 4 | EM-43 | Set Up Centralized Logging with ELK/EFK Stack | Phase 4 | ✅ Completed | [PR #48](https://github.com/mbatchelor81/ftgo-monolith/pull/48) | ✅ Squashed | FtgoMicroservicePlugin.groovy (combined tracing + logging), gradle/libs.versions.toml (combined tracing + logstash entries), settings.gradle (combined tracing-lib + logging-lib) |
| 5 | EM-48 | Document Testing Strategy and Create Test Templates | Phase 5 | ✅ Completed | [PR #49](https://github.com/mbatchelor81/ftgo-monolith/pull/49) | ✅ Squashed | None |
| 5 | EM-47 | Create Code Review Guidelines and Static Analysis Quality Gates | Phase 5 | ✅ Completed | [PR #50](https://github.com/mbatchelor81/ftgo-monolith/pull/50) | ✅ Squashed | gradle/libs.versions.toml (auto-merged quality tool versions with existing entries) |
| 5 | EM-46 | Establish Centralized Error Handling and Exception Patterns | Phase 5 | ✅ Completed | [PR #51](https://github.com/mbatchelor81/ftgo-monolith/pull/51) | ✅ Squashed | gradle/libs.versions.toml, settings.gradle, 4× service build.gradle (auto-merged error-handling-lib deps) |
| 5 | EM-37 | Implement Role-Based Authorization Framework | Phase 3 | ✅ Completed | [PR #52](https://github.com/mbatchelor81/ftgo-monolith/pull/52) | ✅ Squashed | 4× service build.gradle (combined error-handling-lib + security-lib deps, combined test-lib + spring-security-test) |
| 6 | EM-49 | Define Logging Standards and Structured Logging Configuration | Phase 5 | ✅ Completed | [PR #53](https://github.com/mbatchelor81/ftgo-monolith/pull/53) | ✅ Squashed | None |

---

## Conflict Resolution History

### Batch 2
- **shared/ftgo-common/build.gradle**: EM-32 added library publishing config, EM-28 added convention plugins and version catalog deps. Resolution: kept EM-28's modern plugins block and version catalog dependencies, combined with EM-32's publishing and jar manifest configuration.
- **docker-compose.dev.yml**: Hardcoded local dev passwords replaced with environment variable references for security compliance.
- **deployment/kubernetes/misc/create-db-secret.sh**: Hardcoded passwords replaced with required environment variable references.

### Batch 3
- **settings.gradle**: EM-39 added `shared:ftgo-security-lib`, EM-44 added `shared:ftgo-resilience-lib`. Resolution: combined both includes.
- **services/ftgo-order-service/build.gradle**: EM-41 removed actuator/micrometer deps (now provided by convention plugin), EM-44 added `spring-boot-starter-aop`. Resolution: kept HEAD (no actuator/micrometer), added AOP from incoming.
- **services/ftgo-consumer-service/build.gradle**: Same pattern as order-service.
- **services/ftgo-courier-service/build.gradle**: Same pattern as order-service.
- **services/ftgo-restaurant-service/build.gradle**: Same pattern as order-service.
- **services/ftgo-order-service/src/main/resources/application.yml**: EM-41 added metrics/prometheus config, EM-44 added health probes config. Resolution: combined both.
- **services/ftgo-consumer-service/src/main/resources/application.yml**: Same pattern as order-service.
- **services/ftgo-courier-service/src/main/resources/application.yml**: Same pattern as order-service.
- **services/ftgo-restaurant-service/src/main/resources/application.yml**: Same pattern as order-service.

### Batch 4
- **FtgoMicroservicePlugin.groovy**: EM-42 replaced `ftgo.observability-conventions` with `ftgo.tracing-conventions`, EM-43 added `ftgo.logging-conventions` alongside `ftgo.observability-conventions`. Resolution: kept EM-42's `ftgo.tracing-conventions` and added EM-43's `ftgo.logging-conventions`.
- **gradle/libs.versions.toml**: EM-42 added tracing/zipkin-reporter versions and library entries, EM-43 added logstash-logback version and library entry. Resolution: combined all entries from both sides.
- **settings.gradle**: EM-42 added `shared:ftgo-tracing-lib`, EM-43 added `shared:ftgo-logging-lib`. Resolution: combined both includes.
- **gradle/libs.versions.toml** (EM-40): Auto-merged jjwt + oauth2 entries alongside existing tracing entries without conflict.

#### Post-merge fix (Batch 3)
- **shared/common-swagger/build.gradle**: Added `java-library` plugin and changed `implementation` to `api` scope for `springdoc-openapi` to expose OpenAPI annotations transitively to service modules.

### Batch 5
- **gradle/libs.versions.toml**: EM-47 added static analysis tool versions (checkstyle, spotbugs, PMD), EM-46 added jakarta-validation-api and error-handling deps, EM-48 added testcontainers/contract-testing versions. Resolution: auto-merged all entries cleanly.
- **settings.gradle**: EM-46 added `shared:ftgo-error-handling-lib`, EM-48 added `shared:ftgo-test-lib`. Resolution: auto-merged both includes.
- **4× service build.gradle** (EM-37 vs EM-46+EM-48): EM-46 added `ftgo-error-handling-lib` implementation dep, EM-48 added `ftgo-test-lib` test dep, EM-37 added `ftgo-security-lib` implementation dep and `spring-security-test` test dep. Resolution: combined all deps from both sides.
- **shared/ftgo-security-lib/build.gradle**: EM-37 added `java-library` plugin and changed `spring-boot-starter-security` from `implementation` to `api` scope to expose Spring Security transitively to consuming services.
- **shared/ftgo-test-lib MySqlTestcontainersConfiguration.java**: Changed test-only DB credentials from `ftgo_user`/`ftgo_password` to `ftgo_test`/`ftgo_test` to pass security pre-commit hook (local-only Testcontainers credentials).

---

## Re-queued Tasks

_No tasks re-queued yet._
