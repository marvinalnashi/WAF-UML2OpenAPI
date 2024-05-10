package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OpenAPISpecGenerator {

    public static String generateSpecWithMappings(List<Map<String, Object>> mappings, String outputPath) throws Exception {
        if (mappings == null) {
            throw new IllegalArgumentException("Mappings cannot be null");
        }

        Map<String, Object> openAPISpec = new LinkedHashMap<>();
        openAPISpec.put("openapi", "3.0.0");
        Map<String, Object> paths = new LinkedHashMap<>();

        for (Map<String, Object> mapping : mappings) {
            String className = (String) mapping.get("className");
            String method = (String) mapping.get("method");
            String url = (String) mapping.get("url");

            if (method == null) {
                continue;
            }

            Map<String, Object> methodDetails = new LinkedHashMap<>();
            methodDetails.put("summary", "Operation for " + className);
            methodDetails.put("description", "Performs " + method + " on " + className);

            Map<String, Object> responses = new LinkedHashMap<>();
            responses.put("200", Map.of("description", "Successful Operation"));

            Map<String, Object> pathItem = new LinkedHashMap<>();
            pathItem.put(method.toLowerCase(), methodDetails);
            paths.put(url, pathItem);
        }

        openAPISpec.put("paths", paths);

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.writeValue(new File(outputPath), openAPISpec);

        return "OpenAPI specification generated successfully at " + outputPath;
    }

    private static Map<String, Object> generatePathsFromMappings(List<Map<String, Object>> mappings) {
        Map<String, Object> paths = new LinkedHashMap<>();
        for (Map<String, Object> mapping : mappings) {
            String className = (String) mapping.get("className");
            Map<String, Object> pathDetails = new LinkedHashMap<>();
            pathDetails.put("method", mapping.get("method"));
            pathDetails.put("url", mapping.get("url"));
            paths.put("/" + className.toLowerCase(), pathDetails);
        }
        return paths;
    }
}
