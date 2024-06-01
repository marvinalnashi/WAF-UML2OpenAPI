package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MockServerControllerTests {

    @InjectMocks
    private MockServerController mockServerController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testTogglePrismMockServer() {
        ResponseEntity<Object> response = mockServerController.togglePrismMockServer();
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Prism Mock Server is starting...", ((Map<String, String>) response.getBody()).get("message"));
    }
}
