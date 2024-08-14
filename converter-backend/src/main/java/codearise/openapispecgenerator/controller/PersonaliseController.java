package codearise.openapispecgenerator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * REST controller for handling the functionalities related to the management of the example values that are generated for the attributes of the classes.
 */
@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class PersonaliseController {

    /**
     * The path of the directory in which the generated OpenAPI specification is saved.
     */
    private final String outputPath = "./data/export.yml";

    /**
     * The endpoint to fetch the generated OpenAPI specification before its example values are modified by the user.
     *
     * @return a map containing the OpenAPI specification data
     */
    @GetMapping("/personalise")
    public Map<String, Object> getPersonalisedData() {
        try {
            File file = new File(outputPath);
            if (!file.exists()) {
                throw new RuntimeException("File not found: " + outputPath);
            }
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            return mapper.readValue(file, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve OpenAPI specification: " + e.getMessage(), e);
        }
    }

    /**
     * Updates an example value for an attribute of a class in the generated OpenAPI specification.
     *
     * @param updateRequest a map containing the update details
     */
    @PostMapping("/updateExample")
    public void updateExample(@RequestBody Map<String, Object> updateRequest) {
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            File file = new File(outputPath);
            Map<String, Object> openApiSpec = mapper.readValue(file, Map.class);

            String className = (String) updateRequest.get("className");
            String attributeName = (String) updateRequest.get("attributeName");
            int index = (Integer) updateRequest.get("index");
            String newValue = (String) updateRequest.get("newValue");

            Map<String, Object> schemas = (Map<String, Object>) ((Map<String, Object>) openApiSpec.get("components")).get("schemas");
            Map<String, Object> classSchema = (Map<String, Object>) schemas.get(className);
            Map<String, Object> examples = (Map<String, Object>) classSchema.get("examples");
            ((Map<String, Object>) ((List<Object>) examples.get("exampleArray")).get(index)).put(attributeName, newValue);

            updatePaths(openApiSpec, className, attributeName, index, newValue);

            mapper.writeValue(file, openApiSpec);
        } catch (IOException e) {
            throw new RuntimeException("Failed to update OpenAPI specification: " + e.getMessage(), e);
        }
    }

    @PostMapping("/linkExamples")
    public void linkExamples(@RequestBody Map<String, Object> linkRequest) {
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            File file = new File(outputPath);
            Map<String, Object> openApiSpec = mapper.readValue(file, Map.class);

            String className = (String) linkRequest.get("className");
            List<Map<String, Object>> links = (List<Map<String, Object>>) linkRequest.get("links");

            addLinkedExamplesEndpoint(openApiSpec, links);

            mapper.writeValue(file, openApiSpec);
        } catch (IOException e) {
            throw new RuntimeException("Failed to link examples in OpenAPI specification: " + e.getMessage(), e);
        }
    }

    private void addLinkedExamplesEndpoint(Map<String, Object> openApiSpec, List<Map<String, Object>> links) {
        Map<String, Object> paths = (Map<String, Object>) openApiSpec.get("paths");

        String path = "/linked/examples";
        Map<String, Object> getEndpoint = Map.of(
                "get", Map.of(
                        "summary", "Get all linked examples",
                        "responses", Map.of(
                                "200", Map.of(
                                        "description", "A list of linked examples",
                                        "content", Map.of(
                                                "application/json", Map.of(
                                                        "example", links
                                                )
                                        )
                                )
                        )
                )
        );

        paths.put(path, getEndpoint);
    }

    /**
     * Updates the paths in the generated OpenAPI specification with the modified example value.
     *
     * @param openApiSpec The generated OpenAPI specification.
     * @param className The name of the class.
     * @param attributeName The name of the attribute.
     * @param index The index of the example value.
     * @param newValue The new example value for the specified attribute.
     */
    private void updatePaths(Map<String, Object> openApiSpec, String className, String attributeName, int index, String newValue) {
        // As this method is complex, and it contains a bug that's causing example values and their paths to only be updated for the GET /class endpoint but not also the GET /class/{id} endpoint, the code for this method has been broken down and comments have been added to various parts of the method for increased readability and debugging.

        // Retrieves the paths section of the OpenAPI specification.
        Map<String, Object> paths = (Map<String, Object>) openApiSpec.get("paths");

        // Iterates over each path in the paths section.
        for (Map.Entry<String, Object> pathEntry : paths.entrySet()) {
            // Retrieves the HTTP methods for the current path.
            Map<String, Object> methods = (Map<String, Object>) pathEntry.getValue();

            // Iterates over each HTTP method in the current path.
            for (Map.Entry<String, Object> methodEntry : methods.entrySet()) {
                // Retrieve the responses section for the current HTTP method
                Map<String, Object> responses = (Map<String, Object>) ((Map<String, Object>) methodEntry.getValue()).get("responses");

                // Checks if the responses section exists.
                if (responses != null) {
                    // Iterates over each response in the responses section.
                    for (Map.Entry<String, Object> responseEntry : responses.entrySet()) {
                        // Retrieves the content section for the current response.
                        Map<String, Object> content = (Map<String, Object>) ((Map<String, Object>) responseEntry.getValue()).get("content");

                        // Checks if the content section exists.
                        if (content != null) {
                            // Iterates over each content type in the content section.
                            for (Map.Entry<String, Object> contentEntry : content.entrySet()) {
                                // Retrieves the examples section for the current content type.
                                Map<String, Object> examples = (Map<String, Object>) ((Map<String, Object>) contentEntry.getValue()).get("examples");

                                // Checks if the examples section exists and contains exampleArray.
                                if (examples != null && examples.get("exampleArray") != null) {
                                    // Retrieves exampleArray from the examples section.
                                    List<Object> exampleArray = (List<Object>) ((Map<String, Object>) examples.get("exampleArray")).get("value");

                                    // Checks if exampleArray contains enough elements.
                                    if (exampleArray.size() > index) {
                                        // Retrieves the example object at the specified index.
                                        Map<String, Object> example = (Map<String, Object>) exampleArray.get(index);

                                        // Checks if the example object contains the specified attribute.
                                        if (example.containsKey(attributeName)) {
                                            // Updates the contents of the attribute with the newly set example value.
                                            example.put(attributeName, newValue);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}