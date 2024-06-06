package org.example;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PUMLParser implements DiagramParser {

    @Override
    public Map<String, List<String>> parse(InputStream inputStream) throws Exception {
        return parsePlantUMLByType(inputStream, "class");
    }

    @Override
    public Map<String, List<String>> parseAttributes(InputStream inputStream) throws Exception {
        return parsePlantUMLByType(inputStream, "attribute");
    }

    @Override
    public Map<String, List<String>> parseMethods(InputStream inputStream) throws Exception {
        return parsePlantUMLByType(inputStream, "method");
    }

    private Map<String, List<String>> parsePlantUMLByType(InputStream inputStream, String elementType) throws Exception {
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

    private String extractClassName(String line) {
        String[] parts = line.split("[\\s{]");
        return parts[1];
    }
}
