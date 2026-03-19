# =============================================================================
# FTGO Order Service — Multi-stage Dockerfile
# =============================================================================

# --- Stage 1: Build ---
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /workspace

COPY gradle/ gradle/
COPY gradlew .
COPY build.gradle settings.gradle gradle.properties ./
COPY build-logic/ build-logic/
COPY buildSrc/ buildSrc/

COPY shared/ shared/
COPY services/ftgo-order-service/ services/ftgo-order-service/
COPY services/ftgo-order-service-api/ services/ftgo-order-service-api/
COPY services/ftgo-consumer-service-api/ services/ftgo-consumer-service-api/
COPY services/ftgo-restaurant-service-api/ services/ftgo-restaurant-service-api/

RUN chmod +x gradlew && \
    ./gradlew :services-ftgo-order-service:bootJar --no-daemon -x test

# --- Stage 2: Runtime ---
FROM eclipse-temurin:17-jre-alpine

RUN addgroup -S ftgo && adduser -S ftgo -G ftgo

WORKDIR /app

COPY --from=builder /workspace/services/ftgo-order-service/build/libs/*.jar app.jar

RUN chown -R ftgo:ftgo /app
USER ftgo

EXPOSE 8082

ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8082/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
