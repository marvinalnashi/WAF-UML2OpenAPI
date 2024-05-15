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
                                      List<Map<String, Object>> mappings,
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

        mappings.forEach(mapping -> {
            String url = "/" + mapping.get("url");
            paths.put(url, createMappedPathItem(mapping));

            List<String> methodList = (List<String>) mapping.get("methods");
            if (methodList != null) {
                methodList.forEach(methodName ->
                        paths.put(url + "/" + methodName.toLowerCase(), createMethodPathItem(methodName, mapping))
                );
            }
            List<String> attributeList = (List<String>) mapping.get("attributes");
            if (attributeList != null) {
                attributeList.forEach(attributeName ->
                        paths.put(url + "/" + attributeName.toLowerCase(), createAttributePathItem(attributeName, mapping))
                );
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

    private static Map<String, Object> createMethodPathItem(String methodName, Map<String, Object> mapping) {
        Map<String, Object> methodDetails = new LinkedHashMap<>();
        methodDetails.put("summary", "Custom operation for " + methodName);
        methodDetails.put("description", "Performs " + methodName + " on " + mapping.get("className"));
        methodDetails.put("responses", Map.of(
                "200", Map.of("description", "Successful operation")
        ));
        return Map.of("get", methodDetails);
    }

    private static Map<String, Object> createAttributePathItem(String attributeName, Map<String, Object> mapping) {
        Map<String, Object> attributeDetails = new LinkedHashMap<>();
        attributeDetails.put("summary", "Custom operation for " + attributeName);
        attributeDetails.put("description", "Retrieves " + attributeName + " from " + mapping.get("className"));
        attributeDetails.put("responses", Map.of(
                "200", Map.of("description", "Successful retrieval")
        ));
        return Map.of("get", attributeDetails);
    }

    private static Map<String, Object> createMappedPathItem(Map<String, Object> mapping) {
        String method = (String) mapping.get("method");
        Map<String, Object> methodDetails = new LinkedHashMap<>();
        methodDetails.put("summary", "Custom operation for " + mapping.get("className"));
        methodDetails.put("description", "Performs " + method + " on " + mapping.get("className"));
        methodDetails.put("responses", Map.of(
                "200", Map.of("description", "Successful operation")
        ));

        return Map.of(method.toLowerCase(), methodDetails);
    }
}
