package codearise.openapispecgenerator.parser;

import codearise.openapispecgenerator.entity.Relationship;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the DiagramParser interface for PUML diagrams.
 */
public class PUMLParser implements DiagramParser {
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
     * @param elementType The type of the parsed element.
     * @return Map in which the key is the classname and the value is the element.
     * @throws Exception Is returned if an error occurs during the parsing process.
     */
    private Map<String, List<String>> parseElementsByType(InputStream inputStream, String elementType) throws Exception {
        Map<String, List<String>> elements = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            String currentClass = null;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("class")) {
                    currentClass = extractClassName(line);
                    elements.computeIfAbsent(currentClass, k -> new ArrayList<>());
                } else if (currentClass != null && !line.startsWith("'")) {
                    if (elementType.equals("attribute") && line.contains(":") && !line.contains("(")) {
                        String attributeName = line.substring(0, line.indexOf(':')).trim();
                        String attributeType = line.substring(line.indexOf(':') + 1).trim();
                        elements.get(currentClass).add(attributeName + " : " + attributeType);
                    } else if (elementType.equals("method") && line.contains("(") && line.contains(")")) {
                        String methodName = line.substring(0, line.indexOf('(')).trim();
                        elements.get(currentClass).add(methodName + "()");
                    }
                }
            }
        }
        return elements;
    }

    @Override
    public List<Relationship> parseRelationships(InputStream inputStream) throws Exception {
        List<Relationship> relationships = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();

            if (line.matches(".+\\s+\".*\"\\s+[.-]{2,3}\\s+\".*\"\\s+.+\\s*:\\s*.+")) {
                String[] parts = line.split("\\s+[.-]{2,3}\\s+");
                if (parts.length == 2) {
                    String fromPart = parts[0].trim();
                    String toPart = parts[1].trim();

                    String fromClass = extractClassName(fromPart);
                    String toClass = extractClassName(toPart);

                    String relationshipName = "";
                    if (toPart.contains(":")) {
                        relationshipName = toPart.substring(toPart.indexOf(":") + 1).trim();
                        toClass = toClass.substring(0, toClass.indexOf(":")).trim();
                    }

                    relationships.add(new Relationship(fromClass, toClass, relationshipName));
                }
            } else if (line.matches(".+\\s+[.-]{2,3}\\s+.+")) {
                String[] parts = line.split("\\s+[.-]{2,3}\\s+");
                if (parts.length == 2) {
                    String fromClass = extractClassName(parts[0].trim());
                    String toClass = extractClassName(parts[1].trim());

                    relationships.add(new Relationship(fromClass, toClass, "Association"));
                }
            }
        }
        return relationships;
    }

    /**
     * Extracts the class name from a potentially complex string, such as one containing cardinalities or other annotations.
     *
     * @param rawInput The raw input string to process.
     * @return The extracted class name.
     */
    private String extractClassName(String rawInput) {
        if (rawInput.contains("\"")) {
            return rawInput.replaceAll("\"[^\"]*\"", "").trim();
        }
        return rawInput;
    }
}
