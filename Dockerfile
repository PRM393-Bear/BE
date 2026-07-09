# ==========================
# Stage 1: Build
# ==========================
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom.xml trước để tận dụng Docker cache
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build project (không cần clean trong Docker)
RUN mvn package -DskipTests

# ==========================
# Stage 2: Runtime
# ==========================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy file jar
COPY --from=build /app/target/*.jar app.jar

# Spring Boot port
EXPOSE 8080

# Chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]