# Use a Node.js image to build and serve the Angular application
FROM node:21

# Set the working directory in the container
WORKDIR /app

# Copy the package.json and package-lock.json to install dependencies
COPY UMLtoOpenAPISpec/package.json UMLtoOpenAPISpec/package-lock.json ./

# Install all dependencies including Express
RUN npm install

# Copy the rest of the application's source code
COPY UMLtoOpenAPISpec/ ./

# Install necessary dependencies for running Google Chrome in headless mode
RUN apt-get update && apt-get install -y \
    wget \
    gnupg \
    && rm -rf /var/lib/apt/lists/*

# Install Google Chrome
RUN wget -q -O - https://dl.google.com/linux/linux_signing_key.pub | apt-key add - \
    && sh -c 'echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" > /etc/apt/sources.list.d/google-chrome.list' \
    && apt-get update \
    && apt-get install -y google-chrome-stable --no-install-recommends \
    && rm -rf /var/lib/apt/lists/*

# Build the Angular application
RUN npm run build

# Install Express to serve the built application
RUN npm install express --save

# Expose the port the Express server will run on
EXPOSE 80

# Command to run the Express server
CMD ["node", "server.js"]
