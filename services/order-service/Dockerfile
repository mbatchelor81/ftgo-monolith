# =============================================================================
# Order Service — Multi-stage Dockerfile
# =============================================================================
# Build: Gradle with Java 17 (matches build-logic conventions)
# Runtime: Eclipse Temurin JRE 17 on Alpine for minimal image size
# =============================================================================

# ---------------------------------------------------------------------------
# Stage 1: Build
# ---------------------------------------------------------------------------
FROM eclipse-temurin:17-jdk-alpine AS builder

RUN apk add --no-cache bash

WORKDIR /workspace

# Copy entire project context (multi-module Gradle requires all modules
# referenced in settings.gradle to be present for configuration phase)
COPY . .

# Build the service JAR (skip tests for Docker build — tests run in CI)
RUN chmod +x gradlew && \
    ./gradlew :order-service-app:build --no-daemon -x test

# ---------------------------------------------------------------------------
# Stage 2: Runtime
# ---------------------------------------------------------------------------
FROM eclipse-temurin:17-jre-alpine AS runtime

RUN addgroup -S ftgo && adduser -S ftgo -G ftgo

WORKDIR /app

COPY --from=builder /workspace/services/order-service/order-service-app/build/libs/*.jar app.jar

RUN chown -R ftgo:ftgo /app
USER ftgo

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

LABEL org.opencontainers.image.title="ftgo-order-service" \
      org.opencontainers.image.description="FTGO Order Service" \
      org.opencontainers.image.source="https://github.com/mbatchelor81/ftgo-monolith"

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
