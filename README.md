Welcome to the WeAreFrank! OpenAPI specification generator application!
Please read everything below to get the application up and running on your system.

Docker Desktop is required for this application to work.
The following Docker containers are created and used to build and start this application successfully:

- converter-frontend-1
- converter-backend-1
- pgadmin-1
- db-1

To build and serve the application with all of its containers, simply run the command "docker-compose up --build". Please take into account that the build times can be up to 5 minutes, depending on the system the application is building and running on.

The application makes use of a PostgreSQL database. You can manage this database with the pdAdmin interface in your browser, which you can access here: http://localhost:5050/browser/
The default username for pgAdmin is: admin@example.com
The default password for pgAdmin is: admin

The user interface of the application can be accessed at: http://localhost:4200/
The built-in Swagger UI API documentation tool can be accessed from the Manage step of the stepper or manually at: http://localhost:8080/swagger-ui/index.html

The backend uses Javadoc and the frontend uses Compodoc for visualising the documentations.
To generate the Compodoc documentations in HTML files which you can access in your browser, run the command "npm run compodoc" command in your converter-frontend/UMLtoOpenAPISpec/ directory.
To access the generated Compodoc documentations in HTML files, serve them by running the command "npm run docs" and access them at http://127.0.0.1:9000/index.html
You can generate similar HTML documentation files that are viewable in your browser for the backend code through the "Generate docs" option in your IDE.

Also make sure that you have an application.properties file in the converter-backend/src/main/resources directory that uses the following format: 

springdoc.swagger-ui.url=http://localhost:8080/export.yml
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB

management.endpoints.web.exposure.include=health
management.endpoint.health.show-details=always

openai.api.key=YOUROPENAIAPIKEYHERE

spring.datasource.url=jdbc:postgresql://db:5432/wafumlopenapi
spring.datasource.username=YOURUSERNAMEHERE
spring.datasource.password=YOURPASSWORDHERE
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
