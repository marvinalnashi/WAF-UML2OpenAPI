package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Integration test for the MockServerController class.
 * The behaviour of the MockServerController class is tested.
 */
public class MockServerControllerTests {

    @InjectMocks
    private MockServerController mockServerController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Tests starting the Prism Mock Server.
     * Expects a 200 OK response with a success message.
     */
    @Test
    public void testTogglePrismMockServer() {
        ResponseEntity<Object> response = mockServerController.togglePrismMockServer();
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Prism Mock Server is starting...", ((Map<String, String>) response.getBody()).get("message"));
    }
}
