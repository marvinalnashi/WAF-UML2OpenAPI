package codearise.openapispecgenerator.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Controller for managing the state of the Prism Mock Server.
 */
@RestController
public class MockServerController {
    /**
     * The process running the Prism Mock Server.
     */
    private Process prismProcess = null;

    /**
     * Toggles the state of the Prism Mock Server.
     *
     * @return The HTTP response that contains the method that is run based on the current state of the Prism mock server.
     */
    @GetMapping("/toggle-prism-mock")
    public ResponseEntity<Object> togglePrismMockServer() {
        if (prismProcess == null) {
            return startPrismMockServer();
        } else {
            return restartPrismMockServer();
        }
    }

    /**
     * Starts the Prism Mock Server.
     *
     * @return The HTTP response that indicates whether the Prism mock server has started.
     */
    private ResponseEntity<Object> startPrismMockServer() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        try {
            String npmPath = Paths.get("node", "npm.cmd").toString();
            processBuilder.command(npmPath, "run", "start");
            processBuilder.directory(new File("."));
            prismProcess = processBuilder.start();
            logOutput(prismProcess);
            return ResponseEntity.ok().body(Map.of("message", "Prism Mock Server is starting..."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to start Prism Mock Server due to exception: " + e.getMessage()));
        }
    }

    /**
     * Restarts the Prism Mock Server.
     *
     * @return The HTTP response that indicates whether the Prism mock server has restarted.
     */
    private ResponseEntity<Object> restartPrismMockServer() {
        stopPrismMockServer();
        return startPrismMockServer();
    }

    /**
     * Stops the Prism Mock Server.
     */
    private void stopPrismMockServer() {
        if (prismProcess != null) {
            prismProcess.destroy();
            prismProcess = null;
        }
    }

    /**
     * Logs the output of the provided process.
     *
     * @param process The process of which its output is logged.
     */
    private void logOutput(Process process) {
        new Thread(() -> {
            try (BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = input.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try (BufferedReader input = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = input.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @GetMapping("/test-openapi")
    public ResponseEntity<List<Map<String, Object>>> testOpenApiSpecification() {
        List<Map<String, Object>> tests = new ArrayList<>();
        List<String> paths = extractPathsFromOpenAPISpec("./data/export.yml");

        for (String path : paths) {
            if (path.contains("/{id}")) {
                String resourcePath = path.replace("/{id}", "");
                Optional<String> sampleId = getSampleId(resourcePath);
                if (sampleId.isPresent()) {
                    String resolvedPath = path.replace("{id}", sampleId.get());
                    addTestCases(tests, resolvedPath);
                } else {
                    addTestCases(tests, resourcePath);
                }
            } else {
                addTestCases(tests, path);
            }
        }

        return ResponseEntity.ok(tests);
    }

    private void addTestCases(List<Map<String, Object>> tests, String path) {
        tests.add(Map.of("name", "Test GET " + path, "method", "GET", "path", path));
        tests.add(Map.of("name", "Test POST " + path, "method", "POST", "path", path, "body", "{\"example\": \"data\"}"));
        tests.add(Map.of("name", "Test PUT " + path, "method", "PUT", "path", path, "body", "{\"example\": \"updated data\"}"));
        tests.add(Map.of("name", "Test DELETE " + path, "method", "DELETE", "path", path));
    }

    private Optional<String> getSampleId(String resourcePath) {
        try {
            String url = "http://localhost:8080" + resourcePath;
            ProcessBuilder processBuilder = new ProcessBuilder("curl", "-s", url);
            Process process = processBuilder.start();
            process.waitFor();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(process.getInputStream());
            if (rootNode.isArray() && rootNode.size() > 0) {
                return Optional.of(rootNode.get(0).get("id").asText());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
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
