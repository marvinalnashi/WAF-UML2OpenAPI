package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the GenerationController class.
 */
public class GenerationControllerTests {

    @Mock
    private OpenAPISpecGenerator openAPISpecGenerator;

    @InjectMocks
    private GenerationController generationController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Tests parsing individual UML elements from an uploaded UML diagram file.
     * Expects a 200 OK response with the parsed elements.
     */
    @Test
    public void testParseDiagramElements() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.mdj", "application/json", "{}".getBytes());
        ResponseEntity<Map<String, Object>> response = generationController.parseDiagramElements(file);
        assertEquals(200, response.getStatusCodeValue());
    }

    /**
     * Tests generating an OpenAPI specification.
     * Expects a 200 OK response with a message that indicates that the generation process was successful.
     */
    @Test
    public void testGenerateOpenAPISpec() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.mdj", "application/json", "{}".getBytes());
        when(openAPISpecGenerator.generateSpec(any(), any(), any(), any(), any(), any())).thenReturn("Generated OpenAPI specification");

        generationController.umlDataStore.put("classes", Collections.singletonMap("Test", Collections.emptyList()));
        generationController.umlDataStore.put("attributes", Collections.singletonMap("Test", Collections.emptyList()));
        generationController.umlDataStore.put("methods", Collections.singletonMap("Test", Collections.emptyList()));

        ResponseEntity<Map<String, String>> response = generationController.generateOpenAPISpec(file, "{}");
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Generated OpenAPI specification", response.getBody().get("message"));
    }

    /**
     * Tests parsing individual UML elements from an uploaded UML diagram file with an invalid file type.
     * Expects a 415 Unsupported Media Type response.
     */
    @Test
    public void testParseDiagramElementsWithInvalidFileType() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "Invalid content".getBytes());
        ResponseEntity<Map<String, Object>> response = generationController.parseDiagramElements(file);
        assertEquals(415, response.getStatusCodeValue());
    }
}
