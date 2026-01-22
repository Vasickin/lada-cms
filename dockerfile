# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
# Copy only the pom and download dependencies to utilize Docker cache
COPY pom.xml .
RUN mvn dependency:go-offline -B
# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create the slim runtime image
FROM eclipse-temurin:21-jre-jammy AS release
WORKDIR /app
# Copy the JAR from the build stage
COPY --from=build /app/target/*.jar app.jar
# Run as a non-root user for security
RUN useradd -m springuser
USER springuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
