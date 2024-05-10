package org.example;

import java.util.List;
import java.util.Map;

public class MappingDetails {
    private List<Map<String, Object>> mappings;

    public List<Map<String, Object>> getMappings() {
        return mappings;
    }

    public void setMappings(List<Map<String, Object>> mappings) {
        this.mappings = mappings;
    }
}