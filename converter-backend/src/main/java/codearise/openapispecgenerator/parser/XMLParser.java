package codearise.openapispecgenerator.parser;

import codearise.openapispecgenerator.entity.Relationship;
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

/**
 * Implementation of the DiagramParser interface for XML diagrams.
 */
public class XMLParser implements DiagramParser {
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
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(inputStream);
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
                        extractValue(elements, value, elementType, currentClass);
                    }
                }
            }
        }
        return elements;
    }

    /**
     * Extracts the value of the specified element type.
     *
     * @param elements Map in which the key is the classname and the value is the element.
     * @param value The value to extract from the element type.
     * @param elementType The type of the parsed element.
     * @param currentClass The name of the current class.
     */
    private void extractValue(Map<String, List<String>> elements, String value, String elementType, String currentClass) {
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

    /**
     * Extracts the classname from the specified value.
     *
     * @param value The value containing the classname.
     * @return The extracted classname.
     */
    private String extractClassName(String value) {
        if (value.contains("Class<")) {
            return value.substring(value.indexOf("Class<") + 6, value.indexOf(">"));
        }
        return value.replaceAll("<.*?>", "").trim();
    }

    @Override
    public List<Relationship> parseRelationships(InputStream inputStream) throws Exception {
        List<Relationship> relationships = new ArrayList<>();
        Map<String, String> idToClassName = new HashMap<>();

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(inputStream);
        doc.getDocumentElement().normalize();

        NodeList mxCellNodes = doc.getElementsByTagName("mxCell");
        
        for (int i = 0; i < mxCellNodes.getLength(); i++) {
            Element element = (Element) mxCellNodes.item(i);
            String id = element.getAttribute("id");
            String value = element.getAttribute("value").replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&amp;", "&");

            if (element.getAttribute("style").contains("swimlane")) {
                idToClassName.put(id, extractClassName(value));
            }
        }

        for (int i = 0; i < mxCellNodes.getLength(); i++) {
            Element element = (Element) mxCellNodes.item(i);

            if ("1".equals(element.getAttribute("edge"))) {
                String fromId = element.getAttribute("source");
                String toId = element.getAttribute("target");
                String relationshipName = element.getAttribute("value");

                if (relationshipName == null || relationshipName.isEmpty()) {
                    NodeList childNodes = element.getChildNodes();
                    for (int j = 0; j < childNodes.getLength(); j++) {
                        Node childNode = childNodes.item(j);
                        if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element childElement = (Element) childNode;
                            if (childElement.getNodeName().equals("mxGeometry")) {
                                relationshipName = childElement.getAttribute("value");
                                if (relationshipName != null && !relationshipName.isEmpty()) {
                                    break;
                                }
                            }
                        }
                    }
                }

                String fromClass = idToClassName.get(fromId);
                String toClass = idToClassName.get(toId);

                if (fromClass == null || fromClass.isEmpty()) {
                    fromClass = "Unknown Class (ID: " + fromId + ")";
                }
                if (toClass == null || toClass.isEmpty()) {
                    toClass = "Unknown Class (ID: " + toId + ")";
                }

                if (relationshipName == null || relationshipName.isEmpty()) {
                    relationshipName = "Unnamed Relationship";
                }

                relationships.add(new Relationship(fromClass, toClass, relationshipName));
            }
        }

        return relationships;
    }
}
