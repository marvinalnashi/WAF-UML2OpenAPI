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

public class XMLParser implements DiagramParser {

    @Override
    public Map<String, List<String>> parse(InputStream xmlInputStream) throws Exception {
        return parseElementsByType(xmlInputStream, "class");
    }

    @Override
    public Map<String, List<String>> parseAttributes(InputStream xmlInputStream) throws Exception {
        return parseElementsByType(xmlInputStream, "attribute");
    }

    @Override
    public Map<String, List<String>> parseMethods(InputStream xmlInputStream) throws Exception {
        return parseElementsByType(xmlInputStream, "method");
    }

    private Map<String, List<String>> parseElementsByType(InputStream xmlInputStream, String elementType) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlInputStream);
        doc.getDocumentElement().normalize();

        Map<String, List<String>> elements = new HashMap<>();
        NodeList nodeList = doc.getElementsByTagName("mxCell");
        Map<String, String> classContexts = new HashMap<>();

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element cell = (Element) node;
                String style = cell.getAttribute("style");
                String value = cell.getAttribute("value").replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&amp;", "&");
                String parentId = cell.getAttribute("parent");

                if (style.contains("swimlane")) {
                    String className = extractClassName(value);
                    elements.put(className, new ArrayList<>());
                    classContexts.put(cell.getAttribute("id"), className);
                } else if (!value.isEmpty() && style.contains("text;")) {
                    String currentClass = classContexts.get(parentId);
                    if (currentClass != null) {
                        handleValue(elements, value, elementType, currentClass);
                    }
                }
            }
        }
        return elements;
    }

    private void handleValue(Map<String, List<String>> elements, String value, String elementType, String currentClass) {
        String[] parts = value.split("<div>|<br>");
        for (String part : parts) {
            part = part.trim();
            if (part.startsWith("+")) {
                part = part.substring(1).trim();
                if (part.contains("(") && part.contains(")") && elementType.equals("method")) {
                    elements.get(currentClass).add("+" + part);
                } else if (part.contains(":") && !part.contains("(") && elementType.equals("attribute")) {
                    String[] detailParts = part.split(":");
                    String name = detailParts[0].trim();
                    String type = detailParts.length > 1 ? detailParts[1].trim() : "";
                    elements.get(currentClass).add("+" + name + " : " + type);
                }
            }
        }
    }

    private String extractClassName(String value) {
        return value.replaceAll("<.*?>", "").trim();
    }
}
