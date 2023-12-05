FROM eclipse-temurin:21-jre

COPY target/vision-api-consumer.jar /app/vision-api-consumer.jar

WORKDIR /app

CMD [ "java", "-jar", "vision-api-consumer.jar" ]