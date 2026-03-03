package com.bedomain.service;

import com.bedomain.domain.dto.entityinstance.EntityInstanceResponse;
import com.bedomain.domain.entity.*;
import com.bedomain.exception.EntityNotFoundException;
import com.bedomain.exception.InvalidTransitionException;
import com.bedomain.repository.EntityInstanceRepository;
import com.bedomain.repository.StateHistoryRepository;
import com.bedomain.repository.StateMachineRepository;
import com.bedomain.security.JwtAuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StateTransitionService {

    private final EntityInstanceRepository entityInstanceRepository;
    private final StateMachineRepository stateMachineRepository;
    private final StateHistoryService stateHistoryService;
    private final JwtAuthenticationService jwtAuthenticationService;

    @Transactional
    public EntityInstanceResponse triggerTransition(UUID entityId, String event) {
        // 1. Lookup EntityInstance by ID
        EntityInstance entity = entityInstanceRepository.findByIdAndDeletedFalse(entityId)
                .orElseThrow(() -> new EntityNotFoundException("Entity instance not found: " + entityId));

        // 2. Get current state
        String currentState = entity.getCurrentState();
        
        // 3. Get StateMachine config for the entity's EntityType
        String entityTypeName = entity.getEntityType().getName();
        StateMachine stateMachine = stateMachineRepository.findByEntityTypeAndDeletedFalse(
                        entity.getEntityType())
                .orElseThrow(() -> new InvalidTransitionException(
                        "No state machine configured for entity type: " + entityTypeName));

        // 4. Find valid transition
        TransitionSpec validTransition = findValidTransition(stateMachine, currentState, event);

        if (validTransition == null) {
            throw new InvalidTransitionException(
                    String.format("Invalid transition: cannot apply event '%s' from state '%s' for entity type '%s'",
                            event, currentState != null ? currentState : "null", entity.getEntityType().getName()));
        }

        // 5. Get the new state
        String newState = validTransition.getToState().getName();
        String fromState = currentState != null ? currentState : "null";

        // 6. Update entity.currentState
        entity.setCurrentState(newState);
        entity.setUpdatedBy(jwtAuthenticationService.getRequiredUserId());
        entity = entityInstanceRepository.save(entity);

        // 7. Record history
        stateHistoryService.record(entity, fromState, newState, event);

        // 8. Return updated entity
        return toResponse(entity);
    }

    @Transactional(readOnly = true)
    public String getCurrentState(UUID entityId) {
        EntityInstance entity = entityInstanceRepository.findByIdAndDeletedFalse(entityId)
                .orElseThrow(() -> new EntityNotFoundException("Entity instance not found: " + entityId));
        return entity.getCurrentState();
    }

    private TransitionSpec findValidTransition(StateMachine stateMachine, String currentState, String event) {
        List<TransitionSpec> transitions = stateMachine.getTransitions();
        
        return transitions.stream()
                .filter(t -> t.getEvent().equals(event))
                .filter(t -> {
                    String fromStateName = t.getFromState().getName();
                    return (currentState == null && fromStateName.equals("null")) ||
                           (currentState != null && currentState.equals(fromStateName));
                })
                .findFirst()
                .orElse(null);
    }

    private EntityInstanceResponse toResponse(EntityInstance entityInstance) {
        return EntityInstanceResponse.builder()
                .id(entityInstance.getId())
                .entityTypeId(entityInstance.getEntityType().getId())
                .entityTypeName(entityInstance.getEntityType().getName())
                .attributes(entityInstance.getAttributes())
                .currentState(entityInstance.getCurrentState())
                .createdAt(entityInstance.getCreatedAt())
                .createdBy(entityInstance.getCreatedBy())
                .updatedAt(entityInstance.getUpdatedAt())
                .updatedBy(entityInstance.getUpdatedBy())
                .build();
    }
}
