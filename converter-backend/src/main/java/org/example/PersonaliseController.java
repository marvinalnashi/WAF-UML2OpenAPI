package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class PersonaliseController {

    private final String outputPath = "/data/export.yml";

    @GetMapping("/personalise")
    public Map<String, Object> getPersonalisedData() {
        try {
            File file = new File(outputPath);
            if (!file.exists()) {
                throw new RuntimeException("File not found: " + outputPath);
            }
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            return mapper.readValue(file, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve OpenAPI specification: " + e.getMessage(), e);
        }
    }

    @PostMapping("/updateExample")
    public void updateExample(@RequestBody Map<String, Object> updateRequest) {
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            File file = new File(outputPath);
            Map<String, Object> openApiSpec = mapper.readValue(file, Map.class);

            String className = (String) updateRequest.get("className");
            String attributeName = (String) updateRequest.get("attributeName");
            int index = (Integer) updateRequest.get("index");
            String newValue = (String) updateRequest.get("newValue");

            Map<String, Object> schemas = (Map<String, Object>) ((Map<String, Object>) openApiSpec.get("components")).get("schemas");
            Map<String, Object> classSchema = (Map<String, Object>) schemas.get(className);
            Map<String, Object> examples = (Map<String, Object>) classSchema.get("examples");
            ((Map<String, Object>) ((java.util.List<Object>) examples.get("exampleArray")).get(index)).put(attributeName, newValue);

            mapper.writeValue(file, openApiSpec);
        } catch (IOException e) {
            throw new RuntimeException("Failed to update OpenAPI specification: " + e.getMessage(), e);
        }
    }
}