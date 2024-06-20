# First stage: Build the application using Maven
FROM maven:3.8.5-openjdk-17 AS build

# Set the working directory in the container
WORKDIR /app

# Set the working directory in the container
COPY . .

# Package the application
RUN mvn clean package -DskipTests

# Verify the JAR file is created
RUN ls -la target/

# Second stage: Create a slim runtime image
FROM openjdk:17-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the JAR file from the first stage to the second stage
COPY --from=build /app/target/registration-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8080 to the outside world
EXPOSE 8080

# Run the JAR file
ENTRYPOINT ["java", "-jar", "app.jar"]
