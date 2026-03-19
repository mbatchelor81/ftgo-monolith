FROM openjdk:8-jre-alpine

LABEL maintainer="ftgo-team"
LABEL service="ftgo-order-service"

RUN addgroup -S ftgo && adduser -S ftgo -G ftgo

COPY build/libs/ftgo-order-service-*.jar /app/app.jar

USER ftgo

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
