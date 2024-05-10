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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class GenerationController {

    private final String outputPath = "/data/export.yml";

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

            InputStream fileContentStream = new ByteArrayInputStream(file.getBytes());
            Map<String, List<String>> classes = parser.parse(fileContentStream);

            Map<String, Object> mappings = new HashMap<>();
            mappings.put("classes", classes);

            String openAPISpec = OpenAPISpecGenerator.generateSpecWithMappings(mappings, outputPath);
            return ResponseEntity.ok(openAPISpec);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during generation: " + e.getMessage());
        }
    }

    @PostMapping("/apply-mappings")
    public ResponseEntity<String> applyMappings(@RequestBody MappingDetails mappingDetails) {
        if (mappingDetails == null || mappingDetails.getMappings() == null) {
            System.out.println("No mappings received or mappings are null");
            return ResponseEntity.badRequest().body("No mappings data received");
        }

        System.out.println("Received mappings: " + mappingDetails.getMappings());
        try {
            Map<String, Object> mappings = mappingDetails.getMappings();
            String openAPISpec = OpenAPISpecGenerator.generateSpecWithMappings(mappings, outputPath);
            return ResponseEntity.ok(openAPISpec);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to apply mappings: " + e.getMessage());
        }
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