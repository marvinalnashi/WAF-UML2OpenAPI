# Use a multi-stage build to first build the Java application
# Stage 1: Build the application
FROM maven:3.8.4-openjdk-11-slim as builder

# Copy the source code
COPY ./converter-backend /build
WORKDIR /build

# Package the application
RUN mvn clean package

# Stage 2: Setup the runtime container
FROM openjdk:11-slim

# Install necessary packages for fetching Docker's GPG key, adding the Docker repository, and installing Node.js
RUN apt-get update && \
    apt-get install -y \
    apt-transport-https \
    ca-certificates \
    curl \
    gnupg-agent \
    software-properties-common \
    nodejs \
    npm

# Add Docker’s official GPG key
RUN curl -fsSL https://download.docker.com/linux/debian/gpg | apt-key add -

# Set up the Docker stable repository
RUN add-apt-repository \
   "deb [arch=amd64] https://download.docker.com/linux/debian \
   $(lsb_release -cs) \
   stable"

# Install Docker Engine
RUN apt-get update && \
    apt-get install -y docker-ce docker-ce-cli containerd.io

# Existing Dockerfile steps for setting up the Java application environment

# Install a specific stable version of Node.js if necessary
RUN curl -sL https://deb.nodesource.com/setup_18.x | bash - && \
    apt-get install -y nodejs

# Uninstall and Reinstall Prism CLI with a specific version
RUN npm uninstall -g @stoplight/prism-cli && \
    npm install -g @stoplight/prism-cli@4.10.0

# Set the working directory to /app
WORKDIR /app

# Copy the built JAR file from the builder stage
COPY --from=builder /build/target/*.jar /app/app.jar

# Copy the data directory to the container
COPY ./data /data

# Expose the port the application uses
EXPOSE 8080

# Command to run the application
CMD ["java", "-jar", "/app/app.jar"]

