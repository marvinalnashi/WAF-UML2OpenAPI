package org.example;

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

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class GenerationController {
    private final String outputPath = "/data/export.yml";
    private final OpenAPISpecGenerator openAPISpecGenerator;
    private List<Map<String, Object>> savedMappings = new ArrayList<>();
    private Map<String, Object> umlDataStore = new HashMap<>();

    public GenerationController(OpenAPISpecGenerator openAPISpecGenerator) {
        this.openAPISpecGenerator = openAPISpecGenerator;
    }

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

    private Map<String, Object> parseUmlData(byte[] fileContent, DiagramParser parser) throws Exception {
        InputStream classStream = new ByteArrayInputStream(fileContent);
        Map<String, List<String>> classes = parser.parse(classStream);

        InputStream attrStream = new ByteArrayInputStream(fileContent);
        Map<String, List<String>> attributes = parser.parseAttributes(attrStream);

        InputStream methodStream = new ByteArrayInputStream(fileContent);
        Map<String, List<String>> methods = parser.parseMethods(methodStream);

        Map<String, Object> elements = new HashMap<>();
        elements.put("classes", new ArrayList<>(classes.keySet()));
        elements.put("attributes", attributes);
        elements.put("methods", methods);
        return elements;
    }

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

    private ResponseEntity<?> renameClass(String oldName, String newName) {
        Object classesObject = umlDataStore.get("classes");
        if (classesObject instanceof List) {
            List<String> classes = (List<String>) classesObject;
            int index = classes.indexOf(oldName);
            if (index != -1) {
                classes.set(index, newName);
                Map<String, List<String>> attributes = (Map<String, List<String>>) umlDataStore.get("attributes");
                if (attributes.containsKey(oldName)) {
                    List<String> attrList = attributes.remove(oldName);
                    attributes.put(newName, attrList);
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

    private ResponseEntity<?> renameAttribute(String oldName, String newName) {
        Map<String, List<String>> attributes = (Map<String, List<String>>) umlDataStore.get("attributes");
        if (attributes != null) {
            attributes.forEach((key, value) -> {
                List<String> updatedAttributes = value.stream()
                        .map(attr -> attr.equals(oldName) ? newName : attr)
                        .collect(Collectors.toList());
                attributes.put(key, updatedAttributes);
            });
            umlDataStore.put("attributes", attributes);
            return ResponseEntity.ok("Attribute renamed successfully from " + oldName + " to " + newName);
        }
        return ResponseEntity.badRequest().body("Attributes not found for: " + oldName);
    }

    private ResponseEntity<?> renameMethod(String oldName, String newName) {
        Map<String, List<String>> methods = (Map<String, List<String>>) umlDataStore.get("methods");
        if (methods != null) {
            methods.forEach((key, value) -> {
                List<String> updatedMethods = value.stream()
                        .map(meth -> meth.equals(oldName) ? newName : meth)
                        .collect(Collectors.toList());
                methods.put(key, updatedMethods);
            });
            umlDataStore.put("methods", methods);
            return ResponseEntity.ok("Method renamed successfully from " + oldName + " to " + newName);
        }
        return ResponseEntity.badRequest().body("Methods not found for: " + oldName);
    }

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
                    attributes.values().forEach(attrs -> attrs.removeIf(attr -> attr.equals(name)));
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

    @PostMapping(value = "/generate", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Map<String, String>> generateOpenAPISpec(@RequestParam("file") MultipartFile file,
                                                                   @RequestParam("selectedHttpMethods") String selectedHttpMethodsJson) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "File is empty"));
        }

        try {
            System.out.println("Selected HTTP Methods in JSON format: " + selectedHttpMethodsJson);
            Map<String, List<String>> selectedHttpMethods = new ObjectMapper().readValue(selectedHttpMethodsJson, HashMap.class);
            System.out.println("Selected HTTP Methods: " + selectedHttpMethods);

            Map<String, List<String>> classes = safelyCastToMap(umlDataStore.get("classes"));
            Map<String, List<String>> attributes = safelyCastToMap(umlDataStore.get("attributes"));
            Map<String, List<String>> methods = safelyCastToMap(umlDataStore.get("methods"));

            System.out.println("Classes: " + classes);
            System.out.println("Attributes: " + attributes);
            System.out.println("Methods: " + methods);

            String openAPISpec = openAPISpecGenerator.generateSpec(classes, attributes, methods, savedMappings, outputPath, selectedHttpMethods);
            return ResponseEntity.ok(Map.of("message", openAPISpec));
        } catch (ClassCastException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Data type casting error: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error during generation: " + e.getMessage()));
        }
    }

    private Map<String, List<String>> parseStream(DiagramParser parser, byte[] fileContent, String type) throws Exception {
        try (InputStream inputStream = new ByteArrayInputStream(fileContent)) {
            switch (type) {
                case "classes":
                    return parser.parse(inputStream);
                case "attributes":
                    return parser.parseAttributes(inputStream);
                case "methods":
                    return parser.parseMethods(inputStream);
                default:
                    return new HashMap<>();
            }
        }
    }

    @PostMapping("/apply-mappings")
    public ResponseEntity<?> applyMappings(@RequestBody List<Map<String, Object>> mappings) {
        if (mappings == null || mappings.isEmpty()) {
            return ResponseEntity.badRequest().body("No mappings data received or mappings are empty");
        }
        savedMappings = mappings;
        return ResponseEntity.ok().body("Mappings saved successfully");
    }

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
                return new PlantUMLParser();
            default:
                return null;
        }
    }

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