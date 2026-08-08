# Fase 1: Usamos Gradle 9 con JDK 21
FROM gradle:9.0-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon -x test

# Fase 2: Ejecutar la aplicación ligera con JRE 21
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]