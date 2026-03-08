package com.bedomain.service;

import com.bedomain.domain.dto.entityinstance.EntityInstanceResponse;
import com.bedomain.domain.entity.*;
import com.bedomain.event.EventPublisher;
import com.bedomain.exception.EntityNotFoundException;
import com.bedomain.exception.InvalidTransitionException;
import com.bedomain.repository.EntityInstanceRepository;
import com.bedomain.repository.StateHistoryRepository;
import com.bedomain.repository.StateMachineRepository;
import com.bedomain.security.JwtAuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StateTransitionService {

    private final EntityInstanceRepository entityInstanceRepository;
    private final StateMachineRepository stateMachineRepository;
    private final StateHistoryService stateHistoryService;
    private final JwtAuthenticationService jwtAuthenticationService;
    private final EventPublisher eventPublisher;
    private final JavaScriptExecutor javascriptExecutor;

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

        // 6. Get state specs for hook execution
        StateSpec fromStateSpec = stateMachine.getStates().stream()
                .filter(s -> s.getName().equals(fromState))
                .findFirst()
                .orElse(null);
        
        StateSpec toStateSpec = stateMachine.getStates().stream()
                .filter(s -> s.getName().equals(newState))
                .findFirst()
                .orElse(null);

        // 7. Execute onExit script (from old state)
        boolean hookExecuted = false;
        String hookType = null;
        String hookScriptHash = null;
        String hookError = null;
        
        if (fromStateSpec != null && fromStateSpec.getOnExitScript() != null 
                && !fromStateSpec.getOnExitScript().isBlank()) {
            try {
                Map<String, Object> updatedAttributes = javascriptExecutor.execute(
                        fromStateSpec.getOnExitScript(),
                        entity.getAttributes(),
                        entity.getId(),
                        "onExit"
                );
                entity.setAttributes(updatedAttributes);
                hookExecuted = true;
                hookType = "onExit";
                hookScriptHash = computeScriptHash(fromStateSpec.getOnExitScript());
            } catch (JavaScriptExecutor.ScriptExecutionException e) {
                hookError = e.getMessage();
                hookExecuted = true;
                hookType = "onExit";
                hookScriptHash = computeScriptHash(fromStateSpec.getOnExitScript());
                // If failOnError is true, the exception will propagate
                throw;
            }
        }

        // 8. Execute onEnter script (from new state)
        if (toStateSpec != null && toStateSpec.getOnEnterScript() != null 
                && !toStateSpec.getOnEnterScript().isBlank()) {
            try {
                Map<String, Object> updatedAttributes = javascriptExecutor.execute(
                        toStateSpec.getOnEnterScript(),
                        entity.getAttributes(),
                        entity.getId(),
                        "onEnter"
                );
                entity.setAttributes(updatedAttributes);
                hookExecuted = true;
                hookType = "onEnter";
                hookScriptHash = computeScriptHash(toStateSpec.getOnEnterScript());
            } catch (JavaScriptExecutor.ScriptExecutionException e) {
                hookError = e.getMessage();
                hookExecuted = true;
                hookType = "onEnter";
                hookScriptHash = computeScriptHash(toStateSpec.getOnEnterScript());
                // If failOnError is true, the exception will propagate
                throw;
            }
        }

        // 9. Update entity.currentState
        entity.setCurrentState(newState);
        entity.setUpdatedBy(jwtAuthenticationService.getRequiredUserId());
        entity = entityInstanceRepository.save(entity);

        // 10. Record history with hook info
        stateHistoryService.record(entity, fromState, newState, event, 
                hookExecuted, hookType, hookScriptHash, hookError);

        // 11. Publish state changed event
        String userId = jwtAuthenticationService.getRequiredUserId();
        eventPublisher.publishStateChanged(entity, fromState, newState, userId);

        // 12. Return updated entity
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

    /**
     * Compute SHA-256 hash of a script for audit purposes.
     */
    private String computeScriptHash(String script) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(script.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return "SHA-256-unavailable";
        }
    }
}
