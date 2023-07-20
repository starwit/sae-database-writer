FROM eclipse-temurin:17-jre

COPY target/vision-api-consumer.jar /app/vision-api-consumer.jar
COPY .env.template /app/.env

WORKDIR /app

CMD [ "java", "-jar", "vision-api-consumer.jar" ]