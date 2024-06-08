package codearise.openapispecgenerator.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Class that generates an OpenAPI specification and fills it with content based on the configuration that the user has provided in the stepper.
 */
public class OpenAPISpecGenerator {
    /**
     * The API key that is used to access the OpenAI model that is required for generating example values.
     */
    private final String apiKey;

    /**
     * The HTTP client that is used for initiating API requests.
     */
    private static final OkHttpClient httpClient = new OkHttpClient();

    /**
     * The maximum amount of retries that is tolerated for sending API requests to the OpenAI model.
     */
    private static final int MAX_RETRIES = 5;

    /**
     * The initial backoff time for retrying sending API requests to the OpenAI model.
     */
    private static final int INITIAL_BACKOFF = 1000;

    /**
     * The OpenAI model that is used for generating example values.
     */
    private static final String OPENAI_ENGINE = "gpt-3.5-turbo";

    /**
     * Cache for temporarily storing generated example values. This makes sure that the same generated example values are used in both the generateClassSchema method and the createGetAllOperation method, so that both the GET endpoints for a class contain the same example values. (This also fixes the bug with example value modifications and updates).
     */
    private static final Map<String, Object> exampleCache = new HashMap<>();

    /**
     * The constructor of OpenAPISpecGenerator.
     *
     * @param apiKey The API key that is used to access the OpenAI model that is required for generating example values.
     */
    public OpenAPISpecGenerator(@Value("${openai.api.key}") String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Generates the OpenAPI specification based on the configuration that the user has provided in the stepper.
     *
     * @param classes Map in which the key is the classname and the value is its corresponding path.
     * @param attributes Map in which the key is the classname and the value is the corresponding attribute.
     * @param methods Map in which the key is the classname and the value is the corresponding method.
     * @param mappings The list that contains the mappings and modifications done by the user in the Mapping step of the stepper.
     * @param outputPath The path of the directory in which the generated OpenAPI specification is saved.
     * @param selectedHttpMethods Map in which the key is the classname and the value contains the corresponding HTTP methods the user has selected in the Manage Elements tab of the Mapping step of the stepper.
     * @return Message that indicates whether the generation process has completed successfully.
     * @throws Exception Is returned if an error occurs during the generation process.
     */
    public String generateSpec(Map<String, List<String>> classes,
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

                // Methods aren't being moved over to the OpenAPI specification but this line can be used to do so in the future if needed.
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

    /**
     * Generates the class schema that contains classnames, the attributes, and an array that consists of their corresponding example values.
     *
     * @param className The name of the class.
     * @param attributes The list of attributes in a class.
     * @return Map that contains the generated class schema.
     * @throws Exception Is returned if an error occurs during the generation process.
     */
    private Map<String, Object> generateClassSchema(String className, List<String> attributes) throws Exception {
        Map<String, Object> properties = new LinkedHashMap<>();
        List<Map<String, Object>> exampleArray = new ArrayList<>();

        for (int i = 1; i <= 7; i++) {
            Map<String, Object> exampleItem = new LinkedHashMap<>();
            exampleItem.put("id", i);

            List<String> prompts = new ArrayList<>();
            for (String attribute : attributes) {
                String[] parts = attribute.split(" ");
                String name = parts[0].substring(1);
                String type = parts[2];
                prompts.add("Generate a unique example value for a " + type + " attribute named " + name + " for a class " + className + " with id " + i + ". Ensure this value is unique compared to other ids.");
            }

            List<Object> generatedValues = generateExampleValues(prompts, apiKey);

            for (int j = 0; j < attributes.size(); j++) {
                String[] parts = attributes.get(j).split(" ");
                String name = parts[0].substring(1);
                String type = parts[2];
                String format = getTypeFormat(type);

                Map<String, Object> attributeSchema = new LinkedHashMap<>();
                attributeSchema.put("type", mapType(type));
                if (format != null) {
                    attributeSchema.put("format", format);
                }
                properties.put(name, attributeSchema);
                exampleItem.put(name, generatedValues.get(j));
            }
            exampleArray.add(exampleItem);
        }

        return Map.of(
                "type", "object",
                "properties", properties,
                "example", exampleArray.get(0),
                "examples", Map.of("exampleArray", exampleArray),
                "xml", Map.of(
                        "name", className.toLowerCase(),
                        "wrapped", true,
                        "namespace", "http://example.com/schema"
                )
        );
    }

    /**
     * Generates example values for the attributes of the classes in a class schema based on the specified prompt.
     *
     * @param prompts List that contains the prompts that are used to generate example values for the attributes of the classes.
     * @param apiKey The API key that is used to access the OpenAI model that is required for generating example values.
     * @return List of generated example values for the attributes of the classes.
     * @throws IOException Is returned if an error occurs during the generation process.
     */
    private List<Object> generateExampleValues(List<String> prompts, String apiKey) throws IOException {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");

        for (String prompt : prompts) {
            if (exampleCache.containsKey(prompt)) {
                continue;
            }

            String json = "{ \"model\": \"" + OPENAI_ENGINE + "\", \"messages\": [{\"role\": \"system\", \"content\": \"" + prompt + "\"}], \"max_tokens\": 30, \"temperature\": 0.9 }";
            RequestBody body = RequestBody.create(json, JSON);

            int retryCount = 0;
            int backoffTime = INITIAL_BACKOFF;

            while (retryCount < MAX_RETRIES) {
                Request request = new Request.Builder()
                        .url("https://api.openai.com/v1/chat/completions")
                        .post(body)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Authorization", "Bearer " + apiKey)
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        ObjectMapper objectMapper = new ObjectMapper();
                        Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);

                        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
                        if (choices != null && !choices.isEmpty()) {
                            Map<String, Object> messageContent = (Map<String, Object>) choices.get(0).get("message");
                            String content = messageContent.get("content").toString().trim();
                            exampleCache.put(prompt, content);
                            break;
                        }
                    } else if (response.code() == 429) {
                        retryCount++;
                        System.err.println("Rate limited. Retrying in " + backoffTime + "ms");
                        Thread.sleep(backoffTime);
                        backoffTime *= 2;
                    } else if (response.code() == 401) {
                        throw new IOException("Unauthorized: Invalid API key.");
                    } else if (response.code() == 404) {
                        throw new IOException("Invalid endpoint. Please check the URL and endpoint.");
                    } else if (response.code() == 400) {
                        String responseBody = response.body().string();
                        throw new IOException("Bad request: " + responseBody);
                    } else {
                        throw new IOException("Unexpected code " + response);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Thread interrupted during backoff", e);
                }
            }

            if (retryCount == MAX_RETRIES) {
                throw new IOException("Max retries reached. Could not get a response from OpenAI API.");
            }
        }

        List<Object> results = new ArrayList<>();
        for (String prompt : prompts) {
            results.add(exampleCache.get(prompt));
        }

        return results;
    }

