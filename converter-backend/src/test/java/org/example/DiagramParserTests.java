package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.xml.sax.SAXParseException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DiagramParserTests {

    @InjectMocks
    private XMLParser xmlParser;

    @InjectMocks
    private UXFParser uxfParser;

    @InjectMocks
    private MDJParser mdjParser;

    @InjectMocks
    private PlantUMLParser plantUmlParser;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testMDJParser() throws Exception {
        InputStream inputStream = new ByteArrayInputStream("{ \"ownedElements\": [] }".getBytes());
        Map<String, List<String>> result = mdjParser.parse(inputStream);
        assertEquals(0, result.size());
    }

    @Test
    public void testPlantUMLParser() throws Exception {
        InputStream inputStream = new ByteArrayInputStream("class Test {}".getBytes());
        Map<String, List<String>> result = plantUmlParser.parse(inputStream);
        assertEquals(1, result.size());
        assertEquals("Test", result.keySet().iterator().next());
    }

    @Test
    public void testUXFParser() throws Exception {
        InputStream inputStream = new ByteArrayInputStream("<diagram><element><id>UMLClass</id><panel_attributes>Class Test</panel_attributes></element></diagram>".getBytes());
        Map<String, List<String>> result = uxfParser.parse(inputStream);
        assertEquals(1, result.size());
        assertEquals("Test", result.keySet().iterator().next());
    }

    @Test
    public void testXMLParser() throws Exception {
        InputStream inputStream = new ByteArrayInputStream("<mxGraphModel><root><mxCell id=\"1\" style=\"swimlane\" value=\"Class&lt;Test&gt;\" parent=\"0\"/></root></mxGraphModel>".getBytes());
        Map<String, List<String>> result = xmlParser.parse(inputStream);
        assertEquals(1, result.size());
        assertEquals("Test", result.keySet().iterator().next());
    }

    @Test
    public void testParseInvalidFormat() {
        String invalidXML = "Invalid XML Content";
        InputStream inputStream = new ByteArrayInputStream(invalidXML.getBytes());
        assertThrows(SAXParseException.class, () -> {
            xmlParser.parse(inputStream);
        });
    }

}
