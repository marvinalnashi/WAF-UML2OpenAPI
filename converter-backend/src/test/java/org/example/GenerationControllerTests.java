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

public class GenerationControllerTests {

    @Mock
    private OpenAPISpecGenerator openAPISpecGenerator;

    @InjectMocks
    private GenerationController generationController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testParseDiagramElements() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.mdj", "application/json", "{}".getBytes());
        ResponseEntity<Map<String, Object>> response = generationController.parseDiagramElements(file);
        assertEquals(200, response.getStatusCodeValue());
    }

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
}
