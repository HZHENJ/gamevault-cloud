FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN apk add --no-cache curl
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY gamevault-social/target/*.jar app.jar
RUN chown -R appuser:appgroup /app
USER appuser
EXPOSE 8089
ENTRYPOINT ["java", "-jar", "app.jar"]