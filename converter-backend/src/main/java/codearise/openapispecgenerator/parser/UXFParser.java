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
 * Implementation of the DiagramParser interface for UXF diagrams.
 */
public class UXFParser implements DiagramParser {
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

    @Override
    public List<Relationship> parseRelationships(InputStream inputStream) throws Exception {
        List<Relationship> relationships = new ArrayList<>();

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(inputStream);
        doc.getDocumentElement().normalize();

        NodeList relationNodes = doc.getElementsByTagName("element");

        for (int i = 0; i < relationNodes.getLength(); i++) {
            Element element = (Element) relationNodes.item(i);

            if (element.getAttribute("type").equals("Relation")) {
                String fromClass = element.getAttribute("source");
                String toClass = element.getAttribute("target");
                String relationshipType = element.getAttribute("name");

                relationships.add(new Relationship(fromClass, toClass, relationshipType));
            }
        }

        return relationships;
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
        NodeList nodeList = doc.getElementsByTagName("element");

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                String id = element.getAttribute("id");
                if ("UMLClass".equals(id)) {
                    addDetails(element, elements, elementType);
                }
            }
        }
        return elements;
    }

    /**
     * Adds details to parsed elements, such as brackets for methods and data types for attributes, to distinguish them.
     *
     * @param element The name of the class element that is processed.
     * @param elements Map in which the key is the classname and the value is the element.
     * @param elementType The type of the parsed element.
     */
    private void addDetails(Element element, Map<String, List<String>> elements, String elementType) {
        String className = element.getAttribute("name");

        if ("class".equals(elementType)) {
            elements.computeIfAbsent(className, k -> new ArrayList<>()).add("Class: " + className);
        } else {
            NodeList children = element.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node childNode = children.item(i);
                if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element childElement = (Element) childNode;
                    String name = childElement.getAttribute("name");
                    if ("attribute".equals(elementType) && childElement.getTagName().equals("attribute")) {
                        elements.computeIfAbsent(className, k -> new ArrayList<>()).add(name);
                    } else if ("method".equals(elementType) && childElement.getTagName().equals("method")) {
                        elements.computeIfAbsent(className, k -> new ArrayList<>()).add(name + "()");
                    }
                }
            }
        }
    }
}
