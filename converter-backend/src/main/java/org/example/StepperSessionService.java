package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for managing stepper sessions.
 */
@Service
public class StepperSessionService {

    /**
     * Reference variable for the repository for entities related to stepper sessions.
     */
    @Autowired
    private StepperSessionRepository repository;

    /**
     * Saves the current stepper session.
     *
     * @param umlDiagram The UML diagram that is uploaded during the current stepper session.
     * @param openApiSpec The OpenAPI specification that is generated during the current stepper session.
     * @return The saved stepper session.
     */
    public StepperSession saveSession(String umlDiagram, String openApiSpec) {
        StepperSession session = new StepperSession();
        session.setUmlDiagram(umlDiagram);
        session.setOpenApiSpec(openApiSpec);
        session.setCreatedAt(LocalDateTime.now());
        return repository.save(session);
    }

    /**
     * Retrieves a specific stepper session by its ID.
     *
     * @param id The ID of the retrieved stepper session.
     * @return The retrieved stepper session.
     */
    public StepperSession getSession(Long id) {
        return repository.findById(id).orElse(null);
    }

    /**
     * Extracts classes and their corresponding attributes from an OpenAPI specification that was generated during a stepper session and creates a string that describes them for the specified stepper session.
     *
     * @param openApiSpec The OpenAPI specification from which its classes and attributes are extracted.
     * @return String that gives a summary about the classes and their corresponding attributes that are extracted from the specified stepper session.
     * @throws IOException Is returned if an error occurs during the extraction process.
     */
    public String extractClassesAndAttributes(String openApiSpec) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(openApiSpec);
        StringBuilder result = new StringBuilder("An OpenAPI specification that has ");

        JsonNode componentsNode = rootNode.path("components").path("schemas");
        if (componentsNode.isMissingNode()) {
            return "No classes found in OpenAPI specification.";
        }

        Iterator<Map.Entry<String, JsonNode>> fields = componentsNode.fields();
        int classCount = 0;

        while (fields.hasNext()) {
            classCount++;
            Map.Entry<String, JsonNode> field = fields.next();
            String className = field.getKey();
            JsonNode classNode = field.getValue();

            result.append("the class ").append(className).append(" (with ");

            JsonNode propertiesNode = classNode.path("properties");
            if (propertiesNode.isMissingNode()) {
                result.append("no attributes");
            } else {
                int attributeCount = 0;
                Iterator<Map.Entry<String, JsonNode>> properties = propertiesNode.fields();
                while (properties.hasNext()) {
                    attributeCount++;
                    Map.Entry<String, JsonNode> property = properties.next();
                    String attributeName = property.getKey();
                    result.append(attributeName);

                    if (properties.hasNext()) {
                        result.append(", ");
                    }
                }
                result.append(" attribute").append(attributeCount > 1 ? "s" : "");
            }
            result.append(")");
            if (fields.hasNext()) {
                result.append(", ");
            }
        }

        if (classCount == 1) {
            result.insert(result.indexOf("the class"), "one ");
        } else {
            result.insert(result.indexOf("the class"), classCount + " classes, including ");
        }

        return result.toString();
    }

    /**
     * Retrieves all saved stepper sessions.
     *
     * @return List that contains all saved stepper sessions.
     */
    public List<StepperSession> getAllSessions() {
        return repository.findAll().stream().map(session -> {
            try {
                String classesAndAttributes = extractClassesAndAttributes(session.getOpenApiSpec());
                session.setOpenApiSpec(classesAndAttributes);
            } catch (IOException e) {
                e.printStackTrace();
                session.setOpenApiSpec("Error processing OpenAPI specification");
            }
            return session;
        }).collect(Collectors.toList());
    }
}
