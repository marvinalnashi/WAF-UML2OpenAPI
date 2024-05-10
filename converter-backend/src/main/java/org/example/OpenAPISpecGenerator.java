package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OpenAPISpecGenerator {

    public static String generateSpecWithMappings(Map<String, Object> mappings, String outputPath) throws Exception {
        if (mappings == null) {
            throw new IllegalArgumentException("Mappings cannot be null");
        }

        Map<String, Object> openAPISpec = new LinkedHashMap<>();
        openAPISpec.put("openapi", "3.0.0");

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("title", "Dynamically Generated API");
        info.put("version", "1.0.0");
        info.put("description", "API dynamically generated from UML diagram mappings.");
        openAPISpec.put("info", info);

        List<Map<String, String>> servers = new ArrayList<>();
        Map<String, String> server = new LinkedHashMap<>();
        server.put("url", "http://localhost:4010");
        servers.add(server);
        openAPISpec.put("servers", servers);

        Map<String, Object> paths = generatePathsFromMappings(mappings);
        openAPISpec.put("paths", paths);

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.writeValue(new File(outputPath), openAPISpec);

        return "OpenAPI specification generated successfully at " + outputPath;
    }

    // Helper method to generate paths object for OpenAPI spec
    private static Map<String, Object> generatePathsFromMappings(Map<String, Object> mappings) {
        Map<String, Object> paths = new LinkedHashMap<>();
        mappings.forEach((className, details) -> {
            Map<String, Object> pathDetails = (Map<String, Object>) details;
            List<Map<String, Object>> methods = (List<Map<String, Object>>) pathDetails.get("methods");
            Map<String, Object> pathItem = new LinkedHashMap<>();

            methods.forEach(method -> {
                Map<String, Object> operation = new LinkedHashMap<>();
                operation.put("summary", "Operation for " + method.get("method"));
                operation.put("description", "Performs " + method.get("method") + " on " + className);

                Map<String, Object> responses = new LinkedHashMap<>();
                Map<String, Object> response200 = new LinkedHashMap<>();
                response200.put("description", "Successful response");
                response200.put("content", Map.of("application/json", Map.of("schema", Map.of("type", "object"))));
                responses.put("200", response200);

                operation.put("responses", responses);
                pathItem.put((String) method.get("method").toString().toLowerCase(), operation);
            });

            paths.put("/" + className.toString().toLowerCase(), pathItem);
        });

        return paths;
    }
}
