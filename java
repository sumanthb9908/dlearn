# ============================================
# STAGE 1: Builder (Build Application)
# ============================================
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

# Set working directory
WORKDIR /build

# Copy pom.xml first (for layer caching)
COPY pom.xml .

# Download dependencies (cached if pom.xml doesn't change)
RUN mvn dependency:go-offline

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests


# ============================================
# STAGE 2: Runtime (Final Image)
# ============================================
FROM eclipse-temurin:21-jre-alpine

# Create non-root user
RUN addgroup -g 1000 appgroup && \
    adduser -D -u 1000 -G appgroup appuser

# Set working directory
WORKDIR /app

# Copy JAR file from builder stage
COPY --from=builder /build/target/*.jar app.jar

# Change ownership to non-root user
RUN chown appuser:appgroup app.jar

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Run application
CMD ["java", "-jar", "app.jar"]
