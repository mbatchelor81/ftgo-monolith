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
| 4 | EM-29 | Define Per-Service Database Schema Migration Strategy | Phase 1 | Pending | — | — | — |
| 4 | EM-36 | Configure Automated Testing Pipeline (Unit, Integration, E2E) | Phase 2 | Pending | — | — | — |
| 4 | EM-40 | Implement JWT-Based Authentication with Token Management | Phase 3 | Pending | — | — | — |
| 4 | EM-42 | Implement Distributed Tracing with Spring Cloud Sleuth and Zipkin/Jaeger | Phase 4 | Pending | — | — | — |
| 4 | EM-43 | Set Up Centralized Logging with ELK/EFK Stack | Phase 4 | Pending | — | — | — |
| 5 | EM-37 | Implement Role-Based Authorization Framework | Phase 3 | Pending | — | — | — |
| 5 | EM-46 | Establish Centralized Error Handling and Exception Patterns | Phase 5 | Pending | — | — | — |
| 5 | EM-47 | Create Code Review Guidelines and Static Analysis Quality Gates | Phase 5 | Pending | — | — | — |
| 5 | EM-48 | Document Testing Strategy and Create Test Templates | Phase 5 | Pending | — | — | — |
| 6 | EM-49 | Define Logging Standards and Structured Logging Configuration | Phase 5 | Pending | — | — | — |

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

#### Post-merge fix (Batch 3)
- **shared/common-swagger/build.gradle**: Added `java-library` plugin and changed `implementation` to `api` scope for `springdoc-openapi` to expose OpenAPI annotations transitively to service modules.

---

## Re-queued Tasks

_No tasks re-queued yet._
