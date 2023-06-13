FROM eclipse-temurin:17-jre

COPY target/vision-api-consumer.jar /vision-api-consumer.jar

CMD [ "java", "-jar", "/vision-api-consumer.jar" ]