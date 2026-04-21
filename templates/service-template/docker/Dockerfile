# syntax=docker/dockerfile:1.6
#
# Canonical Dockerfile for FTGO microservices.
#
# The image ONLY packages a pre-built Spring Boot fat jar; producing that jar
# is a separate concern handled by the CI workflow
# (.github/workflows/docker-build.yml) or by a local `./gradlew` invocation
# before `docker build`.
#
# Rationale: during the strangler-fig migration, the services/*/build.gradle
# modules are scaffolds that don't apply the Spring Boot plugin yet, and the
# repo-wide gradlew is pinned to a Gradle version whose distribution download
# requires TLS cipher suites unavailable in eclipse-temurin:8-jdk-alpine. Both
# of those problems disappear when the jar is produced on the GitHub Actions
# runner (or the developer's host JDK) and only copied into the image here.
#
# Build context MUST be the repository root so the jar path resolves.
#
# Usage (from repo root):
#   ./gradlew :services:service-template:bootJar -x test
#   docker build \
#     --build-arg SERVICE_NAME=service-template \
#     --build-arg SERVICE_VERSION=0.0.1-SNAPSHOT \
#     --build-arg JAR_PATH=services/service-template/build/libs/service-template.jar \
#     -f templates/service-template/docker/Dockerfile \
#     -t ghcr.io/mbatchelor81/ftgo-service-template:0.0.1-SNAPSHOT .
#
# The runtime image targets < 200 MB by using a slim JRE Alpine base and a
# non-root user.

FROM eclipse-temurin:8-jre-alpine AS runtime

ARG SERVICE_NAME=service-template
ARG SERVICE_VERSION=0.0.0-SNAPSHOT
ARG GIT_SHA=unknown
ARG BUILD_DATE=unknown
# Relative path (from the build context / repo root) to the pre-built fat jar.
ARG JAR_PATH=services/service-template/build/libs/app.jar

LABEL org.opencontainers.image.title="ftgo-${SERVICE_NAME}" \
      org.opencontainers.image.description="FTGO ${SERVICE_NAME} microservice" \
      org.opencontainers.image.source="https://github.com/mbatchelor81/ftgo-monolith" \
      org.opencontainers.image.version="${SERVICE_VERSION}" \
      org.opencontainers.image.revision="${GIT_SHA}" \
      org.opencontainers.image.created="${BUILD_DATE}" \
      org.opencontainers.image.licenses="Apache-2.0"

RUN apk --no-cache add curl tini \
    && addgroup -S ftgo \
    && adduser -S -G ftgo ftgo

WORKDIR /app

COPY --chown=ftgo:ftgo ${JAR_PATH} /app/app.jar

ENV SERVICE_NAME=${SERVICE_NAME} \
    SERVICE_VERSION=${SERVICE_VERSION} \
    JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

USER ftgo

EXPOSE 8080

HEALTHCHECK --start-period=30s --interval=10s --timeout=3s --retries=3 \
  CMD curl -fsS http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["/sbin/tini", "--", "sh", "-c", "exec java ${JAVA_OPTS} -jar /app/app.jar"]
