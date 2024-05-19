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

                String lowerCaseClassName = className.toLowerCase();
                String path = "/" + lowerCaseClassName;
                Map<String, Object> pathItem = new LinkedHashMap<>();
                pathItem.put("get", createGetAllOperation(className, classAttributes));
                paths.put(path, pathItem);

                Map<String, Boolean> selectedMethods = selectedHttpMethods.getOrDefault(className, new HashMap<>());
                for (Map.Entry<String, Boolean> entry : selectedMethods.entrySet()) {
                    if (Boolean.TRUE.equals(entry.getValue())) {
                        String method = entry.getKey().toUpperCase();
                        String lowerCaseMethod = method.toLowerCase();
                        String methodPath = "/" + lowerCaseClassName + (method.equals("GET") || method.equals("DELETE") || method.equals("PUT") ? "/{id}" : "");
                        Map<String, Object> methodPathItem = (Map<String, Object>) paths.getOrDefault(methodPath, new LinkedHashMap<>());
                        methodPathItem.put(lowerCaseMethod, createOperation(className, method, classSchema, method.equals("GET")));
                        paths.put(methodPath, methodPathItem);
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
        List<Map<String, Object>> exampleArray = new ArrayList<>();

        for (int i = 1; i <= 7; i++) {
            Map<String, Object> exampleItem = new LinkedHashMap<>();
            exampleItem.put("id", i);

            for (String attribute : attributes) {
                String[] parts = attribute.split(" ");
                String name = parts[0].substring(1);
                String type = parts[2];
                String format = getTypeFormat(type);

                Map<String, Object> attributeSchema = new LinkedHashMap<>();
                attributeSchema.put("type", mapType(type));
                if (format != null) {
                    attributeSchema.put("format", format);
                }
                properties.put(name, attributeSchema);
                exampleItem.put(name, generateExampleValue(type));
            }
            exampleArray.add(exampleItem);
        }

        return Map.of(
                "type", "object",
                "properties", properties,
                "example", exampleArray.get(0),
                "examples", Map.of("exampleArray", exampleArray),
                "xml", Map.of("name", className.toLowerCase())
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

    private static String getTypeFormat(String type) {
        switch (type.toLowerCase()) {
            case "int":
            case "short":
                return "int32";
            case "long":
                return "int64";
            case "float":
                return "float";
            case "double":
                return "double";
            default:
                return null;
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

    private static Map<String, Object> createGetAllOperation(String className, List<String> attributes) {
        Map<String, Object> operation = new LinkedHashMap<>();
        operation.put("tags", List.of(className));
        operation.put("summary", "Get all instances of " + className);
        operation.put("description", "Fetches all instances of " + className);

        List<Map<String, Object>> examples = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            Map<String, Object> exampleItem = new LinkedHashMap<>();
            exampleItem.put("id", i);

            for (String attribute : attributes) {
                String[] parts = attribute.split(" ");
                String name = parts[0].substring(1);
                String type = parts[2];
                exampleItem.put(name, generateExampleValue(type));
            }
            examples.add(exampleItem);
        }

        operation.put("responses", Map.of(
                "200", Map.of(
                        "description", "Successful retrieval",
                        "content", Map.of(
                                "application/json", Map.of(
                                        "schema", Map.of(
                                                "type", "array",
                                                "items", Map.of("$ref", "#/components/schemas/" + className)
                                        ),
                                        "examples", Map.of("exampleArray", Map.of("value", examples))
                                ),
                                "application/xml", Map.of(
                                        "schema", Map.of(
                                                "type", "array",
                                                "items", Map.of("$ref", "#/components/schemas/" + className)
                                        ),
                                        "examples", Map.of("exampleArray", Map.of("value", examples))
                                )
                        )
                )
        ));
        return operation;
    }

    private static Map<String, Object> createOperation(String className, String method, Map<String, Object> classSchema, boolean isGetMethod) {
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
                            ),
                            "application/xml", Map.of(
                                    "schema", Map.of("$ref", "#/components/schemas/" + className)
                            ),
                            "application/x-www-form-urlencoded", Map.of(
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

        if (isGetMethod) {
            List<Map<String, Object>> examples = (List<Map<String, Object>>) ((Map<String, Object>) classSchema.get("examples")).get("exampleArray");
            Map<String, Object> exampleById = new LinkedHashMap<>();
            for (Map<String, Object> example : examples) {
                int id = (int) example.get("id");
                exampleById.put(String.valueOf(id), Map.of("value", example));
            }
            operation.put("responses", Map.of(
                    "200", Map.of(
                            "description", "Successful operation",
                            "content", Map.of(
                                    "application/json", Map.of(
                                            "schema", Map.of("$ref", "#/components/schemas/" + className),
                                            "examples", exampleById
                                    ),
                                    "application/xml", Map.of(
                                            "schema", Map.of("$ref", "#/components/schemas/" + className),
                                            "examples", exampleById
                                    )
                            )
                    )
            ));
        } else {
            operation.put("responses", Map.of(
                    "200", Map.of(
                            "description", "Successful operation",
                            "content", Map.of(
                                    "application/json", Map.of(
                                            "schema", Map.of("$ref", "#/components/schemas/" + className)
                                    ),
                                    "application/xml", Map.of(
                                            "schema", Map.of("$ref", "#/components/schemas/" + className)
                                    )
                            )
                    )
            ));
        }

        return operation;
    }
}
