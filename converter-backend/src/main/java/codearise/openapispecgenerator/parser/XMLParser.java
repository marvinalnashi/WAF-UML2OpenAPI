package codearise.openapispecgenerator.parser;

import codearise.openapispecgenerator.entity.Relationship;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.InputStream;
import java.util.*;

/**
 * Implementation of the DiagramParser interface for XML diagrams.
 */
public class XMLParser implements DiagramParser {
    // TODO: Fix bugs with handling various relationship types, cardinalities, etc. Right now, the relationships of most Draw.io XML class diagram files are handled correctly but some edge cases still need attention.

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
        Document doc = Jsoup.parse(inputStream, "UTF-8", "");

        Map<String, List<String>> elements = new HashMap<>();
        Map<String, String> classContexts = new HashMap<>();

        Elements cells = doc.select("mxCell");

        for (Element cell : cells) {
            String style = cell.attr("style");
            String value = cell.attr("value").replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&amp;", "&");
            String parentId = cell.attr("parent");

            if (style.contains("swimlane")) {
                String className = extractClassName(value);
                elements.put(className, new ArrayList<>());
                classContexts.put(cell.attr("id"), className);
            } else if (!value.isEmpty() && style.contains("text;")) {
                String currentClass = classContexts.get(parentId);
                if (currentClass != null) {
                    extractValue(elements, value, elementType, currentClass);
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
        Document doc = Jsoup.parse(inputStream, "UTF-8", "");

        List<Relationship> relationships = new ArrayList<>();
        Map<String, String> idToClassName = new HashMap<>();

        Elements cells = doc.select("mxCell");

        for (Element cell : cells) {
            String id = cell.attr("id");
            String value = cell.attr("value").replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&amp;", "&");

            if (cell.attr("style").contains("swimlane")) {
                idToClassName.put(id, extractClassName(value));
            }
        }

        for (Element cell : cells) {
            if ("1".equals(cell.attr("edge"))) {
                String fromId = cell.attr("source");
                String toId = cell.attr("target");
                String relationshipName = cell.attr("value");
                String fromClass = idToClassName.get(fromId);
                String toClass = idToClassName.get(toId);

                if (relationshipName == null || relationshipName.isEmpty()) {
                    relationshipName = extractRelationshipName(cell, idToClassName);
                }

                if (fromClass == null || fromClass.isEmpty()) {
                    fromClass = "Unknown Class (ID: " + fromId + ")";
                }
                if (toClass == null || toClass.isEmpty()) {
                    toClass = "Unknown Class (ID: " + toId + ")";
                }

                if (relationshipName == null || relationshipName.isEmpty()) {
                    relationshipName = "Unnamed Relationship";
                }

                String fromCardinality = extractCardinality(cell, "source");
                String toCardinality = extractCardinality(cell, "target");

                relationships.add(new Relationship(
                        fromClass + (fromCardinality != null ? " [" + fromCardinality + "]" : ""),
                        toClass + (toCardinality != null ? " [" + toCardinality + "]" : ""),
                        relationshipName
                ));
            }
        }

        return relationships;
    }

    private String extractRelationshipName(Element element, Map<String, String> idToClassName) {
        Elements childNodes = element.select("mxCell");
        for (Element childElement : childNodes) {
            String relationshipName = childElement.attr("value");
            if (relationshipName != null && !relationshipName.isEmpty()) {
                return relationshipName;
            }
        }

        String source = idToClassName.get(element.attr("source"));
        String target = idToClassName.get(element.attr("target"));
        if (source != null && target != null) {
            return source + " -> " + target;
        }
        return "";
    }

    private String extractCardinality(Element element, String direction) {
        Elements childNodes = element.select("mxCell");
        for (Element childElement : childNodes) {
            if ("1".equals(childElement.attr("edge"))) {
                String sourceOrTarget = direction.equals("source") ? "source" : "target";
                if (childElement.attr(sourceOrTarget).equals(element.attr("id"))) {
                    return childElement.attr("value");
                }
            }
        }
        return null;
    }
}
