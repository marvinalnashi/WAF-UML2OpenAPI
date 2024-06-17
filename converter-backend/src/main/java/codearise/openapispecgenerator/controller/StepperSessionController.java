package codearise.openapispecgenerator.controller;

import codearise.openapispecgenerator.entity.StepperSession;
import codearise.openapispecgenerator.service.StepperSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * REST controller for managing the stepper sessions.
 */
@RestController
@RequestMapping("/api/sessions")
public class StepperSessionController {

    /**
     * The service for handling the functionalities related to the stepper session.
     */
    @Autowired
    private StepperSessionService service;

    /**
     * The endpoint to save a new stepper session.
     *
     * @param umlDiagram The uploaded UML diagram of a stepper session.
     * @param openApiSpec The generated OpenAPI specification of a stepper session.
     * @return The saved stepper session with its corresponding data.
     */
    @PostMapping(consumes = "multipart/form-data")
    public StepperSession saveSession(@RequestParam("umlDiagram") MultipartFile umlDiagram,
                                      @RequestParam("openApiSpec") String openApiSpec) throws IOException {
        return service.saveSession(umlDiagram.getBytes(), umlDiagram.getOriginalFilename(), openApiSpec);
    }

    /**
     * The endpoint to retrieve all stepper sessions.
     *
     * @return List that contains all the stepper sessions.
     */
    @GetMapping
    public List<StepperSession> getAllSessions() {
        return service.getAllSessions();
    }

    /**
     * The endpoint to retrieve a specific stepper session by its ID.
     *
     * @param id The ID of the stepper session to retrieve.
     * @return The retrieved stepper session.
     */
    @GetMapping("/{id}")
    public StepperSession getSession(@PathVariable Long id) {
        return service.getSession(id);
    }
}