package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PersonaliseControllerTests {

    @InjectMocks
    private PersonaliseController personaliseController;

    private final String outputPath = "/data/export.yml";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        new File(outputPath).getParentFile().mkdirs();
    }

    @Test
    public void testGetPersonalisedData() throws IOException {
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> components = new HashMap<>();
        Map<String, Object> schemas = new HashMap<>();
        Map<String, Object> testSchema = new HashMap<>();
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> attribute = new HashMap<>();
        Map<String, Object> examples = new HashMap<>();
        examples.put("exampleArray", java.util.Collections.singletonList(new HashMap<String, Object>() {{
            put("attribute", "oldValue");
        }}));
        attribute.put("examples", examples);
        properties.put("attribute", attribute);
        testSchema.put("properties", properties);
        schemas.put("Test", testSchema);
        components.put("schemas", schemas);
        data.put("components", components);

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        File file = new File(outputPath);
        file.getParentFile().mkdirs();
        mapper.writeValue(file, data);

        Map<String, Object> result = personaliseController.getPersonalisedData();
        assertEquals(data, result);
    }
}
