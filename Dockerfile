# ---------- Stage 1: build ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Tải dependency trước để tận dụng cache khi chỉ sửa code
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
# Test đã chạy riêng bằng `mvn verify`; ở đây chỉ đóng gói
RUN mvn -B -q package -DskipTests

# ---------- Stage 2: runtime ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Chạy bằng user thường, không dùng root
RUN useradd --system --uid 1001 appuser
COPY --from=build /app/target/*.jar app.jar
USER appuser

EXPOSE 8082
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
