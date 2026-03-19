FROM openjdk:8-jre-alpine

LABEL maintainer="ftgo-team"
LABEL service="ftgo-consumer-service"

RUN addgroup -S ftgo && adduser -S ftgo -G ftgo

COPY build/libs/ftgo-consumer-service-*.jar /app/app.jar

USER ftgo

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
