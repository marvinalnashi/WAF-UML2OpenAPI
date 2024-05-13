package org.example;

import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        elements.put("classes", classes.keySet());
        elements.put("attributes", attributes);
        elements.put("methods", methods);
        return elements;
    }

    @PostMapping("/rename-element")
    public ResponseEntity<?> renameElement(@RequestBody Map<String, String> renameInfo) {
        String type = renameInfo.get("type");
        String oldName = renameInfo.get("oldName");
        String newName = renameInfo.get("newName");

        try {
            switch (type) {
                case "class":
                    Map<String, List<String>> classes = (Map<String, List<String>>) umlDataStore.get("classes");
                    if (classes.containsKey(oldName)) {
                        classes.put(newName, classes.remove(oldName));
                        Map<String, List<String>> attributes = (Map<String, List<String>>) umlDataStore.get("attributes");
                        attributes.put(newName, attributes.remove(oldName));
                        Map<String, List<String>> methods = (Map<String, List<String>>) umlDataStore.get("methods");
                        methods.put(newName, methods.remove(oldName));
                    }
                    break;
                case "attribute":
                    Map<String, List<String>> attributes = (Map<String, List<String>>) umlDataStore.get("attributes");
                    attributes.entrySet().forEach(entry -> {
                        entry.setValue(entry.getValue().stream().map(attr -> attr.equals(oldName) ? newName : attr).collect(Collectors.toList()));
                    });
                    break;
                case "method":
                    Map<String, List<String>> methods = (Map<String, List<String>>) umlDataStore.get("methods");
                    methods.entrySet().forEach(entry -> {
                        entry.setValue(entry.getValue().stream().map(meth -> meth.equals(oldName) ? newName : meth).collect(Collectors.toList()));
                    });
                    break;
            }
            return ResponseEntity.ok("Element renamed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to rename element: " + e.getMessage());
        }
    }

    @PostMapping("/delete-element")
    public ResponseEntity<?> deleteElement(@RequestBody Map<String, String> deleteInfo) {
        String type = deleteInfo.get("type");
        String name = deleteInfo.get("name");

        try {
            switch (type) {
                case "class":
                    ((Map<String, List<String>>) umlDataStore.get("classes")).remove(name);
                    ((Map<String, List<String>>) umlDataStore.get("attributes")).remove(name);
                    ((Map<String, List<String>>) umlDataStore.get("methods")).remove(name);
                    break;
                case "attribute":
                    ((Map<String, List<String>>) umlDataStore.get("attributes")).values().forEach(attrs -> attrs.removeIf(attr -> attr.equals(name)));
                    break;
                case "method":
                    ((Map<String, List<String>>) umlDataStore.get("methods")).values().forEach(methods -> methods.removeIf(method -> method.equals(name)));
                    break;
            }
            return ResponseEntity.ok("Element deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete element: " + e.getMessage());
        }
    }

    @PostMapping("/generate")
    public ResponseEntity<String> generateOpenAPISpec(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File is empty");
        }

        try {
            String fileName = file.getOriginalFilename();
            DiagramParser parser = getParserForFileType(fileName);
            if (parser == null) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body("Unsupported file type");
            }

            Map<String, List<String>> classes = parseStream(parser, file.getBytes(), "classes");
            Map<String, List<String>> attributes = parseStream(parser, file.getBytes(), "attributes");
            Map<String, List<String>> methods = parseStream(parser, file.getBytes(), "methods");

            String openAPISpec = OpenAPISpecGenerator.generateSpec(classes, attributes, methods, savedMappings, outputPath);
            return ResponseEntity.ok(openAPISpec);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during generation: " + e.getMessage());
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
}