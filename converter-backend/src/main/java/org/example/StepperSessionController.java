package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
public class StepperSessionController {

    @Autowired
    private StepperSessionService service;

    @PostMapping
    public StepperSession saveSession(@RequestBody StepperSession session) {
        return service.saveSession(session.getUmlDiagram(), session.getOpenApiSpec());
    }

    @GetMapping
    public List<StepperSession> getAllSessions() {
        return service.getAllSessions();
    }

    @GetMapping("/{id}")
    public StepperSession getSession(@PathVariable Long id) {
        return service.getSession(id);
    }
}