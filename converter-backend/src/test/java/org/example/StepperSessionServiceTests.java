package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the StepperSessionService class.
 */
public class StepperSessionServiceTests {

    @Mock
    private StepperSessionRepository repository;

    @InjectMocks
    private StepperSessionService service;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Tests saving a stepper session.
     * Expects the saved stepper session with the correct id.
     */
    @Test
    public void testSaveSession() {
        StepperSession session = new StepperSession();
        session.setId(1L);
        when(repository.save(any())).thenReturn(session);

        StepperSession result = service.saveSession("umlContent", "openApiContent");
        assertEquals(1L, result.getId());
    }

    /**
     * Tests fetching all sessions.
     * Expects the list of stepper sessions to contain the correct corresponding OpenAPI specifications.
     */
    @Test
    public void testGetAllSessions() throws IOException {
        StepperSession session = new StepperSession();
        session.setId(1L);
        session.setOpenApiSpec("{\"components\":{\"schemas\":{\"Test\":{\"properties\":{\"attribute\":\"string\"}}}}}");
        when(repository.findAll()).thenReturn(Collections.singletonList(session));

        List<StepperSession> result = service.getAllSessions();
        assertEquals(1, result.size());
        assertEquals("An OpenAPI specification that has one the class Test (with attribute attribute)", result.get(0).getOpenApiSpec());
    }

    /**
     * Tests fetching a session by id.
     * Expects the fetched stepper session with the correct id.
     */
    @Test
    public void testGetSession() {
        StepperSession session = new StepperSession();
        session.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(session));

        StepperSession result = service.getSession(1L);
        assertEquals(1L, result.getId());
    }
}
