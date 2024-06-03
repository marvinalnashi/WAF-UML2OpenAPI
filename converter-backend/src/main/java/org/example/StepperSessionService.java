package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StepperSessionService {

    @Autowired
    private StepperSessionRepository repository;

    public StepperSession saveSession(String umlDiagram, String openApiSpec) {
        StepperSession session = new StepperSession();
        session.setUmlDiagram(umlDiagram);
        session.setOpenApiSpec(openApiSpec);
        session.setCreatedAt(LocalDateTime.now());
        return repository.save(session);
    }

    public List<StepperSession> getAllSessions() {
        return repository.findAll();
    }

    public StepperSession getSession(Long id) {
        return repository.findById(id).orElse(null);
    }
}
