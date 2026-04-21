# syntax=docker/dockerfile:1.6
#
# Dockerfile for the courier-service microservice. See
# templates/service-template/docker/Dockerfile for the canonical reference and
# rationale.

FROM eclipse-temurin:8-jre-alpine AS runtime

ARG SERVICE_NAME=courier-service
ARG SERVICE_VERSION=0.0.0-SNAPSHOT
ARG GIT_SHA=unknown
ARG BUILD_DATE=unknown
ARG JAR_PATH=services/courier-service/build/libs/app.jar

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
