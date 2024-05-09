package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.util.*;

public class OpenAPISpecGenerator {

    public static String generateSpec(Map<String, List<String>> entities,
                                      Map<String, List<String>> attributes,
                                      Map<String, List<String>> methods,
                                      Map<String, Object> mappings,
                                      String outputPath) throws Exception {
        Map<String, Object> openAPISpec = new HashMap<>();
        openAPISpec.put("openapi", "3.0.0");

        Map<String, Object> info = new HashMap<>();
        info.put("title", "Generated API");
        info.put("version", "1.0.0");
        info.put("description", "API dynamically generated from UML.");
        openAPISpec.put("info", info);

        List<Map<String, String>> servers = new ArrayList<>();
        Map<String, String> server = new LinkedHashMap<>();
        server.put("url", "http://localhost:4010");
        servers.add(server);
        openAPISpec.put("servers", servers);

        Map<String, Object> paths = new HashMap<>();
        entities.forEach((className, classList) -> {
            String basePath = mappings.containsKey(className) ? (String) mappings.get(className) : className.toLowerCase();
            paths.put("/" + basePath, createPathItem("Get all instances of " + className, classList, mappings));

            if (attributes.containsKey(className)) {
                paths.put("/" + basePath + "/attributes", createPathItem("Get attributes of " + className, attributes.get(className), mappings));
            }

            if (methods.containsKey(className)) {
                paths.put("/" + basePath + "/methods", createPathItem("Get methods of " + className, methods.get(className), mappings));
            }
        });

        openAPISpec.put("paths", paths);

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.writeValue(new File(outputPath), openAPISpec);

        return "OpenAPI specification generated successfully at " + outputPath;
    }

    // Helper method to create a path item with dynamic descriptions that are based on mappings
    private static Map<String, Object> createPathItem(String description, List<String> details, Map<String, Object> mappings) {
        Map<String, Object> pathItem = new HashMap<>();
        Map<String, Object> getOperation = new HashMap<>();
        getOperation.put("summary", description);
        getOperation.put("description", description);

        Map<String, Object> responses = new HashMap<>();
        Map<String, Object> response200 = new HashMap<>();
        response200.put("description", "Successful response");
        response200.put("content", Map.of("application/json", Map.of("example", details)));
        responses.put("200", response200);

        getOperation.put("responses", responses);
        pathItem.put("get", getOperation);

        return pathItem;
    }
}
