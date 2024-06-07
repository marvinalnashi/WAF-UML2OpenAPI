package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * Unit tests for the StepperSessionController class.
 */
public class StepperSessionControllerTests {

    @Mock
    private StepperSessionService service;

    @InjectMocks
    private StepperSessionController controller;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    /**
     * Tests saving a stepper session.
     * Expects a 200 OK response and a message that contains the stepper session's id.
     */
    @Test
    public void testSaveSession() throws Exception {
        StepperSession session = new StepperSession();
        session.setId(1L);
        when(service.saveSession(any(), any())).thenReturn(session);

        mockMvc.perform(post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"umlDiagram\":\"umlContent\",\"openApiSpec\":\"openApiContent\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    /**
     * Tests fetching all stepper sessions.
     * Expects a 200 OK response and a message that contains a list of all the fetched stepper sessions.
     */
    @Test
    public void testGetAllSessions() throws Exception {
        StepperSession session = new StepperSession();
        session.setId(1L);
        when(service.getAllSessions()).thenReturn(Collections.singletonList(session));

        mockMvc.perform(get("/api/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    /**
     * Tests fetching a stepper session by id.
     * Expects a 200 OK response and a message that contains the information about the fetched stepper session.
     */
    @Test
    public void testGetSession() throws Exception {
        StepperSession session = new StepperSession();
        session.setId(1L);
        when(service.getSession(1L)).thenReturn(session);

        mockMvc.perform(get("/api/sessions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }
}
