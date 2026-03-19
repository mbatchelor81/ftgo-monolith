# =============================================================================
# FTGO Consumer Service — Multi-stage Dockerfile
# =============================================================================
# Build:  docker build -t ftgo/ftgo-consumer-service .
# Run:    docker run -p 8081:8081 ftgo/ftgo-consumer-service
#
# For production, prefer Jib (./gradlew :services-ftgo-consumer-service:jibDockerBuild)
# which produces optimized layered images without requiring a Docker daemon.
# =============================================================================

# --- Stage 1: Build ---
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /workspace

# Copy Gradle wrapper and config first for layer caching
COPY gradle/ gradle/
COPY gradlew .
COPY build.gradle settings.gradle gradle.properties ./
COPY build-logic/ build-logic/
COPY buildSrc/ buildSrc/

# Copy shared libraries and service source
COPY shared/ shared/
COPY services/ftgo-consumer-service/ services/ftgo-consumer-service/
COPY services/ftgo-consumer-service-api/ services/ftgo-consumer-service-api/

# Build the service (skip tests — they run in CI)
RUN chmod +x gradlew && \
    ./gradlew :services-ftgo-consumer-service:bootJar --no-daemon -x test

# --- Stage 2: Runtime ---
FROM eclipse-temurin:17-jre-alpine

RUN addgroup -S ftgo && adduser -S ftgo -G ftgo

WORKDIR /app

COPY --from=builder /workspace/services/ftgo-consumer-service/build/libs/*.jar app.jar

RUN chown -R ftgo:ftgo /app
USER ftgo

EXPOSE 8081

ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
