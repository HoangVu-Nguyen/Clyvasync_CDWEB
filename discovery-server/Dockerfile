# Stage 1: Build stage
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

# Copy file POM cha
COPY pom.xml .

# Copy source code của Discovery Server
COPY discovery-server/ discovery-server/

# Build Discovery Server thành file JAR
# Vì không phụ thuộc module common nên không cần chạy mvn install cho các module khác
RUN mvn -f discovery-server/pom.xml clean package -DskipTests

# Stage 2: Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy file jar từ Stage 1 sang Stage 2
# Lưu ý tên file jar theo artifactId và version trong POM: discovery-server-0.0.1-SNAPSHOT.jar
COPY --from=build /app/discovery-server/target/discovery-server-0.0.1-SNAPSHOT.jar app.jar

# Eureka Server mặc định chạy ở port 8761
EXPOSE 8761

ENTRYPOINT ["java", "-jar", "app.jar"]