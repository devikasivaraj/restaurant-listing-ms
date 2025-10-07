# The previous image for Eureka used Java 17 (class file version 61.0).
# The Order App requires Java 21 (class file version 65.0).
# We are changing the base image to Java 21 to support all microservices.
FROM openjdk:21-jdk-slim

# Set the working directory inside the container
WORKDIR /opt

# Copy the application JAR file into the container
COPY target/*.jar /opt/app.jar

# Define the command to run the application when the container starts
ENTRYPOINT exec java $JAVA_OPTS -jar app.jar
