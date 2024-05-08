package org.example;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface DiagramParser {
    Map<String, List<String>> parse(InputStream diagramStream) throws Exception;
    Map<String, List<String>> parseAttributes(InputStream diagramStream) throws Exception;
    Map<String, List<String>> parseMethods(InputStream diagramStream) throws Exception;
}
