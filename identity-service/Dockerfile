# Stage 1: Build stage
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

# 1. Copy file POM cha (Root)
COPY pom.xml .

# 2. Copy các module chung (Common) mà Identity Service phụ thuộc vào
COPY common-core/ common-core/
COPY common-database/ common-database/
COPY common-security/ common-security/

# 3. Copy module đích
COPY identity-service/ identity-service/

# 4. Cài đặt POM cha vào local repository của container (chỉ cài đặt file POM)
RUN mvn install -N -DskipTests

# 5. Cài đặt các module Common bằng cách trỏ trực tiếp file POM của từng cái
# Cách này bỏ qua việc kiểm tra các module con khác trong dự án cha
RUN mvn -f common-core/pom.xml install -DskipTests && \
    mvn -f common-database/pom.xml install -DskipTests && \
    mvn -f common-security/pom.xml install -DskipTests

# 6. Build gói JAR cho identity-service
RUN mvn -f identity-service/pom.xml clean package -DskipTests

# Stage 2: Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy file jar từ Stage 1 (Hãy đảm bảo tên file jar khớp với cấu hình Maven của bạn)
COPY --from=build /app/identity-service/target/*.jar app.jar

EXPOSE 8081

# Tối ưu: Sử dụng các tham số JVM để container nhận diện RAM tốt hơn
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]