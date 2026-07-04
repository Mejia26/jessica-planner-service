FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
ENV SPRING_PROFILES_ACTIVE=prod
COPY --from=build /workspace/target/agile-task-service-0.0.1-SNAPSHOT.jar /app/agile-task-service.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/agile-task-service.jar"]
