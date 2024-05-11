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
        Map<String, Object> openAPISpec = createOpenAPISpec(mappings);

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.writeValue(new File(outputPath), openAPISpec);

        return "OpenAPI specification generated successfully at " + outputPath;
    }

    public static String generateSpecWithElementsAndMappings(Map<String, List<String>> elements, List<Map<String, Object>> mappings, String outputPath) throws Exception {
        Map<String, Object> openAPISpec = createOpenAPISpec(mappings);
        Map<String, Object> paths = (Map<String, Object>) openAPISpec.get("paths");
        if (paths == null) paths = new LinkedHashMap<>();

        Map<String, Object> finalPaths = paths;
        elements.forEach((className, details) -> {
            details.forEach(method -> {
                Map<String, Object> methodDetails = new LinkedHashMap<>();
                methodDetails.put("summary", "Operation for " + className);
                methodDetails.put("description", "Automatically generated operation for " + className);

                Map<String, Object> responses = new LinkedHashMap<>();
                responses.put("200", Map.of("description", "Successful Operation"));

                finalPaths.put("/" + className.toLowerCase(), Map.of("get", methodDetails));
            });
        });

        openAPISpec.put("paths", paths);

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.writeValue(new File(outputPath), openAPISpec);

        return "OpenAPI specification generated successfully at " + outputPath;
    }

    private static Map<String, Object> createOpenAPISpec(List<Map<String, Object>> mappings) {
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
        return openAPISpec;
    }
}
