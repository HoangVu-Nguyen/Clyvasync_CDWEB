# Stage 1: Build stage
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

# 1. Copy file POM cha
COPY pom.xml .

# 2. Copy module phụ thuộc duy nhất của Gateway là common-core
COPY common-core/ common-core/

# 3. Copy source code của Gateway Service
COPY gateway-service/ gateway-service/

# 4. Cài đặt POM cha vào repo local (Non-recursive)
RUN mvn install -N -DskipTests

# 5. Cài đặt common-core vào repo local của container
# Gateway cần bản build của common-core để compile thành công
RUN mvn -f common-core/pom.xml install -DskipTests

# 6. Build Gateway Service thành file JAR
RUN mvn -f gateway-service/pom.xml clean package -DskipTests

# Stage 2: Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy file JAR từ stage build
# Lưu ý: Đảm bảo version 0.0.1-SNAPSHOT khớp chính xác với file pom.xml của bạn
COPY --from=build /app/gateway-service/target/*.jar app.jar
# Gateway thường chạy ở port 8080
EXPOSE 8080

# Tối ưu cho môi trường Reactive (WebFlux)
# Sử dụng tham số để Java tự điều chỉnh bộ nhớ trong container
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]