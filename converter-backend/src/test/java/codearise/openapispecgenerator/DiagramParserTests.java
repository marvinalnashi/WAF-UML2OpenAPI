package codearise.openapispecgenerator;

import codearise.openapispecgenerator.parser.MDJParser;
import codearise.openapispecgenerator.parser.UXFParser;
import codearise.openapispecgenerator.parser.XMLParser;
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

/**
 * Unit tests for the parser classes (XMLParser, UXFParser, MDJParser, and PUMLParser).
 */
public class DiagramParserTests {

    @InjectMocks
    private XMLParser xmlParser;

    @InjectMocks
    private UXFParser uxfParser;

    @InjectMocks
    private MDJParser mdjParser;

    @InjectMocks
    private codearise.openapispecgenerator.parser.PUMLParser PUMLParser;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Tests the StarUML MDJ parser class with a sample input stream.
     * Expects an empty result map as the input has no elements.
     */
    @Test
    public void testMDJParser() throws Exception {
        InputStream inputStream = new ByteArrayInputStream("{ \"ownedElements\": [] }".getBytes());
        Map<String, List<String>> result = mdjParser.parse(inputStream);
        assertEquals(0, result.size());
    }

    /**
     * Tests the PlantUML PUML parser class with a sample input stream.
     * Expects a result map that contains the class Test.
     */
    @Test
    public void testPlantUMLParser() throws Exception {
        InputStream inputStream = new ByteArrayInputStream("class Test {}".getBytes());
        Map<String, List<String>> result = PUMLParser.parse(inputStream);
        assertEquals(1, result.size());
        assertEquals("Test", result.keySet().iterator().next());
    }

    /**
     * Tests the UMLet UXF parser class with a sample input stream.
     * Expects a result map that contains the class Test.
     */
    @Test
    public void testUXFParser() throws Exception {
        InputStream inputStream = new ByteArrayInputStream("<diagram><element><id>UMLClass</id><panel_attributes>Class Test</panel_attributes></element></diagram>".getBytes());
        Map<String, List<String>> result = uxfParser.parse(inputStream);
        assertEquals(1, result.size());
        assertEquals("Test", result.keySet().iterator().next());
    }

    /**
     * Tests the Draw.io XML parser class with a sample input stream.
     * Expects a result map that contains the class Test.
     */
    @Test
    public void testXMLParser() throws Exception {
        InputStream inputStream = new ByteArrayInputStream("<mxGraphModel><root><mxCell id=\"1\" style=\"swimlane\" value=\"Class&lt;Test&gt;\" parent=\"0\"/></root></mxGraphModel>".getBytes());
        Map<String, List<String>> result = xmlParser.parse(inputStream);
        assertEquals(1, result.size());
        assertEquals("Test", result.keySet().iterator().next());
    }

    /**
     * Tests the Draw.io XML parser class with invalid XML content.
     * Expects an exception to be thrown.
     */
    @Test
    public void testParseInvalidFormat() {
        String invalidXML = "Invalid XML Content";
        InputStream inputStream = new ByteArrayInputStream(invalidXML.getBytes());
        assertThrows(SAXParseException.class, () -> {
            xmlParser.parse(inputStream);
        });
    }

    /**
     * Tests the Draw.io XML parser class with malformed XML content.
     * Expects an exception to be thrown.
     */
    @Test
    public void testParseMalformedXML() {
        String malformedXML = "<mxGraphModel><root><mxCell id=\"1\" style=\"swimlane\" value=\"Class<Test\" parent=\"0\"></root></mxGraphModel>";
        InputStream inputStream = new ByteArrayInputStream(malformedXML.getBytes());
        assertThrows(SAXParseException.class, () -> {
            xmlParser.parse(inputStream);
        });
    }
}
