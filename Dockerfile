# ---------- Stage 1: build ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
# Tai truoc dependency de tan dung Docker layer cache
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B clean package -DskipTests

# ---------- Stage 2: runtime ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S ktx && adduser -S ktx -G ktx
COPY --from=build /app/target/*.jar app.jar
USER ktx
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
