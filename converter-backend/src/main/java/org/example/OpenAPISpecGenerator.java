package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OpenAPISpecGenerator {

    public static String generateSpec(Map<String, List<String>> entities,
                                      Map<String, List<String>> attributes,
                                      Map<String, List<String>> methods,
                                      String outputPath) throws Exception {
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
        entities.forEach((className, classList) -> {
            paths.put("/" + className.toLowerCase(),
                    createPathItem("Get all instances of " + className, classList));

            if (attributes.containsKey(className)) {
                paths.put("/" + className.toLowerCase() + "/attributes",
                        createPathItem("Get attributes of " + className, attributes.get(className)));
            }

            if (methods.containsKey(className)) {
                paths.put("/" + className.toLowerCase() + "/methods",
                        createPathItem("Get methods of " + className, methods.get(className)));
            }
        });

        openAPISpec.put("paths", paths);

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.writeValue(new File(outputPath), openAPISpec);

        return "OpenAPI specification generated successfully at " + outputPath;
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
