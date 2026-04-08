# =============================================================================
# ftgo-CHANGEME-service — Multi-stage Docker build
# =============================================================================
# Replace all occurrences of CHANGEME with your service's bounded context name.
#
# Stage 1: Build the application with Gradle
# Stage 2: Create a minimal runtime image with JRE 17
# =============================================================================

# ---------------------------------------------------------------------------
# Stage 1 — Builder
# ---------------------------------------------------------------------------
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /workspace

# Copy Gradle wrapper and build files first for dependency caching
COPY gradlew gradlew
COPY gradle/ gradle/
COPY build.gradle settings.gradle gradle.properties ./

# Copy buildSrc for custom plugins
COPY buildSrc/ buildSrc/

# Copy shared libraries needed for compilation
COPY shared/ shared/

# Copy service API modules needed for compilation
COPY services/ftgo-CHANGEME-service-api/ services/ftgo-CHANGEME-service-api/
# Add other API dependencies as needed:
# COPY services/ftgo-other-service-api/ services/ftgo-other-service-api/

# Copy the service source
COPY services/ftgo-CHANGEME-service/ services/ftgo-CHANGEME-service/

# Build the service JAR (skip tests — they run in CI separately)
RUN chmod +x gradlew && \
    ./gradlew :services:ftgo-CHANGEME-service:bootJar --no-daemon -x test

# ---------------------------------------------------------------------------
# Stage 2 — Runtime
# ---------------------------------------------------------------------------
FROM eclipse-temurin:17-jre AS runtime

LABEL service="ftgo-CHANGEME-service"
LABEL maintainer="ftgo-team"
LABEL org.opencontainers.image.source="https://github.com/mbatchelor81/ftgo-monolith"
LABEL org.opencontainers.image.description="FTGO CHANGEME Service"

# Install curl for health checks and dumb-init for proper signal handling
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl dumb-init && \
    rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd --gid 1001 ftgo && \
    useradd --uid 1001 --gid ftgo --shell /bin/false --create-home ftgo

WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /workspace/services/ftgo-CHANGEME-service/build/libs/ftgo-CHANGEME-service-*.jar app.jar

# Set ownership to non-root user
RUN chown -R ftgo:ftgo /app

USER ftgo

EXPOSE 8080

# Health check using Spring Boot Actuator
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Use dumb-init as PID 1 for proper signal handling (SIGTERM, etc.)
ENTRYPOINT ["dumb-init", "--"]
CMD ["java", \
     "-XX:+UseContainerSupport", \
     "-XX:MaxRAMPercentage=75.0", \
     "-Djava.security.egd=file:/dev/./urandom", \
     "-jar", "app.jar"]
