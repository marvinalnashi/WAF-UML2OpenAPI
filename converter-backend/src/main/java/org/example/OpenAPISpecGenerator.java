package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.util.*;

public class OpenAPISpecGenerator {

    public static String generateSpec(Map<String, List<String>> classes,
                                      Map<String, List<String>> attributes,
                                      Map<String, List<String>> methods,
                                      List<Map<String, Object>> mappings,
                                      String outputPath,
                                      Map<String, Map<String, Boolean>> selectedHttpMethods) throws Exception {
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
            Map<String, Object> components = new LinkedHashMap<>();
            Map<String, Object> schemas = new LinkedHashMap<>();

            for (String className : classes.keySet()) {
                List<String> classAttributes = attributes.getOrDefault(className, new ArrayList<>());
                List<String> classMethods = methods.getOrDefault(className, new ArrayList<>());

                Map<String, Object> classSchema = generateClassSchema(className, classAttributes);
                schemas.put(className, classSchema);

//                entities.forEach((className, classList) -> {
//                            addPathItem(paths, "/" + className.toLowerCase(), createPathItem("Get all instances of " + className, classList));
//
//                            if (attributes.containsKey(className)) {
//                                addPathItem(paths, "/" + className.toLowerCase() + "/attributes", createPathItem("Get attributes of " + className, attributes.get(className)));
//                            }
//
//                            if (methods.containsKey(className)) {
//                                addPathItem(paths, "/" + className.toLowerCase() + "/methods", createPathItem("Get methods of " + className, methods.get(className)));
//                            }

                Map<String, Boolean> selectedMethods = selectedHttpMethods.getOrDefault(className, new HashMap<>());
                for (Map.Entry<String, Boolean> entry : selectedMethods.entrySet()) {
                    if (Boolean.TRUE.equals(entry.getValue())) {
                        String method = entry.getKey().toUpperCase();
                        String lowerCaseMethod = method.toLowerCase();
                        String path = "/" + className.toLowerCase() + (method.equals("GET") || method.equals("DELETE") ? "/{id}" : "");
                        Map<String, Object> pathItem = (Map<String, Object>) paths.getOrDefault(path, new LinkedHashMap<>());
                        pathItem.put(lowerCaseMethod, createOperation(className, method));
                        paths.put(path, pathItem);
                    }
                }
            }

            components.put("schemas", schemas);
            openAPISpec.put("paths", paths);
            openAPISpec.put("components", components);

            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            mapper.writeValue(new File(outputPath), openAPISpec);

            return "OpenAPI specification generated successfully at " + outputPath;
        } catch (Exception e) {
            System.err.println("Error during OpenAPI specification generation: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private static Map<String, Object> generateClassSchema(String className, List<String> attributes) {
        Map<String, Object> properties = new LinkedHashMap<>();
        Map<String, Object> example = new LinkedHashMap<>();

        for (String attribute : attributes) {
            String[] parts = attribute.split(" ");
            String name = parts[0].substring(1);
            String type = parts[2];

            properties.put(name, Map.of("type", mapType(type)));
            example.put(name, generateExampleValue(type));
        }

        return Map.of(
                "type", "object",
                "properties", properties,
                "example", example
        );
    }

    private static String mapType(String type) {
        switch (type.toLowerCase()) {
            case "int":
                return "integer";
            case "string":
                return "string";
            case "long":
                return "integer";
            case "float":
                return "number";
            case "double":
                return "number";
            case "boolean":
                return "boolean";
            case "char":
                return "string";
            case "byte":
                return "string";
            case "short":
                return "integer";
            default:
                return "string";
        }
    }

    private static Object generateExampleValue(String type) {
        Random random = new Random();
        switch (type.toLowerCase()) {
            case "int":
            case "long":
            case "short":
                return random.nextInt(100);
            case "string":
                return "exampleString";
            case "float":
            case "double":
                return random.nextDouble() * 100;
            case "boolean":
                return random.nextBoolean();
            case "char":
                return (char) (random.nextInt(26) + 'a');
            case "byte":
                return (byte) random.nextInt(256);
            default:
                return "exampleString";
        }
    }

    private static Map<String, Object> createOperation(String className, String method) {
        Map<String, Object> operation = new LinkedHashMap<>();
        operation.put("tags", List.of(className));
        operation.put("summary", method + " operation for " + className);
        operation.put("description", "Performs " + method + " operation for " + className);

        if (method.equalsIgnoreCase("POST") || method.equalsIgnoreCase("PUT")) {
            operation.put("requestBody", Map.of(
                    "description", "Payload for " + method + " operation",
                    "required", true,
                    "content", Map.of(
                            "application/json", Map.of(
                                    "schema", Map.of("$ref", "#/components/schemas/" + className)
                            )
                    )
            ));
        }

        if (method.equalsIgnoreCase("GET") || method.equalsIgnoreCase("DELETE")) {
            List<Map<String, Object>> parameters = new ArrayList<>();
            parameters.add(Map.of(
                    "name", "id",
                    "in", "path",
                    "required", true,
                    "schema", Map.of("type", "string")
            ));
            operation.put("parameters", parameters);
        }

        operation.put("responses", Map.of(
                "200", Map.of(
                        "description", "Successful operation",
                        "content", Map.of(
                                "application/json", Map.of(
                                        "schema", Map.of("$ref", "#/components/schemas/" + className)
                                )
                        )
                )
        ));
        return operation;
    }
}
