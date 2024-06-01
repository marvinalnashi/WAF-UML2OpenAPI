package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PersonaliseControllerTests {

    @InjectMocks
    private PersonaliseController personaliseController;

    private ObjectMapper yamlMapper;
    private ObjectMapper jsonMapper;

//    @BeforeEach
//    public void setUp() {
//        MockitoAnnotations.openMocks(this);
//        yamlMapper = new ObjectMapper(new YAMLFactory());
//        jsonMapper = new ObjectMapper();
//    }

//    @Test
//    public void testGetPersonalisedData() throws IOException {
//        File tempFile = File.createTempFile("export", ".yml");
//        tempFile.deleteOnExit();
//        try (FileWriter writer = new FileWriter(tempFile)) {
//            writer.write(
//                    "components:\n" +
//                            "  schemas:\n" +
//                            "    Test:\n" +
//                            "      properties:\n" +
//                            "        attribute:\n" +
//                            "          examples:\n" +
//                            "            exampleArray:\n" +
//                            "              - attribute: oldValue\n"
//            );
//        }
//        Map<String, Object> yamlContent = yamlMapper.readValue(tempFile, Map.class);
//        String jsonContent = jsonMapper.writeValueAsString(yamlContent);
//        Map<String, Object> expectedResponse = jsonMapper.readValue(jsonContent, Map.class);
//        Map<String, Object> actualResponse = personaliseController.getPersonalisedData();
//        assertEquals(expectedResponse, actualResponse);
//    }
}
