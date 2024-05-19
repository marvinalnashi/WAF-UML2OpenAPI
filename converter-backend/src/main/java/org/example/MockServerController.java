package org.example;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

@RestController
public class MockServerController {

    private Process prismProcess = null;

    @GetMapping("/toggle-prism-mock")
    public ResponseEntity<Object> togglePrismMockServer() {
        if (prismProcess == null) {
            return startPrismMockServer();
        } else {
            return restartPrismMockServer();
        }
    }

    private ResponseEntity<Object> startPrismMockServer() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        try {
            processBuilder.command("bash", "-c", "prism mock -p 4010 --cors --host 0.0.0.0 /data/export.yml --errors --dynamic");
            prismProcess = processBuilder.start();
            logOutput(prismProcess);
            return ResponseEntity.ok().body(Map.of("message", "Prism Mock Server is starting..."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to start Prism Mock Server due to exception: " + e.getMessage()));
        }
    }

    private ResponseEntity<Object> restartPrismMockServer() {
        stopPrismMockServer();
        return startPrismMockServer();
    }

    private void stopPrismMockServer() {
        if (prismProcess != null) {
            prismProcess.destroy();
            prismProcess = null;
        }
    }

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
}
