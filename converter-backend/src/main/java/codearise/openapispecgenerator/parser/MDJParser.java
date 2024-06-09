package codearise.openapispecgenerator.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the DiagramParser interface for MDJ diagrams.
 */
public class MDJParser implements DiagramParser {
    /**
     * Parses the uploaded UML diagram to extract its classes.
     *
     * @param inputStream The UML diagram that is being processed as input.
     * @return Map in which the key is the classname and the value contains the class details.
     * @throws Exception Is returned if an error occurs during the parsing process.
     */
    @Override
    public Map<String, List<String>> parse(InputStream inputStream) throws Exception {
        return parseElementsByType(inputStream, "class");
    }

    /**
     * Parses the uploaded UML diagram to extract its attributes.
     *
     * @param inputStream The UML diagram that is being processed as input.
     * @return Map in which the key is the classname and the value is the attribute.
     * @throws Exception Is returned if an error occurs during the parsing process.
     */
    @Override
    public Map<String, List<String>> parseAttributes(InputStream inputStream) throws Exception {
        return parseElementsByType(inputStream, "attribute");
    }

    /**
     * Parses the uploaded UML diagram to extract its methods.
     *
     * @param inputStream The UML diagram that is being processed as input.
     * @return Map in which the key is the classname and the value is the method.
     * @throws Exception Is returned if an error occurs during the parsing process.
     */
    @Override
    public Map<String, List<String>> parseMethods(InputStream inputStream) throws Exception {
        return parseElementsByType(inputStream, "method");
    }

    /**
     * Parses the uploaded UML diagram to extract its elements and distinguishes them based on their types.
     *
     * @param inputStream The UML diagram that is being processed as input.
     * @param elementType The type of element to parse.
     * @return Map in which the key is the classname and the value is the element.
     * @throws Exception Is returned if an error occurs during the parsing process.
     */
    private Map<String, List<String>> parseElementsByType(InputStream inputStream, String elementType) throws Exception {
        Map<String, List<String>> elements = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(inputStream);
        JsonNode ownedElements = rootNode.path("ownedElements");

        for (JsonNode ownedElement : ownedElements) {
            JsonNode modelElements = ownedElement.path("ownedElements");
            for (JsonNode modelElement : modelElements) {
                if ("UMLClass".equals(modelElement.path("_type").asText())) {
                    String className = modelElement.path("name").asText();
                    elements.computeIfAbsent(className, k -> new ArrayList<>());
                    addDetails(modelElement, elements, className, elementType);
                }
            }
        }
        return elements;
    }

    /**
     * Adds details to parsed elements, such as brackets for methods and data types for attributes, to distinguish them.
     *
     * @param modelElement The model element node in the Jackson tree.
     * @param elements Map in which the key is the classname and the value is the element.
     * @param className The classname linked to the traversed element.
     * @param type The type of the traversed element.
     */
    private void addDetails(JsonNode modelElement, Map<String, List<String>> elements, String className, String type) {
        if (type.equals("attribute")) {
            JsonNode attributes = modelElement.path("attributes");
            for (JsonNode attribute : attributes) {
                String attributeName = attribute.path("name").asText();
                String attributeType = attribute.path("type").asText();
                if (attributeType.isEmpty()) {
                    attributeType = "String";
                }
                elements.computeIfAbsent(className, k -> new ArrayList<>())
                        .add("+" + attributeName + " : " + attributeType);
            }
        } else if (type.equals("method")) {
            JsonNode operations = modelElement.path("operations");
            for (JsonNode operation : operations) {
                String methodName = operation.path("name").asText();
                elements.computeIfAbsent(className, k -> new ArrayList<>())
                        .add("+" + methodName + "()");
            }
        }
    }
}
