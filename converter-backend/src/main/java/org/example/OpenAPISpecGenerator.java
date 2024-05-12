package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OpenAPISpecGenerator {
    public static String generateSpecWithElementsAndMappings(Map<String, List<String>> elements, List<Map<String, Object>> mappings, String outputPath) throws Exception {
        Map<String, Object> openAPISpec = createOpenAPISpec(mappings);
        Map<String, Object> paths = (Map<String, Object>) openAPISpec.get("paths");
        if (paths == null) paths = new LinkedHashMap<>();

        Map<String, Object> finalPaths = paths;
        elements.forEach((className, details) -> {
            List<String> attributes = details.size() > 0 ? details.subList(0, 1) : new ArrayList<>();
            List<String> methods = details.size() > 1 ? details.subList(1, details.size()) : new ArrayList<>();

            finalPaths.put("/" + className.toLowerCase() + "/instances",
                    createPathItem("Get all instances of " + className, details));
            finalPaths.put("/" + className.toLowerCase() + "/attributes",
                    createPathItem("Get attributes of " + className, attributes));
            finalPaths.put("/" + className.toLowerCase() + "/methods",
                    createPathItem("Get methods of " + className, methods));
        });

        openAPISpec.put("paths", paths);

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.writeValue(new File(outputPath), openAPISpec);

        return "OpenAPI specification generated successfully at " + outputPath;
    }

    private static Map<String, Object> createOpenAPISpec(List<Map<String, Object>> mappings) {
        Map<String, Object> openAPISpec = new LinkedHashMap<>();
        openAPISpec.put("openapi", "3.0.0");

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("title", "Generated API");
        info.put("version", "1.0.0");
        info.put("description", "API dynamically generated from UML.");
        openAPISpec.put("info", info);

        List<Map<String, String>> servers = new ArrayList<>();
        Map<String, String> server = new LinkedHashMap<>();
        server.put("url", "http://localhost:4010");
        servers.add(server);
        openAPISpec.put("servers", servers);

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
            paths.put("/" + url, pathItem);
        }

        openAPISpec.put("paths", paths);
        return openAPISpec;
    }

    private static Map<String, Object> createPathItem(String description, List<String> details) {
        Map<String, Object> pathItem = new LinkedHashMap<>();
        Map<String, Object> getOperation = new LinkedHashMap<>();
        getOperation.put("summary", description);
        getOperation.put("description", description);

        Map<String, Object> responses = new LinkedHashMap<>();
        Map<String, Object> response200 = new LinkedHashMap<>();
        response200.put("description", "Successful response");
        response200.put("content", Map.of(
                "application/json", Map.of(
                        "example", details
                )
        ));
        responses.put("200", response200);

        getOperation.put("responses", responses);
        pathItem.put("get", getOperation);
        return pathItem;
    }
}
