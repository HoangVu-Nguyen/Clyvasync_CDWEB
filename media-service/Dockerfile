# Stage 1: Build stage
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

# 1. Copy file POM cha
COPY pom.xml .

# 2. Copy source code của các module chung
COPY common-core/ common-core/
COPY common-database/ common-database/
COPY common-security/ common-security/

# 3. Copy source code của Media Service
COPY media-service/ media-service/

# 4. Cài đặt POM cha vào repo local (Non-recursive)
RUN mvn install -N -DskipTests

# 5. Cài đặt các module Common bằng cách trỏ trực tiếp file POM
# Cách này giúp tránh lỗi "Child module not found" từ dự án cha
RUN mvn -f common-core/pom.xml install -DskipTests && \
    mvn -f common-database/pom.xml install -DskipTests && \
    mvn -f common-security/pom.xml install -DskipTests

# 6. Build Media Service thành file JAR
RUN mvn -f media-service/pom.xml clean package -DskipTests

# Stage 2: Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy file jar từ Stage 1
COPY --from=build /app/media-service/target/media-service-1.0.0.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]