# syntax=docker/dockerfile:1.6
#
# Canonical multi-stage Dockerfile for FTGO microservices.
#
# Build context MUST be the repository root so Gradle can resolve shared
# modules (libs/ftgo-common, libs/ftgo-common-jpa, ftgo-domain, etc.).
#
# Usage (from repo root):
#   docker build \
#     --build-arg SERVICE_NAME=service-template \
#     --build-arg SERVICE_VERSION=0.0.1-SNAPSHOT \
#     -f templates/service-template/docker/Dockerfile \
#     -t ghcr.io/mbatchelor81/ftgo-service-template:0.0.1-SNAPSHOT .
#
# The runtime image targets < 200 MB by using a slim JRE Alpine base and a
# non-root user. Layers are structured so dependency downloads cache
# independently of source changes.

###############################################################################
# Stage 1: build the Spring Boot fat jar with Gradle.
###############################################################################
FROM eclipse-temurin:8-jdk-alpine AS builder

ARG SERVICE_NAME=service-template
ARG GRADLE_OPTS="-Dorg.gradle.daemon=false -Dorg.gradle.parallel=true"
ENV GRADLE_OPTS=${GRADLE_OPTS}

# gradlew uses `#!/usr/bin/env bash`, which alpine lacks by default.
RUN apk --no-cache add bash

WORKDIR /workspace

# Gradle wrapper + build metadata first so dependency resolution layers cache
# independently of source changes.
COPY gradlew gradlew
COPY gradle gradle
COPY settings.gradle build.gradle gradle.properties ./
COPY buildSrc buildSrc

RUN chmod +x gradlew && ./gradlew --version

# Copy the remaining sources. In the legacy flat layout these live at the repo
# root; in the new layout they live under services/ and libs/. Copying the
# whole tree keeps this Dockerfile valid across the migration.
COPY . .

# Produce the bootable jar for the requested service. The task path matches
# the settings.gradle module coordinates (services:<name>).
RUN ./gradlew --no-daemon ":services:${SERVICE_NAME}:bootJar" -x test \
    && cp services/${SERVICE_NAME}/build/libs/*.jar /workspace/app.jar

###############################################################################
# Stage 2: minimal JRE runtime image.
###############################################################################
FROM eclipse-temurin:8-jre-alpine AS runtime

ARG SERVICE_NAME=service-template
ARG SERVICE_VERSION=0.0.0-SNAPSHOT
ARG GIT_SHA=unknown
ARG BUILD_DATE=unknown

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

COPY --from=builder --chown=ftgo:ftgo /workspace/app.jar /app/app.jar

ENV SERVICE_NAME=${SERVICE_NAME} \
    SERVICE_VERSION=${SERVICE_VERSION} \
    JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

USER ftgo

EXPOSE 8080

HEALTHCHECK --start-period=30s --interval=10s --timeout=3s --retries=3 \
  CMD curl -fsS http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["/sbin/tini", "--", "sh", "-c", "exec java ${JAVA_OPTS} -jar /app/app.jar"]
