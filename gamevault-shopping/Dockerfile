# ==========================================
# Dockerfile for Shopping Service
# ==========================================

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# 复制整个项目
COPY . .

# 构建
RUN mvn clean package -DskipTests -pl gamevault-auth -am

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN apk add --no-cache curl
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder /app/gamevault-shopping/target/*.jar app.jar

RUN chown -R appuser:appgroup /app
USER appuser

EXPOSE 8082

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8082/actuator/health || exit 1

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
