package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
     * @param session The stepper session that is saved.
     * @return The saved stepper session with its corresponding data.
     */
    @PostMapping
    public StepperSession saveSession(@RequestBody StepperSession session) {
        return service.saveSession(session.getUmlDiagram(), session.getOpenApiSpec());
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