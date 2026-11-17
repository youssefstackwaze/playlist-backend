# Backend only — the Angular front-end (../frontend) is deployed separately as a static site.
# Build context: this backend/ folder.

# ---------- 1. Build the Spring Boot jar ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml ./
RUN mvn -q -B dependency:go-offline
COPY src ./src
# Tests are run locally / in CI (mvn test); skipped here to keep the build fast.
RUN mvn -q -B -DskipTests package && cp target/*.jar app.jar

# ---------- 2. Runtime image ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
USER app
COPY --from=build /app/app.jar app.jar
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC -Xss512k -XX:TieredStopAtLevel=1"
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
