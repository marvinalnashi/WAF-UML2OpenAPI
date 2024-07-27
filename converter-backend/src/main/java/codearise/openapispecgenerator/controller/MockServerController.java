package codearise.openapispecgenerator.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
            processBuilder.command("bash", "-c", "prism mock -p 4010 --cors --host 0.0.0.0 /data/export.yml --errors");
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
    public ResponseEntity<Object> testOpenApiSpecification() {
        List<Map<String, Object>> testResults = new ArrayList<>();
        List<String> paths = extractPathsFromOpenAPISpec("/data/export.yml");

        for (String path : paths) {
            if (path.endsWith("/{id}")) {
                testResults.add(testPath(path, "GET"));
                testResults.add(testPath(path, "PUT", "{\"example\": \"updated data\"}"));
                testResults.add(testPath(path, "DELETE"));
            } else {
                testResults.add(testPath(path, "GET"));
                testResults.add(testPath(path, "POST", "{\"example\": \"data\"}"));
            }
        }

        return ResponseEntity.ok().body(testResults);
    }

    private Map<String, Object> testPath(String path, String method) {
        return testPath(path, method, null);
    }

    private Map<String, Object> testPath(String path, String method, String body) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        try {
            String command = String.format("curl -X %s -H 'Content-Type: application/json' %s http://localhost:4010%s", method, body != null ? "-d '" + body + "'" : "", path);
            processBuilder.command("bash", "-c", command);
            Process testProcess = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(testProcess.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            int exitCode = testProcess.waitFor();
            if (exitCode == 0) {
                return Map.of("path", path, "method", method, "output", output.toString());
            } else {
                return Map.of("path", path, "method", method, "error", "Failed to test path.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("path", path, "method", method, "error", "Exception: " + e.getMessage());
        }
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