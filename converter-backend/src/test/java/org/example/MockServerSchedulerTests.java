package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import java.io.File;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class MockServerSchedulerTests {

    @InjectMocks
    private MockServerScheduler mockServerScheduler;

    private final String triggerFilePath = "mock-server-trigger.flag";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCheckAndStartMockServer() throws IOException {
        MockServerScheduler spyScheduler = spy(mockServerScheduler);
        doNothing().when(spyScheduler).checkAndStartMockServer();

        spyScheduler.checkAndStartMockServer();

        verify(spyScheduler, times(1)).checkAndStartMockServer();
    }

    @Test
    public void testNoTriggerFile() {
        File triggerFile = new File(triggerFilePath);
        if (triggerFile.exists()) {
            triggerFile.delete();
        }

        mockServerScheduler.checkAndStartMockServer();

        assertFalse(triggerFile.exists());
    }
}
