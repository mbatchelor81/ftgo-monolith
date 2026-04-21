# See templates/service-template/docker/Dockerfile for the canonical form.
# Build with ./gradlew :services:order-service:bootJar, then docker build .

FROM openjdk:8u171-jre-alpine
RUN apk --no-cache add curl
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8080
HEALTHCHECK --start-period=30s --interval=5s \
  CMD curl -f http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app/app.jar"]
