# =============================================================================
# Consumer Service — Multi-stage Dockerfile
# =============================================================================
# Build: Gradle with Java 17 (matches build-logic conventions)
# Runtime: Eclipse Temurin JRE 17 on Alpine for minimal image size
# =============================================================================

# ---------------------------------------------------------------------------
# Stage 1: Build
# ---------------------------------------------------------------------------
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /workspace

# Copy Gradle wrapper and build files first for layer caching
COPY gradle/ gradle/
COPY gradlew settings.gradle build.gradle gradle.properties ./
COPY build-logic/ build-logic/
COPY buildSrc/ buildSrc/

# Copy shared libraries and modules required by consumer-service
COPY ftgo-common/ ftgo-common/
COPY ftgo-common-jpa/ ftgo-common-jpa/
COPY ftgo-domain/ ftgo-domain/
COPY ftgo-test-util/ ftgo-test-util/
COPY common-swagger/ common-swagger/
COPY libs/ libs/

# Copy service source
COPY services/consumer-service/ services/consumer-service/

# Build the service JAR (skip tests for Docker build — tests run in CI)
RUN chmod +x gradlew && \
    ./gradlew :consumer-service-app:bootJar --no-daemon -x test

# ---------------------------------------------------------------------------
# Stage 2: Runtime
# ---------------------------------------------------------------------------
FROM eclipse-temurin:17-jre-alpine AS runtime

RUN addgroup -S ftgo && adduser -S ftgo -G ftgo

WORKDIR /app

COPY --from=builder /workspace/services/consumer-service/consumer-service-app/build/libs/*.jar app.jar

RUN chown -R ftgo:ftgo /app
USER ftgo

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

LABEL org.opencontainers.image.title="ftgo-consumer-service" \
      org.opencontainers.image.description="FTGO Consumer Service" \
      org.opencontainers.image.source="https://github.com/mbatchelor81/ftgo-monolith"

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
