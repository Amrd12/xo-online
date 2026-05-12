FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
# Download dependencies
RUN mvn dependency:go-offline -B
# Copy source code
COPY src ./src
# Build the application
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
# Copy the built jar from the build stage
COPY --from=build /app/target/xo-0.0.1-SNAPSHOT.jar app.jar
# Expose the application port
EXPOSE 8000
# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
