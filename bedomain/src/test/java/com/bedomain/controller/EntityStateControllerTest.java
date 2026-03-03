package com.bedomain.controller;

import com.bedomain.domain.dto.entityinstance.EntityInstanceResponse;
import com.bedomain.domain.dto.statemachine.StateHistoryResponse;
import com.bedomain.domain.dto.statemachine.TriggerTransitionRequest;
import com.bedomain.exception.InvalidTransitionException;
import com.bedomain.service.StateHistoryService;
import com.bedomain.service.StateTransitionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EntityStateControllerTest {

    @Mock
    private StateTransitionService stateTransitionService;

    @Mock
    private StateHistoryService stateHistoryService;

    @InjectMocks
    private EntityStateController entityStateController;

    private UUID entityId;
    private UUID entityTypeId;
    private EntityInstanceResponse testResponse;
    private StateHistoryResponse historyResponse;

    @BeforeEach
    void setUp() {
        entityId = UUID.randomUUID();
        entityTypeId = UUID.randomUUID();

        testResponse = EntityInstanceResponse.builder()
                .id(entityId)
                .entityTypeId(entityTypeId)
                .entityTypeName("TestEntity")
                .attributes(Map.of("field1", "value1"))
                .currentState("ACTIVE")
                .createdAt(Instant.now())
                .createdBy("test-user")
                .updatedAt(Instant.now())
                .updatedBy("test-user")
                .build();

        historyResponse = StateHistoryResponse.builder()
                .id(UUID.randomUUID())
                .fromState("DRAFT")
                .toState("ACTIVE")
                .event("ACTIVATE")
                .triggeredBy("test-user")
                .timestamp(Instant.now())
                .build();
    }

    @Test
    void triggerTransition_ReturnsOk() {
        TriggerTransitionRequest request = new TriggerTransitionRequest();
        request.setEvent("ACTIVATE");

        when(stateTransitionService.triggerTransition(entityId, "ACTIVATE")).thenReturn(testResponse);

        ResponseEntity<EntityInstanceResponse> response = entityStateController.triggerTransition(entityId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ACTIVE", response.getBody().getCurrentState());
    }

    @Test
    void triggerTransition_InvalidTransition_ReturnsBadRequest() {
        TriggerTransitionRequest request = new TriggerTransitionRequest();
        request.setEvent("INVALID");

        when(stateTransitionService.triggerTransition(entityId, "INVALID"))
                .thenThrow(new InvalidTransitionException("Invalid transition"));

        assertThrows(InvalidTransitionException.class, () ->
                entityStateController.triggerTransition(entityId, request));
    }

    @Test
    void getHistory_ReturnsOk() {
        when(stateHistoryService.getHistoryForEntity(entityId)).thenReturn(List.of(historyResponse));

        ResponseEntity<List<StateHistoryResponse>> response = entityStateController.getHistory(entityId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getHistory_ReturnsEmptyList() {
        when(stateHistoryService.getHistoryForEntity(entityId)).thenReturn(List.of());

        ResponseEntity<List<StateHistoryResponse>> response = entityStateController.getHistory(entityId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void getCurrentState_ReturnsOk() {
        when(stateTransitionService.getCurrentState(entityId)).thenReturn("ACTIVE");

        ResponseEntity<String> response = entityStateController.getCurrentState(entityId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("ACTIVE", response.getBody());
    }

    @Test
    void getCurrentState_ReturnsNull() {
        when(stateTransitionService.getCurrentState(entityId)).thenReturn(null);

        ResponseEntity<String> response = entityStateController.getCurrentState(entityId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
    }
}
