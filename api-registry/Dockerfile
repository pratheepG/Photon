FROM eclipse-temurin:17-jdk-alpine
LABEL authors="pratheepg"
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]