# Template Dockerfile for FTGO microservices.
#
# Build the service jar first (./gradlew :services:<name>-service:bootJar),
# then `docker build -t ftgo/<name>-service .` from the service directory.

FROM openjdk:8u171-jre-alpine

RUN apk --no-cache add curl

WORKDIR /app

# Copy the bootable Spring Boot jar produced by Gradle's bootJar task.
COPY build/libs/*.jar app.jar

EXPOSE 8080

HEALTHCHECK --start-period=30s --interval=5s \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app/app.jar"]
