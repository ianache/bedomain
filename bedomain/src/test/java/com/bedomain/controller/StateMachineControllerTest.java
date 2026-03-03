package com.bedomain.controller;

import com.bedomain.domain.dto.statemachine.CreateStateMachineRequest;
import com.bedomain.domain.dto.statemachine.StateMachineResponse;
import com.bedomain.domain.dto.statemachine.UpdateStateMachineRequest;
import com.bedomain.exception.EntityNotFoundException;
import com.bedomain.service.StateMachineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StateMachineControllerTest {

    @Mock
    private StateMachineService stateMachineService;

    @InjectMocks
    private StateMachineController stateMachineController;

    private UUID testId;
    private StateMachineResponse testResponse;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        
        testResponse = StateMachineResponse.builder()
            .id(testId)
            .name("TestStateMachine")
            .description("Test Description")
            .entityTypeId(UUID.randomUUID())
            .entityTypeName("TestEntityType")
            .createdAt(Instant.now())
            .createdBy("test-user")
            .updatedAt(Instant.now())
            .updatedBy("test-user")
            .build();
    }

    @Test
    void create_shouldReturn201_whenValidRequest() {
        // Arrange
        CreateStateMachineRequest request = new CreateStateMachineRequest();
        request.setName("TestStateMachine");
        request.setDescription("Test Description");
        request.setEntityTypeId(UUID.randomUUID());
        
        when(stateMachineService.create(any(CreateStateMachineRequest.class))).thenReturn(testResponse);

        // Act
        ResponseEntity<StateMachineResponse> response = stateMachineController.create(request);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testId, response.getBody().getId());
        assertEquals("TestStateMachine", response.getBody().getName());
    }

    @Test
    void findAll_shouldReturn200_withPagedResults() {
        // Arrange
        PageRequest pageable = PageRequest.of(0, 10);
        Page<StateMachineResponse> page = new PageImpl<>(List.of(testResponse));
        when(stateMachineService.findAll(any(PageRequest.class))).thenReturn(page);

        // Act
        ResponseEntity<Page<StateMachineResponse>> response = stateMachineController.findAll(pageable);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    void findById_shouldReturn200_whenExists() {
        // Arrange
        when(stateMachineService.findById(testId)).thenReturn(testResponse);

        // Act
        ResponseEntity<StateMachineResponse> response = stateMachineController.findById(testId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testId, response.getBody().getId());
    }

    @Test
    void findById_shouldThrowException_whenNotFound() {
        // Arrange
        UUID notFoundId = UUID.randomUUID();
        when(stateMachineService.findById(notFoundId))
            .thenThrow(new EntityNotFoundException("State machine not found: " + notFoundId));

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> stateMachineController.findById(notFoundId));
    }

    @Test
    void update_shouldReturn200_whenValidRequest() {
        // Arrange
        UpdateStateMachineRequest request = new UpdateStateMachineRequest();
        request.setName(Optional.of("UpdatedStateMachine"));
        request.setDescription(Optional.of("Updated Description"));

        StateMachineResponse updatedResponse = StateMachineResponse.builder()
            .id(testId)
            .name("UpdatedStateMachine")
            .description("Updated Description")
            .build();

        when(stateMachineService.update(any(UUID.class), any(UpdateStateMachineRequest.class)))
            .thenReturn(updatedResponse);

        // Act
        ResponseEntity<StateMachineResponse> response = stateMachineController.update(testId, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("UpdatedStateMachine", response.getBody().getName());
    }

    @Test
    void update_shouldThrowException_whenNotFound() {
        // Arrange
        UUID notFoundId = UUID.randomUUID();
        UpdateStateMachineRequest request = new UpdateStateMachineRequest();
        request.setName(Optional.of("UpdatedName"));

        when(stateMachineService.update(any(UUID.class), any(UpdateStateMachineRequest.class)))
            .thenThrow(new EntityNotFoundException("State machine not found: " + notFoundId));

        // Act & Assert
        assertThrows(EntityNotFoundException.class, 
            () -> stateMachineController.update(notFoundId, request));
    }

    @Test
    void delete_shouldReturn204_whenExists() {
        // Arrange
        doNothing().when(stateMachineService).delete(testId);

        // Act
        ResponseEntity<Void> response = stateMachineController.delete(testId);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void delete_shouldThrowException_whenNotFound() {
        // Arrange
        UUID notFoundId = UUID.randomUUID();
        doThrow(new EntityNotFoundException("State machine not found: " + notFoundId))
            .when(stateMachineService).delete(notFoundId);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> stateMachineController.delete(notFoundId));
    }
}
