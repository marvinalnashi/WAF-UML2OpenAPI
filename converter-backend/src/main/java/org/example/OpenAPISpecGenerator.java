package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OpenAPISpecGenerator {

    public String generateSpecWithElementsAndMappings(Map<String, List<String>> elements,
                                                      List<Map<String, Object>> mappings,
                                                      String outputPath) throws Exception {
        Map<String, Object> openAPISpec = createInitialSpec();
        Map<String, Object> paths = new LinkedHashMap<>();

        elements.forEach((className, details) -> {
            paths.put("/" + className.toLowerCase() + "/instances",
                    createPathItem("Get all instances of " + className, details));

            if (!details.isEmpty()) {
                paths.put("/" + className.toLowerCase() + "/attributes",
                        createPathItem("Get attributes of " + className, details.subList(0, 1)));
                if (details.size() > 1) {
                    paths.put("/" + className.toLowerCase() + "/methods",
                            createPathItem("Get methods of " + className, details.subList(1, details.size())));
                }
            }
        });

        mappings.forEach(mapping -> {
            String url = "/" + mapping.get("url");
            paths.put(url, createMappedPathItem(mapping));
        });

        openAPISpec.put("paths", paths);
        writeSpecToFile(openAPISpec, outputPath);

        return "OpenAPI specification generated successfully at " + outputPath;
    }

    private Map<String, Object> createInitialSpec() {
        Map<String, Object> openAPISpec = new LinkedHashMap<>();
        openAPISpec.put("openapi", "3.0.0");

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("title", "Generated API");
        info.put("version", "1.0.0");
        info.put("description", "API dynamically generated from UML.");
        openAPISpec.put("info", info);

        List<Map<String, String>> servers = new ArrayList<>();
        servers.add(Map.of("url", "http://localhost:4010"));
        openAPISpec.put("servers", servers);

        return openAPISpec;
    }

    private Map<String, Object> createPathItem(String description, List<String> details) {
        Map<String, Object> getOperation = new LinkedHashMap<>();
        getOperation.put("summary", description);
        getOperation.put("description", description);
        getOperation.put("responses", Map.of(
                "200", Map.of(
                        "description", "Successful response",
                        "content", Map.of(
                                "application/json", Map.of(
                                        "example", details
                                )
                        )
                )
        ));

        return Map.of("get", getOperation);
    }

    private Map<String, Object> createMappedPathItem(Map<String, Object> mapping) {
        String method = (String) mapping.get("method");
        Map<String, Object> methodDetails = new LinkedHashMap<>();
        methodDetails.put("summary", "Custom operation for " + mapping.get("className"));
        methodDetails.put("description", "Performs " + method + " on " + mapping.get("className"));
        methodDetails.put("responses", Map.of(
                "200", Map.of("description", "Successful operation")
        ));

        return Map.of(method.toLowerCase(), methodDetails);
    }

    private void writeSpecToFile(Map<String, Object> spec, String path) throws Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.writeValue(new File(path), spec);
    }
}
