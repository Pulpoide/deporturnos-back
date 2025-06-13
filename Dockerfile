# --- Fase de Construcción (Build Stage) ---
FROM gradle:jdk20-alpine AS builder
WORKDIR /app
COPY gradlew .
COPY gradle gradle/
COPY build.gradle settings.gradle ./
COPY src src/
RUN chmod +x ./gradlew
RUN ./gradlew bootJar --no-daemon

# --- Fase de Ejecución (Runtime Stage) ---
FROM eclipse-temurin:21-jre-jammy
RUN groupadd --system spring && useradd --system --gid spring spring
USER spring
WORKDIR /app
EXPOSE 8080
COPY --from=builder --chown=spring:spring /app/build/libs/*.jar ./app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]