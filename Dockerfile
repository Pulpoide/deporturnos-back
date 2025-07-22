# --- Fase de Construcción (Build Stage) ---
FROM gradle:jdk20-alpine AS builder
WORKDIR /home/gradle/src
COPY --chown=gradle:gradle gradlew gradle/ build.gradle settings.gradle ./
COPY --chown=gradle:gradle src/ src/
RUN chmod +x gradlew \
    && ./gradlew bootJar --no-daemon

# --- Fase de Ejecución (Runtime Stage) ---
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S spring && adduser -S -G spring spring

WORKDIR /app
USER spring:spring

EXPOSE 8080

COPY --from=builder --chown=spring:spring \
     /home/gradle/src/build/libs/*.jar app.jar

ENTRYPOINT ["java",
  "-XX:+UseContainerSupport",
  "-XX:MaxRAMPercentage=75.0",
  "-XX:+UseG1GC",
  "-jar","app.jar"]