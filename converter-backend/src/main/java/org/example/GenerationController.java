package org.example;

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

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class GenerationController {

    private final String outputPath = "/data/export.yml";
    private final OpenAPISpecGenerator openAPISpecGenerator;
    private List<Map<String, Object>> savedMappings = new ArrayList<>();

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

            return ResponseEntity.ok().body(elements);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error parsing diagram: " + e.getMessage()));
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