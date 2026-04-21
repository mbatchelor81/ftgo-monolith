# syntax=docker/dockerfile:1.6
#
# Dockerfile for the FTGO Courier microservice.
#
# Build context MUST be the repository root so Gradle can resolve shared
# modules.
#
#   docker build \
#     --build-arg SERVICE_VERSION=0.0.1-SNAPSHOT \
#     --build-arg GIT_SHA=$(git rev-parse --short HEAD) \
#     -f services/courier-service/docker/Dockerfile \
#     -t ghcr.io/mbatchelor81/ftgo-courier-service:0.0.1-SNAPSHOT .
#
# See templates/service-template/docker/Dockerfile for the canonical form.

###############################################################################
# Stage 1: build the Spring Boot fat jar with Gradle.
###############################################################################
FROM eclipse-temurin:8-jdk-alpine AS builder

ARG SERVICE_NAME=courier-service
ARG GRADLE_OPTS="-Dorg.gradle.daemon=false -Dorg.gradle.parallel=true"
ENV GRADLE_OPTS=${GRADLE_OPTS}

WORKDIR /workspace

COPY gradlew gradlew
COPY gradle gradle
COPY settings.gradle build.gradle gradle.properties ./
COPY buildSrc buildSrc

RUN chmod +x gradlew && ./gradlew --version

COPY . .

RUN ./gradlew --no-daemon ":services:${SERVICE_NAME}:bootJar" -x test \
    && cp services/${SERVICE_NAME}/build/libs/*.jar /workspace/app.jar

###############################################################################
# Stage 2: minimal JRE runtime image.
###############################################################################
FROM eclipse-temurin:8-jre-alpine AS runtime

ARG SERVICE_NAME=courier-service
ARG SERVICE_VERSION=0.0.0-SNAPSHOT
ARG GIT_SHA=unknown
ARG BUILD_DATE=unknown

LABEL org.opencontainers.image.title="ftgo-${SERVICE_NAME}" \
      org.opencontainers.image.description="FTGO Courier microservice" \
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
