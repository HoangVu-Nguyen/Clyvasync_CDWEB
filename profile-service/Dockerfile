# Stage 1: Build stage
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

# 1. Copy file POM cha
COPY pom.xml .

# 2. Copy các module chung (Common)
COPY common-core/ common-core/
COPY common-database/ common-database/
COPY common-security/ common-security/

# 3. Copy module đích
COPY profile-service/ profile-service/

# 4. Bước FIX: Cài đặt POM cha vào repo local
RUN mvn install -N -DskipTests

# 5. Bước FIX: Cài đặt các module Common
# Thay vì dùng -pl từ root, ta chạy trực tiếp file pom của từng module
RUN mvn -f common-core/pom.xml install -DskipTests && \
    mvn -f common-database/pom.xml install -DskipTests && \
    mvn -f common-security/pom.xml install -DskipTests

# 6. Build gói JAR cho profile-service
RUN mvn -f profile-service/pom.xml clean package -DskipTests

# Stage 2: Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/profile-service/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]