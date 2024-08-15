package codearise.openapispecgenerator.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class MockServerController {

    @GetMapping("/test-openapi")
    public ResponseEntity<List<Map<String, Object>>> testOpenApiSpecification() {
        List<Map<String, Object>> tests = new ArrayList<>();
        List<String> paths = extractPathsFromOpenAPISpec("./data/export.yml");

        for (String path : paths) {
            addTestCases(tests, path);
        }

        return ResponseEntity.ok(tests);
    }

    private void addTestCases(List<Map<String, Object>> tests, String path) {
        tests.add(Map.of("name", "Test GET " + path, "method", "GET", "path", path));
        tests.add(Map.of("name", "Test POST " + path, "method", "POST", "path", path, "body", Map.of("example", "data")));
        tests.add(Map.of("name", "Test PUT " + path, "method", "PUT", "path", path, "body", Map.of("example", "updated data")));
        tests.add(Map.of("name", "Test DELETE " + path, "method", "DELETE", "path", path));
    }

    private List<String> extractPathsFromOpenAPISpec(String filePath) {
        List<String> paths = new ArrayList<>();
        try {
            ObjectMapper mapper = new ObjectMapper(new com.fasterxml.jackson.dataformat.yaml.YAMLFactory());
            JsonNode root = mapper.readTree(new File(filePath));
            JsonNode pathsNode = root.get("paths");
            if (pathsNode != null) {
                pathsNode.fieldNames().forEachRemaining(paths::add);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return paths;
    }
}
