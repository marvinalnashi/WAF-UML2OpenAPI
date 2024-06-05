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

@Service
public class StepperSessionService {

    @Autowired
    private StepperSessionRepository repository;

    public StepperSession saveSession(String umlDiagram, String openApiSpec) {
        StepperSession session = new StepperSession();
        session.setUmlDiagram(umlDiagram);
        session.setOpenApiSpec(openApiSpec);
        session.setCreatedAt(LocalDateTime.now());
        return repository.save(session);
    }

    public StepperSession getSession(Long id) {
        return repository.findById(id).orElse(null);
    }

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