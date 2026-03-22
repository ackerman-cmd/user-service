FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY build/libs/user-service-0.0.1-SNAPSHOT-plain.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
