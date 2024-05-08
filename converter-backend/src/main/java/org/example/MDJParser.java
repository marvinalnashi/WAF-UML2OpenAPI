package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MDJParser implements DiagramParser {

    @Override
    public Map<String, List<String>> parse(InputStream mdjInputStream) throws Exception {
        return parseElementsByType(mdjInputStream, "class");
    }

    @Override
    public Map<String, List<String>> parseAttributes(InputStream mdjInputStream) throws Exception {
        return parseElementsByType(mdjInputStream, "attribute");
    }

    @Override
    public Map<String, List<String>> parseMethods(InputStream mdjInputStream) throws Exception {
        return parseElementsByType(mdjInputStream, "method");
    }

    private Map<String, List<String>> parseElementsByType(InputStream mdjInputStream, String elementType) throws Exception {
        Map<String, List<String>> elements = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(mdjInputStream);
        JsonNode ownedElements = rootNode.path("ownedElements");

        for (JsonNode ownedElement : ownedElements) {
            JsonNode modelElements = ownedElement.path("ownedElements");
            for (JsonNode modelElement : modelElements) {
                if ("UMLClass".equals(modelElement.path("_type").asText())) {
                    String className = modelElement.path("name").asText();
                    elements.computeIfAbsent(className, k -> new ArrayList<>());
                    extractDetails(modelElement, elements, className, elementType);
                }
            }
        }
        return elements;
    }

    private void extractDetails(JsonNode modelElement, Map<String, List<String>> elements, String className, String type) {
        if (type.equals("attribute")) {
            JsonNode attributes = modelElement.path("attributes");
            for (JsonNode attribute : attributes) {
                String attributeName = attribute.path("name").asText();
                String attributeType = attribute.path("type").asText();
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
