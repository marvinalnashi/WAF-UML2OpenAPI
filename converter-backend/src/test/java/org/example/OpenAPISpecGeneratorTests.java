package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.io.File;
import java.util.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class OpenAPISpecGeneratorTests {

    @Mock
    private OpenAPISpecGenerator openAPISpecGenerator;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGenerateSpec() throws Exception {
        Map<String, List<String>> classes = Map.of("Test", List.of("attribute"));
        Map<String, List<String>> attributes = Map.of("Test", List.of("+attribute : string"));
        Map<String, List<String>> methods = Map.of("Test", List.of("+method()"));
        List<Map<String, Object>> mappings = new ArrayList<>();
        String outputPath = "/data/export.yml";
        Map<String, Map<String, Boolean>> selectedHttpMethods = Map.of("Test", Map.of("GET", true));
        String expectedResponse = "OpenAPI specification generated successfully at " + outputPath;
        when(openAPISpecGenerator.generateSpec(classes, attributes, methods, mappings, outputPath, selectedHttpMethods)).thenReturn(expectedResponse);
        String response = openAPISpecGenerator.generateSpec(classes, attributes, methods, mappings, outputPath, selectedHttpMethods);
        assertEquals(expectedResponse, response);
    }

    @Test
    public void testGenerateSpecWithNoData() throws Exception {
        Map<String, List<String>> classes = Collections.emptyMap();
        Map<String, List<String>> attributes = Collections.emptyMap();
        Map<String, List<String>> methods = Collections.emptyMap();
        List<Map<String, Object>> mappings = new ArrayList<>();
        String outputPath = "/data/export.yml";
        Map<String, Map<String, Boolean>> selectedHttpMethods = Collections.emptyMap();
        String expectedResponse = "OpenAPI specification generated successfully at " + outputPath;
        when(openAPISpecGenerator.generateSpec(classes, attributes, methods, mappings, outputPath, selectedHttpMethods)).thenReturn(expectedResponse);
        String response = openAPISpecGenerator.generateSpec(classes, attributes, methods, mappings, outputPath, selectedHttpMethods);
        assertEquals(expectedResponse, response);
    }
}