    /**
     * Maps a UML data type to a data type that can be used in the generated OpenAPI specification.
     *
     * @param type The UML data type that is used as input.
     * @return The corresponding data type that can be used in the generated OpenAPI specification.
     */
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

    /**
     * Fetches the format of the specified numeric data type.
     *
     * @param type The UML data type that is used as input.
     * @return The corresponding format for the specified UML data type or null if the specified data type is not in the list of data types that need formatting.
     */
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

    /**
     * Creates a GET HTTP method for fetching all instances of a class.
     *
     * @param className The name of the class.
     * @param attributes List of attributes for the specified class.
     * @return Map that contains all the data that is used for creating the GET HTTP method.
     * @throws Exception Is returned if an error occurs during the generation process.
     */
    private Map<String, Object> createGetAllOperation(String className, List<String> attributes) throws Exception {
        Map<String, Object> operation = new LinkedHashMap<>();
        operation.put("tags", List.of(className));
        operation.put("summary", "Get all instances of " + className);
        operation.put("description", "Fetches all instances of " + className);

        List<Map<String, Object>> examples = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            Map<String, Object> exampleItem = new LinkedHashMap<>();
            exampleItem.put("id", i);

            List<String> prompts = new ArrayList<>();
            for (String attribute : attributes) {
                String[] parts = attribute.split(" ");
                String name = parts[0].substring(1);
                String type = parts[2];
                prompts.add("Generate a unique example value for a " + type + " attribute named " + name + " for a class " + className + " with id " + i + ". Ensure this value is unique compared to other ids.");
            }

            List<Object> generatedValues = generateExampleValues(prompts, apiKey);
            for (int j = 0; j < attributes.size(); j++) {
                String[] parts = attributes.get(j).split(" ");
                String name = parts[0].substring(1);
                exampleItem.put(name, generatedValues.get(j));
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
                                                "items", Map.of("$ref", "#/components/schemas/" + className),
                                                "xml", Map.of(
                                                        "name", className.toLowerCase(),
                                                        "wrapped", true,
                                                        "namespace", "http://example.com/schema"
                                                )
                                        ),
                                        "examples", Map.of("exampleArray", Map.of("value", examples))
                                )
                        )
                )
        ));
        return operation;
    }

    /**
     * Generates endpoints for the HTTP methods the user has selected in the Manage Elements tab of the Manage step of the stepper.
     *
     * @param className The name of the class.
     * @param method The selected HTTP method.
     * @param classSchema The schema of the class that defines its structure.
     * @param isGetMethod Boolean that indicates whether the endpoint is a GET HTTP method.
     * @return Map that contains all the data that is used for creating the HTTP method.
     * @throws Exception Is returned if an error occurs during the generation process.
     */
    private Map<String, Object> createOperation(String className, String method, Map<String, Object> classSchema, boolean isGetMethod) throws Exception {
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
