# Stage 1: Build the JAR
FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the JAR
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/statistical-tests-project-1.0.jar app.jar
EXPOSE 7911
ENTRYPOINT ["java", "-jar", "app.jar"]