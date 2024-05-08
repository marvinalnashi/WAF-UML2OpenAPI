package org.example;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class MockServerScheduler {

    private final String triggerFilePath = "mock-server-trigger.flag";

    @Scheduled(fixedDelay = 5000)
    public void checkAndStartMockServer() {
        File triggerFile = new File(triggerFilePath);
        if (triggerFile.exists()) {
            try {
                ProcessBuilder pb = new ProcessBuilder("prism", "mock", "-p", "4010", "--cors", "export.yml");
                pb.start();
                System.out.println("Mock server started.");
                boolean deleted = triggerFile.delete();
                if (!deleted) {
                    System.err.println("Failed to delete trigger file.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
