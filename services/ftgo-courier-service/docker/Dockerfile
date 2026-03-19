FROM openjdk:8-jre-alpine

LABEL maintainer="ftgo-team"
LABEL service="ftgo-courier-service"

RUN addgroup -S ftgo && adduser -S ftgo -G ftgo

COPY build/libs/ftgo-courier-service-*.jar /app/app.jar

USER ftgo

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
