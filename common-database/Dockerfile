# Bước 1: Build file JAR
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Bước 2: Chạy ứng dụng
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/identity-service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]