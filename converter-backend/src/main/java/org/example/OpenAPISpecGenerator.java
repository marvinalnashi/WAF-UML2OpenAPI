package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.util.*;

public class OpenAPISpecGenerator {

    public static String generateSpec(Map<String, List<String>> entities,
                                      Map<String, List<String>> attributes,
                                      Map<String, List<String>> methods,
                                      List<Map<String, Object>> mappings,
                                      String outputPath,
                                      Map<String, List<String>> selectedHttpMethods) throws Exception {
        try {
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
                addPathItem(paths, "/" + className.toLowerCase(), createPathItem("Get all instances of " + className, classList));

                if (attributes.containsKey(className)) {
                    addPathItem(paths, "/" + className.toLowerCase() + "/attributes", createPathItem("Get attributes of " + className, attributes.get(className)));
                }

                if (methods.containsKey(className)) {
                    addPathItem(paths, "/" + className.toLowerCase() + "/methods", createPathItem("Get methods of " + className, methods.get(className)));
                }

                List<String> selectedMethods = selectedHttpMethods.get(className);
                if (selectedMethods != null) {
                    for (String method : selectedMethods) {
                        switch (method.toUpperCase()) {
                            case "GET":
                                addPathItem(paths, "/" + className.toLowerCase() + "/{id}", createSimpleOperationWithId("GET"));
                                break;
                            case "POST":
                                addPathItem(paths, "/" + className.toLowerCase(), createSimpleOperation("POST"));
                                break;
                            case "PUT":
                                addPathItem(paths, "/" + className.toLowerCase() + "/{id}", createSimpleOperationWithId("PUT"));
                                break;
                            case "DELETE":
                                addPathItem(paths, "/" + className.toLowerCase() + "/{id}", createSimpleOperationWithId("DELETE"));
                                break;
                        }
                    }
                }
            });

            mappings.forEach(mapping -> {
                String className = (String) mapping.get("className");
                String baseUri = "/" + className.toLowerCase();

                String method = (String) mapping.get("method");
                if (method != null && !method.isEmpty()) {
                    addPathItem(paths, baseUri, Map.of(method.toLowerCase(), createOperation("Custom operation for " + className)));
                }

                List<String> attrList = (List<String>) mapping.get("attributes");
                if (attrList != null && !attrList.isEmpty()) {
                    String attrUri = baseUri + "/attributes";
                    addPathItem(paths, attrUri, Map.of("get", createAttributeOperation("Get attributes of " + className, attrList)));
                }

                List<String> methodList = (List<String>) mapping.get("methods");
                if (methodList != null && !methodList.isEmpty()) {
                    String methodUri = baseUri + "/methods";
                    addPathItem(paths, methodUri, Map.of("get", createMethodOperation("Get methods of " + className, methodList)));
                }
            });

            openAPISpec.put("paths", paths);

            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            mapper.writeValue(new File(outputPath), openAPISpec);

            return "OpenAPI specification generated successfully at " + outputPath;
        } catch (Exception e) {
            System.err.println("Error during OpenAPI specification generation: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
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

    private static Map<String, Object> createSimpleOperation(String method) {
        Map<String, Object> operation = new LinkedHashMap<>();
        operation.put("summary", method + " operation");
        operation.put("description", "Performs " + method + " operation");
        operation.put("responses", Map.of("200", Map.of("description", "Successful operation")));
        return Map.of(method.toLowerCase(), operation);
    }

    private static Map<String, Object> createSimpleOperationWithId(String method) {
        Map<String, Object> operation = new LinkedHashMap<>();
        operation.put("summary", method + " operation");
        operation.put("description", "Performs " + method + " operation");

        List<Map<String, Object>> parameters = new ArrayList<>();
        Map<String, Object> idParam = new LinkedHashMap<>();
        idParam.put("name", "id");
        idParam.put("in", "path");
        idParam.put("required", true);
        idParam.put("schema", Map.of("type", "string"));
        parameters.add(idParam);
        operation.put("parameters", parameters);

        operation.put("responses", Map.of("200", Map.of("description", "Successful operation")));
        return Map.of(method.toLowerCase(), operation);
    }

    private static Map<String, Object> createOperation(String description) {
        Map<String, Object> operation = new LinkedHashMap<>();
        operation.put("summary", description);
        operation.put("description", description);
        operation.put("responses", Map.of("200", Map.of("description", "Successful operation")));
        return operation;
    }

    private static Map<String, Object> createAttributeOperation(String description, List<String> attributes) {
        Map<String, Object> operation = new LinkedHashMap<>();
        operation.put("summary", description);
        operation.put("description", "Fetch all attributes for " + description);
        operation.put("responses", Map.of("200", Map.of("description", "Successful retrieval", "content", Map.of("application/json", Map.of("example", attributes)))));
        return operation;
    }

    private static Map<String, Object> createMethodOperation(String description, List<String> methods) {
        Map<String, Object> operation = new LinkedHashMap<>();
        operation.put("summary", description);
        operation.put("description", "Fetch all methods for " + description);
        operation.put("responses", Map.of("200", Map.of("description", "Successful retrieval", "content", Map.of("application/json", Map.of("example", methods)))));
        return operation;
    }

    private static void addPathItem(Map<String, Object> paths, String path, Map<String, Object> operation) {
        if (paths.containsKey(path)) {
            Map<String, Object> existingOperations = new LinkedHashMap<>((Map<String, Object>) paths.get(path));
            existingOperations.putAll(operation);
            paths.put(path, existingOperations);
        } else {
            paths.put(path, operation);
        }
    }
}
