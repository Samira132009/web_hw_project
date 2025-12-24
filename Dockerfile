FROM gradle:8.5-jdk17-alpine AS build
WORKDIR /app
COPY . .
RUN gradle build --no-daemon -x test

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN apk add --no-cache curl

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/build/libs/blog-api.jar app.jar


RUN mkdir -p /app/logs /app/data

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/api/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]