package org.example;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UXFParser implements DiagramParser {

    @Override
    public Map<String, List<String>> parse(InputStream diagramStream) throws Exception {
        return parseElementsByType(diagramStream, "class");
    }

    @Override
    public Map<String, List<String>> parseAttributes(InputStream diagramStream) throws Exception {
        return parseElementsByType(diagramStream, "attribute");
    }

    @Override
    public Map<String, List<String>> parseMethods(InputStream diagramStream) throws Exception {
        return parseElementsByType(diagramStream, "method");
    }

    private Map<String, List<String>> parseElementsByType(InputStream diagramStream, String elementType) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(diagramStream);
        doc.getDocumentElement().normalize();

        Map<String, List<String>> elements = new HashMap<>();
        NodeList nodeList = doc.getElementsByTagName("element");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                String id = element.getElementsByTagName("id").item(0).getTextContent();
                if ("UMLClass".equals(id)) {
                    processClassElement(element, elements, elementType);
                }
            }
        }
        return elements;
    }

    private void processClassElement(Element element, Map<String, List<String>> elements, String elementType) {
        String panelAttributes = element.getElementsByTagName("panel_attributes").item(0).getTextContent();
        String[] lines = panelAttributes.split("\\n");
        String className = lines[0].replace("Class ", "").trim();

        if ("class".equals(elementType)) {
            elements.computeIfAbsent(className, k -> new ArrayList<>()).add("Details for class: " + className);
        } else {
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.startsWith("+")) {
                    if ("attribute".equals(elementType) && !line.contains("(")) {
                        elements.computeIfAbsent(className, k -> new ArrayList<>()).add(line);
                    } else if ("method".equals(elementType) && line.contains("(")) {
                        elements.computeIfAbsent(className, k -> new ArrayList<>()).add(line);
                    }
                }
            }
        }
    }
}
