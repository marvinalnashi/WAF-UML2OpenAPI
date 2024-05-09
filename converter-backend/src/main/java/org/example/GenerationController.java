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
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@RestController
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

    @PostMapping("/generate")
    public ResponseEntity<String> generateOpenAPISpec(MultipartHttpServletRequest request) {
        Iterator<String> itr = request.getFileNames();
        MultipartFile file = request.getFile(itr.next());
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        String mappingsJson = request.getParameter("mappings");
        Map<String, Object> mappings = new HashMap<>();
        if (mappingsJson != null && !mappingsJson.isEmpty()) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                mappings = objectMapper.readValue(mappingsJson, Map.class);
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.badRequest().body("Error parsing mappings");
            }
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

            String openAPISpec = OpenAPISpecGenerator.generateSpec(classes, attributes, methods, mappings, outputPath);
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
