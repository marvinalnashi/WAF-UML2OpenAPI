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

    private final String outputPath = "data/export.yml";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        new File(outputPath).getParentFile().mkdirs();
    }
}
