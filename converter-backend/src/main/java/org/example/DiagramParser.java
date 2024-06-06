package org.example;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * The interface which parser classes inherit from.
 */
public interface DiagramParser {
    /**
     * Parses the diagram from the provided InputStream.
     *
     * @param inputStream The UML diagram that is being processed as input.
     * @return Map in which the key is the classname and the value contains the class details.
     * @throws Exception Is returned if an error occurs during the parsing process.
     */
    Map<String, List<String>> parse(InputStream inputStream) throws Exception;

    /**
     * Parses the attributes from the provided InputStream.
     *
     * @param inputStream The UML diagram that is being processed as input.
     * @return Map in which the key is the classname and the value is the attribute.
     * @throws Exception Is returned if an error occurs during the parsing process.
     */
    Map<String, List<String>> parseAttributes(InputStream inputStream) throws Exception;

    /**
     * Parses the methods from the provided InputStream.
     *
     * @param inputStream The UML diagram that is being processed as input.
     * @return Map in which the key is the classname and the value is the method.
     * @throws Exception Is returned if an error occurs during the parsing process.
     */
    Map<String, List<String>> parseMethods(InputStream inputStream) throws Exception;
}
