# Dockerfile for Prism Mock Server
FROM node:21

# Install Prism CLI globally
RUN npm install -g @stoplight/prism-cli

WORKDIR /app

# Keeps the container running indefinitely.
CMD ["tail", "-f", "/dev/null"]
