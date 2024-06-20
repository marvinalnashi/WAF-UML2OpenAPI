package codearise.openapispecgenerator.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity that represents a stepper session.
 */
@Entity
public class StepperSession {
    /**
     * The unique identifier for a stepper session.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The UML diagram that was uploaded during a stepper session.
     */
    @Column(name = "uml_diagram_url")
    private String umlDiagramUrl;

    /**
     * The OpenAPI specification that was generated during a stepper session.
     */
    @Lob
    @Column(name = "open_api_spec", columnDefinition = "TEXT")
    private String openApiSpec;

    /**
     * The date/timestamp that indicates when the button to save a stepper session and restart the stepper was clicked in the Manage step of the stepper during a session.
     */
    private LocalDateTime createdAt;

    // Getters and setters for StepperSession

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUmlDiagramUrl() {
        return umlDiagramUrl;
    }

    public void setUmlDiagramUrl(String umlDiagramUrl) {
        this.umlDiagramUrl = umlDiagramUrl;
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
