package org.example;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class StepperSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String umlDiagram;
    private String openApiSpec;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUmlDiagram() {
        return umlDiagram;
    }

    public void setUmlDiagram(String umlDiagram) {
        this.umlDiagram = umlDiagram;
    }

    public String getOpenApiSpec() {
        return openApiSpec;
    }

    public void setOpenApiSpec(String openApiSpec) {
        this.openApiSpec = openApiSpec;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
