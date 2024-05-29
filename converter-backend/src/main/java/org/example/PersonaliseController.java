package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
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
}