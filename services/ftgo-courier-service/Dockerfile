# ---- Builder stage ----
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /workspace

# Install bash (required by Gradle wrapper internals)
RUN apk add --no-cache bash

# Copy Gradle wrapper and build configuration for dependency caching
COPY services/gradlew services/gradlew.bat services/settings.gradle ./services/
COPY services/gradle/ ./services/gradle/
COPY build-logic/ ./build-logic/
COPY gradle/libs.versions.toml ./gradle/

# Copy shared library build files (for dependency resolution)
COPY services/ftgo-common/build.gradle ./services/ftgo-common/
COPY services/ftgo-common-jpa/build.gradle ./services/ftgo-common-jpa/
COPY services/ftgo-domain/build.gradle ./services/ftgo-domain/
COPY services/ftgo-security-lib/build.gradle ./services/ftgo-security-lib/
COPY services/ftgo-observability-lib/build.gradle ./services/ftgo-observability-lib/
COPY services/ftgo-openapi-lib/build.gradle ./services/ftgo-openapi-lib/
COPY services/ftgo-error-handling-lib/build.gradle ./services/ftgo-error-handling-lib/
COPY services/ftgo-courier-service-api/build.gradle ./services/ftgo-courier-service-api/
COPY services/ftgo-courier-service/build.gradle ./services/ftgo-courier-service/

# Download dependencies (cached unless build files change)
RUN cd services && chmod +x gradlew && ./gradlew :ftgo-courier-service:dependencies --no-daemon 2>/dev/null || true

# Copy shared library sources
COPY services/ftgo-common/src/ ./services/ftgo-common/src/
COPY services/ftgo-common-jpa/src/ ./services/ftgo-common-jpa/src/
COPY services/ftgo-domain/src/ ./services/ftgo-domain/src/
COPY services/ftgo-security-lib/src/ ./services/ftgo-security-lib/src/
COPY services/ftgo-observability-lib/src/ ./services/ftgo-observability-lib/src/
COPY services/ftgo-openapi-lib/src/ ./services/ftgo-openapi-lib/src/
COPY services/ftgo-error-handling-lib/src/ ./services/ftgo-error-handling-lib/src/
COPY services/ftgo-courier-service-api/src/ ./services/ftgo-courier-service-api/src/

# Copy service source code
COPY services/ftgo-courier-service/src/ ./services/ftgo-courier-service/src/

# Build the application jar
RUN cd services && ./gradlew :ftgo-courier-service:bootJar --no-daemon -x test

# Create minimal custom JRE with jlink (Alpine-native, musl-compatible)
RUN jlink \
    --add-modules java.base,java.compiler,java.desktop,java.instrument,java.logging,java.management,java.naming,java.net.http,java.rmi,java.security.jgss,java.security.sasl,java.sql,java.transaction.xa,java.xml,jdk.crypto.ec,jdk.jfr,jdk.management,jdk.unsupported \
    --strip-debug --compress=2 --no-header-files --no-man-pages \
    --output /custom-jre

# ---- Runtime stage ----
FROM alpine:3.19

ENV JAVA_HOME=/opt/java
ENV PATH="$JAVA_HOME/bin:$PATH"

RUN apk add --no-cache wget && \
    addgroup -S -g 1001 ftgo && \
    adduser -S -u 1001 -G ftgo -s /sbin/nologin ftgo

WORKDIR /app

COPY --from=builder /custom-jre $JAVA_HOME
COPY --from=builder /workspace/services/ftgo-courier-service/build/libs/*.jar app.jar

RUN chown -R ftgo:ftgo /app

USER ftgo

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health/liveness || exit 1

# Use exec form for proper signal handling (PID 1)
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
