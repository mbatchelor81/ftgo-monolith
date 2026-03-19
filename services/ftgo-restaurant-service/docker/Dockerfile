FROM openjdk:8-jre-alpine

LABEL maintainer="ftgo-team"
LABEL service="ftgo-restaurant-service"

RUN addgroup -S ftgo && adduser -S ftgo -G ftgo

COPY build/libs/ftgo-restaurant-service-*.jar /app/app.jar

USER ftgo

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
