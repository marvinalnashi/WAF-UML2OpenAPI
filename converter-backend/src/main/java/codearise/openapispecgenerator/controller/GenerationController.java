package codearise.openapispecgenerator.controller;

import codearise.openapispecgenerator.entity.Relationship;
import codearise.openapispecgenerator.util.OpenAPISpecGenerator;
import codearise.openapispecgenerator.parser.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The REST controller that contains the stepper functionalities and the corresponding endpoints.
 */
@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class GenerationController {
    /**
     * The path where the generated OpenAPI specification will be saved.
     */
    private final String outputPath = "./data/export.yml";

    /**
     * The reference variable for the class that is used to generate an OpenAPI specification and fill it with content that is based on the uploaded UML diagram and the modifications done by the user in the stepper.
     */
    private final OpenAPISpecGenerator openAPISpecGenerator;

    /**
     * A list to store the mappings and modifications done and applied by the user in the Mapping step of the stepper.
     */
    private List<Map<String, Object>> savedMappings = new ArrayList<>();

    private Map<String, List<Map<String, String>>> savedRelationships = new HashMap<>();

    /**
     * Map in which all the data related to the uploaded UML diagram and the modifications done by the user is stored.
     */
    public Map<String, Object> umlDataStore = new HashMap<>();

    /**
     * The constructor of GenerationController.
     *
     * @param openAPISpecGenerator The class instance for generating an OpenAPI specification.
     */
    public GenerationController(OpenAPISpecGenerator openAPISpecGenerator) {
        this.openAPISpecGenerator = openAPISpecGenerator;
    }

    /**
     * The endpoint to fetch the generated OpenAPI specification.
     *
     * @return The HTTP response containing the OpenAPI specification file as body.
     */
    @GetMapping("/export.yml")
    public ResponseEntity<Resource> getOpenAPISpec() {
        Resource fileResource = new FileSystemResource(outputPath);
        if (!fileResource.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"export.yml\"")
                .contentType(MediaType.parseMediaType("application/x-yaml"))
                .body(fileResource);
    }

    /**
     * The endpoint to parse individual UML elements from an uploaded UML diagram file.
     *
     * @param file The uploaded UML diagram file.
     * @return The HTTP response containing the parsed individual UML elements as body.
     */
    @PostMapping("/parse-elements")
    public ResponseEntity<Map<String, Object>> parseDiagramElements(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "File is empty"));
        }
        try {
            String fileName = file.getOriginalFilename();
            DiagramParser parser = getParserForFileType(fileName);
            if (parser == null) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(Map.of("error", "Unsupported file type"));
            }

            byte[] fileContent = file.getBytes();
            Map<String, Object> elements = parseUmlData(fileContent, parser);
            umlDataStore = elements;
            return ResponseEntity.ok().body(elements);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error parsing diagram: " + e.getMessage()));
        }
    }

    /**
     * Uses a suitable parser class to parse the data of the uploaded UML diagram.
     *
     * @param fileContent The contents of the uploaded UML diagram file.
     * @param parser The parser class that is used for extracting elements from the uploaded UML diagram.
     * @return Map containing the parsed individual elements of the uploaded UML diagram.
     * @throws Exception Is returned if an error occurs during the parsing process.
     */
    private Map<String, Object> parseUmlData(byte[] fileContent, DiagramParser parser) throws Exception {
        InputStream inputStream = new ByteArrayInputStream(fileContent);

        Map<String, List<String>> classes = parser.parse(inputStream);
        inputStream.reset();

        Map<String, List<String>> attributes = parser.parseAttributes(inputStream);
        inputStream.reset();

        Map<String, List<String>> methods = parser.parseMethods(inputStream);
        inputStream.reset();

        List<Relationship> relationships = parser.parseRelationships(inputStream);

        Map<String, Object> elements = new HashMap<>();
        elements.put("classes", new ArrayList<>(classes.keySet()));
        elements.put("attributes", attributes);
        elements.put("methods", methods);
        elements.put("relationships", relationships);

        return elements;
    }

    /**
     * The endpoint to rename an element of an uploaded UML diagram.
     *
     * @param renameInfo Map containing the type, old name, and new name of the element to rename.
     * @return The HTTP response containing the result of the rename operation as body, which consists of the old and new element values.
     */
    @PostMapping("/rename-element")
    public ResponseEntity<?> renameElement(@RequestBody Map<String, String> renameInfo) {
        String type = renameInfo.get("type");
        String oldName = renameInfo.get("oldName");
        String newName = renameInfo.get("newName");

        if (oldName == null || newName == null || type == null) {
            return ResponseEntity.badRequest().body("Missing parameters for renaming.");
        }

        try {
            switch (type) {
                case "class":
                    return renameClass(oldName, newName);
                case "attribute":
                    return renameAttribute(oldName, newName);
                case "method":
                    return renameMethod(oldName, newName);
                default:
                    return ResponseEntity.badRequest().body("Invalid type for renaming.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to rename element: " + e.getMessage());
        }
    }

    /**
     * Renames an element of an uploaded UML diagram.
     *
     * @param oldName The current name of the class.
     * @param newName The new name of the class.
     * @return The HTTP response containing the result of the rename operation as body, which consists of the old and new classnames.
     */
    private ResponseEntity<?> renameClass(String oldName, String newName) {
        Object classesObject = umlDataStore.get("classes");
        if (classesObject instanceof List) {
            List<String> classes = (List<String>) classesObject;
            int index = classes.indexOf(oldName);
            if (index != -1) {
                classes.set(index, newName);
                Map<String, List<String>> attributes = (Map<String, List<String>>) umlDataStore.get("attributes");
                if (attributes.containsKey(oldName)) {
                    List<String> attributeList = attributes.remove(oldName);
                    attributes.put(newName, attributeList);
                }
                Map<String, List<String>> methods = (Map<String, List<String>>) umlDataStore.get("methods");
                if (methods.containsKey(oldName)) {
                    List<String> methodList = methods.remove(oldName);
                    methods.put(newName, methodList);
                }
                umlDataStore.put("classes", classes);
                umlDataStore.put("attributes", attributes);
                umlDataStore.put("methods", methods);
                return ResponseEntity.ok("Class renamed successfully from " + oldName + " to " + newName);
            }
            return ResponseEntity.badRequest().body("Class not found: " + oldName);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected data type for classes.");
    }

    /**
     * Renames an attribute of the uploaded UML diagram.
     *
     * @param oldName The current name of the attribute.
     * @param newName The new name of the attribute.
     * @return The HTTP response containing the result of the rename operation as body, which consists of the old and new attribute values.
     */
    private ResponseEntity<?> renameAttribute(String oldName, String newName) {
        Map<String, List<String>> attributes = (Map<String, List<String>>) umlDataStore.get("attributes");
        if (attributes != null) {
            attributes.forEach((key, value) -> {
                List<String> updatedAttributes = value.stream()
                        .map(attribute -> attribute.equals(oldName) ? newName : attribute)
                        .collect(Collectors.toList());
                attributes.put(key, updatedAttributes);
            });
            umlDataStore.put("attributes", attributes);
            return ResponseEntity.ok("Attribute renamed successfully from " + oldName + " to " + newName);
        }
        return ResponseEntity.badRequest().body("Attributes not found for: " + oldName);
    }

    /**
     * Renames a method of the uploaded UML diagram.
     *
     * @param oldName The current name of the method.
     * @param newName The new name of the method.
     * @return The HTTP response containing the result of the rename operation as body, which consists of the old and new method values.
     */
    private ResponseEntity<?> renameMethod(String oldName, String newName) {
        Map<String, List<String>> methods = (Map<String, List<String>>) umlDataStore.get("methods");
        if (methods != null) {
            methods.forEach((key, value) -> {
                List<String> updatedMethods = value.stream()
                        .map(method -> method.equals(oldName) ? newName : method)
                        .collect(Collectors.toList());
                methods.put(key, updatedMethods);
            });
            umlDataStore.put("methods", methods);
            return ResponseEntity.ok("Method renamed successfully from " + oldName + " to " + newName);
        }
        return ResponseEntity.badRequest().body("Methods not found for: " + oldName);
    }

    /**
     * The endpoint to delete an element from the uploaded UML diagram or from the elements added by the user.
     *
     * @param deleteInfo Map for the elemented that is removed, in which the key is the name of the element and the value is the type of the element.
     * @return The HTTP response containing the result of the delete operation as body, which consists of the old and new element values.
     */
    @PostMapping("/delete-element")
    public ResponseEntity<?> deleteElement(@RequestBody Map<String, String> deleteInfo) {
        String type = deleteInfo.get("type");
        String name = deleteInfo.get("name");

        try {
            switch (type) {
                case "class":
                    List<String> classes = (List<String>) umlDataStore.get("classes");
                    classes.remove(name);
                    break;
                case "attribute":
                    Map<String, List<String>> attributes = (Map<String, List<String>>) umlDataStore.get("attributes");
                    attributes.values().forEach(attributesList -> attributesList.removeIf(attribute -> attribute.equals(name)));
                    break;
                case "method":
                    Map<String, List<String>> methods = (Map<String, List<String>>) umlDataStore.get("methods");
                    methods.values().forEach(methodsList -> methodsList.removeIf(method -> method.equals(name)));
                    break;
            }
            return ResponseEntity.ok("Element deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete element: " + e.getMessage());
        }
    }

    /**
     * The endpoint to generate an OpenAPI specification based on the uploaded UML diagram file and the modifications done by the user.
     *
     * @param file The uploaded UML diagram file.
     * @param selectedHttpMethodsJson a JSON string containing the HTTP methods the user selected in the table of the Manage Elements step of the stepper.
     * @return The HTTP response containing the generated OpenAPI specification as body.
     */
    @PostMapping(value = "/generate", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Map<String, String>> generateOpenAPISpec(@RequestParam("file") MultipartFile file,
                                                                   @RequestParam("selectedHttpMethods") String selectedHttpMethodsJson,
                                                                   @RequestParam(value = "relationships", required = false) String relationshipsJson) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "File is empty"));
        }

        try {
            System.out.println("Selected HTTP Methods in JSON format: " + selectedHttpMethodsJson);
            Map<String, List<String>> selectedHttpMethods = new ObjectMapper().readValue(selectedHttpMethodsJson, HashMap.class);
            System.out.println("Selected HTTP Methods: " + selectedHttpMethods);

            Map<String, List<Map<String, String>>> savedRelationships = new HashMap<>();
            if (relationshipsJson != null && !relationshipsJson.isEmpty()) {
                List<Map<String, String>> relationshipsList = new ObjectMapper().readValue(relationshipsJson, List.class);

                for (Map<String, String> relationship : relationshipsList) {
                    String classFrom = relationship.get("classFrom");
                    List<Map<String, String>> classRelationships = savedRelationships.getOrDefault(classFrom, new ArrayList<>());
                    classRelationships.add(relationship);
                    savedRelationships.put(classFrom, classRelationships);
                }
            }

            Map<String, List<String>> classes = safelyCastToMap(umlDataStore.get("classes"));
            Map<String, List<String>> attributes = safelyCastToMap(umlDataStore.get("attributes"));
            Map<String, List<String>> methods = safelyCastToMap(umlDataStore.get("methods"));

            System.out.println("Classes: " + classes);
            System.out.println("Attributes: " + attributes);
            System.out.println("Methods: " + methods);

            Map<String, Map<String, Boolean>> convertedHttpMethods = new HashMap<>();
            selectedHttpMethods.forEach((className, methodsList) -> {
                Map<String, Boolean> methodsMap = new HashMap<>();
                for (String method : methodsList) {
                    methodsMap.put(method, true);
                }
                convertedHttpMethods.put(className, methodsMap);
            });

            String openAPISpec = openAPISpecGenerator.generateSpec(classes, attributes, methods, savedMappings, savedRelationships, outputPath, convertedHttpMethods);
            return ResponseEntity.ok(Map.of("message", openAPISpec));
        } catch (ClassCastException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Data type casting error: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error during generation: " + e.getMessage()));
        }
    }


    /**
     * The endpoint to apply the elements the user has added and/or modified in the Mapping step of the stepper.
     *
     * @param mappings A list that contains all the elements the user has added and all the existing elements of the uploaded UML diagram.
     * @return The HTTP response containing the result of the operation in which the mappings and modifications were applied as body.
     */
    @PostMapping("/apply-mappings")
    public ResponseEntity<?> applyMappings(@RequestBody List<Map<String, Object>> mappings) {
        if (mappings == null || mappings.isEmpty()) {
            return ResponseEntity.ok().body("No elements remain in the buffer that need to be applied. Proceeding.");
        }
        savedMappings = mappings;
        return ResponseEntity.ok().body("Mappings saved successfully");
    }

    @PostMapping("/apply-relationships")
    public ResponseEntity<?> applyRelationships(@RequestBody Map<String, Object> relationshipsData) {
        try {
            List<Map<String, String>> relationships = (List<Map<String, String>>) relationshipsData.get("relationships");
            if (relationships == null || relationships.isEmpty()) {
                return ResponseEntity.ok(Map.of("message", "No relationships provided."));
            }

            savedRelationships.clear();

            for (Map<String, String> relationship : relationships) {
                String classFrom = relationship.get("classFrom");
                savedRelationships
                        .computeIfAbsent(classFrom, k -> new ArrayList<>())
                        .add(relationship);
            }

            umlDataStore.put("relationships", savedRelationships);

            return ResponseEntity.ok(Map.of("message", "Relationships saved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/class-names")
    public ResponseEntity<List<String>> getClassNames() {
        List<String> classes = (List<String>) umlDataStore.get("classes");
        return ResponseEntity.ok(classes);
    }

    /**
     * Fetches the appropriate parser class for the specified file type.
     *
     * @param fileName The name of the uploaded UML diagram file.
     * @return The parser class that can handle the specified UML diagram file or null if the uploaded UML diagram has an unsupported file format.
     */
    private DiagramParser getParserForFileType(String fileName) {
        if (fileName == null) {
            return null;
        }
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        switch (extension) {
            case "xml":
                return new XMLParser();
            case "uxf":
                return new UXFParser();
            case "mdj":
                return new MDJParser();
            case "puml":
                return new PUMLParser();
            default:
                return null;
        }
    }

    /**
     * The endpoint to add a new element to the collection of existing elements of the uploaded UML diagram and the elements added by the user.
     *
     * @param newElement Map that contains the values of the newly added element.
     * @return The HTTP response containing the result of the addition operation as body.
     */
    @PostMapping("/add-new-element")
    public ResponseEntity<?> addNewElement(@RequestBody Map<String, Object> newElement) {
        String className = (String) newElement.get("className");
        List<String> attributes = (List<String>) newElement.get("attributes");
        List<String> methods = (List<String>) newElement.get("methods");

        if (!umlDataStore.containsKey("classes")) {
            umlDataStore.put("classes", new ArrayList<>());
        }
        List<String> classes = (List<String>) umlDataStore.get("classes");
        classes.add(className);

        if (!umlDataStore.containsKey("attributes")) {
            umlDataStore.put("attributes", new HashMap<>());
        }
        Map<String, List<String>> attributesMap = (Map<String, List<String>>) umlDataStore.get("attributes");
        attributesMap.put(className, attributes);

        if (!umlDataStore.containsKey("methods")) {
            umlDataStore.put("methods", new HashMap<>());
        }
        Map<String, List<String>> methodsMap = (Map<String, List<String>>) umlDataStore.get("methods");
        methodsMap.put(className, methods);

        return ResponseEntity.ok(Map.of("message", "New element added successfully"));
    }

    /**
     * Safely casts an object to a map that contains the values of a type of individual UML elements.
     *
     * @param data The object that is cast to a map.
     * @return The map that is the result of the cast operation.
     * @throws ClassCastException Is returned if an error occurs during the casting process.
     */
    private Map<String, List<String>> safelyCastToMap(Object data) {
        if (data instanceof Map) {
            return (Map<String, List<String>>) data;
        } else if (data instanceof List) {
            Map<String, List<String>> map = new HashMap<>();
            for (Object item : (List) data) {
                if (item instanceof String) {
                    map.put((String) item, new ArrayList<>());
                }
            }
            return map;
        } else {
            throw new ClassCastException("Expected Map but found " + (data == null ? "null" : data.getClass().getSimpleName()));
        }
    }
}

