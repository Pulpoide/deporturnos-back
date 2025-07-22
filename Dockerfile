# --- Fase de Construcción (Build Stage) ---
FROM gradle:jdk20-alpine AS builder
WORKDIR /home/gradle/src

COPY gradlew ./
COPY gradlew.bat ./
COPY gradle/wrapper/ gradle/wrapper/
COPY build.gradle settings.gradle ./
COPY src/ src/

RUN chmod +x gradlew
RUN ./gradlew bootJar --no-daemon

# --- Fase de Ejecución (Runtime Stage) ---
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S spring && adduser -S -G spring spring

WORKDIR /app
USER spring:spring
EXPOSE 8080

COPY --from=builder --chown=spring:spring \
     /home/gradle/src/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-XX:+UseG1GC", "-jar", "app.jar"]
