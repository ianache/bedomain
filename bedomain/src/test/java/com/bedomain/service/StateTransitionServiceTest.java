package com.bedomain.service;

import com.bedomain.domain.dto.entityinstance.EntityInstanceResponse;
import com.bedomain.domain.entity.*;
import com.bedomain.exception.EntityNotFoundException;
import com.bedomain.exception.InvalidTransitionException;
import com.bedomain.repository.EntityInstanceRepository;
import com.bedomain.repository.StateMachineRepository;
import com.bedomain.security.JwtAuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StateTransitionServiceTest {

    @Mock
    private EntityInstanceRepository entityInstanceRepository;

    @Mock
    private StateMachineRepository stateMachineRepository;

    @Mock
    private StateHistoryService stateHistoryService;

    @Mock
    private JwtAuthenticationService jwtAuthenticationService;

    @InjectMocks
    private StateTransitionService stateTransitionService;

    private UUID entityId;
    private UUID entityTypeId;
    private EntityType testEntityType;
    private EntityInstance testEntity;
    private StateMachine testStateMachine;
    private StateSpec fromState;
    private StateSpec toState;
    private TransitionSpec validTransition;

    @BeforeEach
    void setUp() {
        entityId = UUID.randomUUID();
        entityTypeId = UUID.randomUUID();

        testEntityType = EntityType.builder()
                .id(entityTypeId)
                .name("TestEntity")
                .build();

        fromState = StateSpec.builder()
                .id(UUID.randomUUID())
                .name("DRAFT")
                .type(StateSpec.StateType.INITIAL)
                .build();

        toState = StateSpec.builder()
                .id(UUID.randomUUID())
                .name("ACTIVE")
                .type(StateSpec.StateType.INTERMEDIATE)
                .build();

        validTransition = TransitionSpec.builder()
                .id(UUID.randomUUID())
                .event("ACTIVATE")
                .fromState(fromState)
                .toState(toState)
                .build();

        testStateMachine = StateMachine.builder()
                .id(UUID.randomUUID())
                .name("TestStateMachine")
                .entityType(testEntityType)
                .states(List.of(fromState, toState))
                .transitions(List.of(validTransition))
                .build();

        testEntity = EntityInstance.builder()
                .id(entityId)
                .entityType(testEntityType)
                .currentState("DRAFT")
                .attributes(new HashMap<>())
                .build();
    }

    @Test
    void triggerTransition_Success() {
        when(entityInstanceRepository.findByIdAndDeletedFalse(entityId)).thenReturn(Optional.of(testEntity));
        when(stateMachineRepository.findByEntityTypeAndDeletedFalse(testEntityType)).thenReturn(Optional.of(testStateMachine));
        when(entityInstanceRepository.save(any(EntityInstance.class))).thenReturn(testEntity);

        EntityInstanceResponse response = stateTransitionService.triggerTransition(entityId, "ACTIVATE");

        assertNotNull(response);
        assertEquals("ACTIVE", response.getCurrentState());
        verify(stateHistoryService).record(eq(testEntity), eq("DRAFT"), eq("ACTIVE"), eq("ACTIVATE"));
    }

    @Test
    void triggerTransition_EntityNotFound_ThrowsException() {
        when(entityInstanceRepository.findByIdAndDeletedFalse(entityId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> 
            stateTransitionService.triggerTransition(entityId, "ACTIVATE"));
    }

    @Test
    void triggerTransition_InvalidTransition_ThrowsException() {
        when(entityInstanceRepository.findByIdAndDeletedFalse(entityId)).thenReturn(Optional.of(testEntity));
        when(stateMachineRepository.findByEntityTypeAndDeletedFalse(testEntityType)).thenReturn(Optional.of(testStateMachine));

        assertThrows(InvalidTransitionException.class, () -> 
            stateTransitionService.triggerTransition(entityId, "INVALID_EVENT"));
    }

    @Test
    void triggerTransition_NoStateMachine_ThrowsException() {
        when(entityInstanceRepository.findByIdAndDeletedFalse(entityId)).thenReturn(Optional.of(testEntity));
        when(stateMachineRepository.findByEntityTypeAndDeletedFalse(testEntityType)).thenReturn(Optional.empty());

        assertThrows(InvalidTransitionException.class, () -> 
            stateTransitionService.triggerTransition(entityId, "ACTIVATE"));
    }

    @Test
    void triggerTransition_FirstTransition_NullState() {
        testEntity.setCurrentState(null);
        
        // Create initial transition
        StateSpec initialState = StateSpec.builder()
                .id(UUID.randomUUID())
                .name("null")
                .type(StateSpec.StateType.INITIAL)
                .build();
        
        TransitionSpec initialTransition = TransitionSpec.builder()
                .id(UUID.randomUUID())
                .event("INITIATE")
                .fromState(initialState)
                .toState(toState)
                .build();
        
        testStateMachine.setTransitions(List.of(initialTransition, validTransition));

        when(entityInstanceRepository.findByIdAndDeletedFalse(entityId)).thenReturn(Optional.of(testEntity));
        when(stateMachineRepository.findByEntityTypeAndDeletedFalse(testEntityType)).thenReturn(Optional.of(testStateMachine));
        when(entityInstanceRepository.save(any(EntityInstance.class))).thenReturn(testEntity);

        EntityInstanceResponse response = stateTransitionService.triggerTransition(entityId, "INITIATE");

        assertNotNull(response);
    }

    @Test
    void getCurrentState_ReturnsCurrentState() {
        when(entityInstanceRepository.findByIdAndDeletedFalse(entityId)).thenReturn(Optional.of(testEntity));

        String currentState = stateTransitionService.getCurrentState(entityId);

        assertEquals("DRAFT", currentState);
    }

    @Test
    void getCurrentState_EntityNotFound_ThrowsException() {
        when(entityInstanceRepository.findByIdAndDeletedFalse(entityId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> 
            stateTransitionService.getCurrentState(entityId));
    }
}
