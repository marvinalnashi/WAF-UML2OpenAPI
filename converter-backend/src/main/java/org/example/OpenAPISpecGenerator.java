package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.util.*;

public class OpenAPISpecGenerator {

    private static Map<String, Object> lastGeneratedSpec = new HashMap<>();

    public static String generateSpec(Map<String, List<String>> entities,
                                      Map<String, List<String>> attributes,
                                      Map<String, List<String>> methods,
                                      Map<String, Object> mappings,
                                      String outputPath) throws Exception {
        Map<String, Object> openAPISpec = buildInitialSpec(entities, attributes, methods);
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
        lastGeneratedSpec = openAPISpec;
        saveSpecToFile(openAPISpec, outputPath);
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

    public static String applyMappings(Map<String, Object> mappings) throws Exception {
        Map<String, Object> paths = (Map<String, Object>) lastGeneratedSpec.get("paths");
        for (String key : mappings.keySet()) {
            Map<String, Object> newDetails = (Map<String, Object>) mappings.get(key);
            paths.put(key, newDetails);
        }
        lastGeneratedSpec.put("paths", paths);
        saveSpecToFile(lastGeneratedSpec, "output.yml");
        return "OpenAPI specification updated successfully.";
    }

    private static Map<String, Object> buildInitialSpec(Map<String, List<String>> entities,
                                                        Map<String, List<String>> attributes,
                                                        Map<String, List<String>> methods) {
        Map<String, Object> openAPISpec = new HashMap<>();
        Map<String, Object> paths = new HashMap<>();
        Map<String, Object> components = new HashMap<>();
        Map<String, Object> schemas = new HashMap<>();

        entities.forEach((entityName, entityDetails) -> {
            Map<String, Object> pathItem = new HashMap<>();
            Map<String, Object> getOperation = new HashMap<>();
            List<Map<String, Object>> parameters = new ArrayList<>();

            Map<String, Object> idParam = new HashMap<>();
            idParam.put("name", "id");
            idParam.put("in", "path");
            idParam.put("required", true);
            idParam.put("schema", Map.of("type", "string"));
            parameters.add(idParam);

            getOperation.put("summary", "Get a single " + entityName + " by ID");
            getOperation.put("operationId", "get" + entityName);
            getOperation.put("tags", List.of(entityName));
            getOperation.put("parameters", parameters);
            getOperation.put("responses", Map.of(
                    "200", Map.of("description", "successful operation",
                            "content", Map.of("application/json",
                                    Map.of("schema", Map.of("$ref", "#/components/schemas/" + entityName)))),
                    "404", Map.of("description", "Entity not found")
            ));

            pathItem.put("get", getOperation);
            paths.put("/" + entityName.toLowerCase() + "/{id}", pathItem);

            Map<String, Object> schema = new HashMap<>();
            Map<String, Object> properties = new HashMap<>();
            attributes.getOrDefault(entityName, new ArrayList<>()).forEach(attr -> {
                properties.put(attr, Map.of("type", "string"));
            });
            schema.put("type", "object");
            schema.put("properties", properties);
            schemas.put(entityName, schema);
        });

        components.put("schemas", schemas);
        openAPISpec.put("paths", paths);
        openAPISpec.put("components", components);
        openAPISpec.put("openapi", "3.0.0");
        openAPISpec.put("info", Map.of("title", "Generated API", "version", "1.0.0"));

        return openAPISpec;
    }

    private static void saveSpecToFile(Map<String, Object> spec, String outputPath) throws Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.writeValue(new File(outputPath), spec);
    }

    private static Map<String, Object> buildPathItem(Map<String, Object> pathDetails) {
        Map<String, Object> item = new HashMap<>();
        Map<String, Object> operation = new HashMap<>();
        operation.put("summary", pathDetails.get("summary"));
        operation.put("description", pathDetails.get("description"));
        operation.put("operationId", "customOperation" + pathDetails.hashCode());
        operation.put("tags", List.of("Custom"));
        operation.put("responses", Map.of(
                "200", Map.of("description", "successful operation",
                        "content", Map.of("application/json",
                                Map.of("schema", Map.of("type", "object", "properties", Map.of("data", "Dynamic data")))))
        ));

        item.put(pathDetails.get("method").toString().toLowerCase(), operation);
        return item;
    }


    public static Map<String, Object> getLastGeneratedSpec() {
        return lastGeneratedSpec;
    }
}
