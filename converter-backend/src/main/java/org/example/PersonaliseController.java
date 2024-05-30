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

            updatePaths(openApiSpec, className, attributeName, index, newValue);

            mapper.writeValue(file, openApiSpec);
        } catch (IOException e) {
            throw new RuntimeException("Failed to update OpenAPI specification: " + e.getMessage(), e);
        }
    }

    private void updatePaths(Map<String, Object> openApiSpec, String className, String attributeName, int index, String newValue) {
        Map<String, Object> paths = (Map<String, Object>) openApiSpec.get("paths");
        for (Map.Entry<String, Object> pathEntry : paths.entrySet()) {
            Map<String, Object> methods = (Map<String, Object>) pathEntry.getValue();
            for (Map.Entry<String, Object> methodEntry : methods.entrySet()) {
                Map<String, Object> responses = (Map<String, Object>) ((Map<String, Object>) methodEntry.getValue()).get("responses");
                if (responses != null) {
                    for (Map.Entry<String, Object> responseEntry : responses.entrySet()) {
                        Map<String, Object> content = (Map<String, Object>) ((Map<String, Object>) responseEntry.getValue()).get("content");
                        if (content != null) {
                            for (Map.Entry<String, Object> contentEntry : content.entrySet()) {
                                Map<String, Object> examples = (Map<String, Object>) ((Map<String, Object>) contentEntry.getValue()).get("examples");
                                if (examples != null && examples.get("exampleArray") != null) {
                                    java.util.List<Object> exampleArray = (java.util.List<Object>) ((Map<String, Object>) examples.get("exampleArray")).get("value");
                                    if (exampleArray.size() > index) {
                                        Map<String, Object> example = (Map<String, Object>) exampleArray.get(index);
                                        if (example.containsKey(attributeName)) {
                                            example.put(attributeName, newValue);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}