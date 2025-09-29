FROM eclipse-temurin:25-jre

COPY target/sae-database-writer*.jar /app/sae-database-writer.jar

WORKDIR /app

CMD [ "java", "-jar", "sae-database-writer.jar" ]