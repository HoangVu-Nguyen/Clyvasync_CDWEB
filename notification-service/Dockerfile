# Stage 1: Build stage
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

# 1. Copy file POM cha
COPY pom.xml .

# 2. Xóa block <modules> trong POM cha để Maven không tìm các module con khác
RUN sed -i '/<modules>/,/<\/modules>/d' pom.xml

# 3. Copy module common-core (Notification service chỉ cần core)
COPY common-core/ common-core/

# 4. Copy source code của Notification Service
COPY notification-service/ notification-service/

# 5. Cài đặt POM cha vào repo local (Non-recursive)
RUN mvn install -N -DskipTests

# 6. Cài đặt common-core vào repo local của container
RUN mvn -f common-core/pom.xml install -DskipTests

# 7. Build Notification Service thành file JAR
RUN mvn -f notification-service/pom.xml clean package -DskipTests

# Stage 2: Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy file jar (Dùng wildcard *.jar để an toàn hơn nếu bạn đổi version)
COPY --from=build /app/notification-service/target/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]