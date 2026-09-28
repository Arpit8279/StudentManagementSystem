# ── Stage 1: Build the JAR ────────────────────────────────────────────────────
FROM eclipse-temurin:19-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and pom.xml first (layer cache — only re-downloads deps on pom.xml change)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

# Copy source and build (skip tests — tests run in CI, not during image build)
COPY src ./src
RUN ./mvnw package -DskipTests -B

# ── Stage 2: Minimal runtime image ───────────────────────────────────────────
FROM eclipse-temurin:19-jre-alpine

WORKDIR /app

# Create a non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy only the built JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the default Spring Boot port
EXPOSE 8080

# Start the application
ENTRYPOINT ["java", "-jar", "app.jar"]
