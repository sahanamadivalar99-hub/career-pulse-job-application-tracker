# Build stage using eclipse-temurin:21
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Install Maven to build the application
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Copy pom.xml and project source code
COPY pom.xml .
COPY src ./src

# Build target/job-tracker-1.0.0.jar
RUN mvn clean package -DskipTests -B

# Runtime stage using eclipse-temurin:21
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the built jar to target/job-tracker-1.0.0.jar
COPY --from=builder /app/target/job-tracker-1.0.0.jar target/job-tracker-1.0.0.jar

# Expose web port 8080
EXPOSE 8080

# Run target/job-tracker-1.0.0.jar on port 8080
ENTRYPOINT ["java", "-jar", "target/job-tracker-1.0.0.jar"]
