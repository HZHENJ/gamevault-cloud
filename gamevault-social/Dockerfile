# ==========================================
# Stage 1: Build
# ==========================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 安装 Maven
RUN apk add --no-cache maven

# 复制根 pom.xml
COPY pom.xml .

# 复制 gamevault-common（包含所有文件）
COPY gamevault-common/pom.xml ./gamevault-common/
COPY gamevault-common/src ./gamevault-common/src

# 复制 gamevault-social（包含所有文件）
COPY gamevault-social/pom.xml ./gamevault-social/
COPY gamevault-social/src ./gamevault-social/src

# 编译（强制使用 -parameters）
RUN mvn clean package -DskipTests -Dmaven.compiler.parameters=true -pl gamevault-social -am

# ==========================================
# Stage 2: Runtime
# ==========================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN apk add --no-cache curl
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# 从 build stage 复制 JAR
COPY --from=build /app/gamevault-social/target/*.jar app.jar

RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8089

ENTRYPOINT ["java", "-jar", "app.jar"]