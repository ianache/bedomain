package com.bedomain.service;

import com.bedomain.domain.dto.statemachine.*;
import com.bedomain.domain.entity.*;
import com.bedomain.exception.EntityNotFoundException;
import com.bedomain.repository.*;
import com.bedomain.security.JwtAuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StateMachineService {

    private final StateMachineRepository stateMachineRepository;
    private final StateSpecRepository stateSpecRepository;
    private final TransitionSpecRepository transitionSpecRepository;
    private final EntityTypeRepository entityTypeRepository;
    private final JwtAuthenticationService jwtAuthenticationService;

    @Transactional
    @CacheEvict(value = "stateMachines", allEntries = true)
    public StateMachineResponse create(CreateStateMachineRequest request) {
        EntityType entityType = entityTypeRepository.findById(request.getEntityTypeId())
            .orElseThrow(() -> new EntityNotFoundException("Entity type not found: " + request.getEntityTypeId()));

        String userId = jwtAuthenticationService.getRequiredUserId();

        StateMachine stateMachine = StateMachine.builder()
            .name(request.getName())
            .description(request.getDescription())
            .entityType(entityType)
            .createdBy(userId)
            .build();

        // Build state specs
        if (request.getStates() != null) {
            for (CreateStateSpecRequest stateReq : request.getStates()) {
                StateSpec stateSpec = StateSpec.builder()
                    .name(stateReq.getName())
                    .description(stateReq.getDescription())
                    .type(StateSpec.StateType.valueOf(stateReq.getType().toUpperCase()))
                    .build();
                stateMachine.addState(stateSpec);
            }
        }

        // Build transition specs after states are added
        if (request.getTransitions() != null) {
            Map<String, StateSpec> stateMap = stateMachine.getStates().stream()
                .collect(Collectors.toMap(StateSpec::getName, s -> s));

            for (CreateTransitionSpecRequest transReq : request.getTransitions()) {
                StateSpec fromState = stateMap.get(transReq.getFromStateName());
                StateSpec toState = stateMap.get(transReq.getToStateName());

                if (fromState == null) {
                    throw new IllegalArgumentException("From state not found: " + transReq.getFromStateName());
                }
                if (toState == null) {
                    throw new IllegalArgumentException("To state not found: " + transReq.getToStateName());
                }

                TransitionSpec transitionSpec = TransitionSpec.builder()
                    .event(transReq.getEvent())
                    .description(transReq.getDescription())
                    .fromState(fromState)
                    .toState(toState)
                    .build();
                stateMachine.addTransition(transitionSpec);
            }
        }

        stateMachine = stateMachineRepository.save(stateMachine);
        return toResponse(stateMachine);
    }

    @Transactional(readOnly = true)
    public Page<StateMachineResponse> findAll(Pageable pageable) {
        return stateMachineRepository.findByDeletedFalse(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "stateMachines", key = "#id")
    public StateMachineResponse findById(UUID id) {
        StateMachine stateMachine = stateMachineRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("State machine not found: " + id));
        
        if (stateMachine.isDeleted()) {
            throw new EntityNotFoundException("State machine not found: " + id);
        }
        
        return toResponse(stateMachine);
    }

    @Transactional
    @CacheEvict(value = "stateMachines", allEntries = true)
    public StateMachineResponse update(UUID id, UpdateStateMachineRequest request) {
        StateMachine stateMachine = stateMachineRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("State machine not found: " + id));

        if (stateMachine.isDeleted()) {
            throw new EntityNotFoundException("State machine not found: " + id);
        }

        String userId = jwtAuthenticationService.getRequiredUserId();

        if (request.getName().isPresent()) {
            stateMachine.setName(request.getName().get());
        }

        if (request.getDescription().isPresent()) {
            stateMachine.setDescription(request.getDescription().get());
        }

        // Add new states
        if (request.getAddStates() != null) {
            for (CreateStateSpecRequest stateReq : request.getAddStates()) {
                StateSpec stateSpec = StateSpec.builder()
                    .name(stateReq.getName())
                    .description(stateReq.getDescription())
                    .type(StateSpec.StateType.valueOf(stateReq.getType().toUpperCase()))
                    .build();
                stateMachine.addState(stateSpec);
            }
        }

        // Remove states
        if (request.getRemoveStateIds() != null) {
            Set<UUID> toRemove = new HashSet<>(request.getRemoveStateIds());
            stateMachine.getStates().removeIf(s -> toRemove.contains(s.getId()));
        }

        // Add new transitions
        if (request.getAddTransitions() != null) {
            Map<String, StateSpec> stateMap = stateMachine.getStates().stream()
                .collect(Collectors.toMap(StateSpec::getName, s -> s));

            for (CreateTransitionSpecRequest transReq : request.getAddTransitions()) {
                StateSpec fromState = stateMap.get(transReq.getFromStateName());
                StateSpec toState = stateMap.get(transReq.getToStateName());

                if (fromState == null || toState == null) {
                    continue; // Skip invalid transitions
                }

                TransitionSpec transitionSpec = TransitionSpec.builder()
                    .event(transReq.getEvent())
                    .description(transReq.getDescription())
                    .fromState(fromState)
                    .toState(toState)
                    .build();
                stateMachine.addTransition(transitionSpec);
            }
        }

        // Remove transitions
        if (request.getRemoveTransitionIds() != null) {
            Set<UUID> toRemove = new HashSet<>(request.getRemoveTransitionIds());
            stateMachine.getTransitions().removeIf(t -> toRemove.contains(t.getId()));
        }

        stateMachine.setUpdatedBy(userId);
        stateMachine = stateMachineRepository.save(stateMachine);
        return toResponse(stateMachine);
    }

    @Transactional
    @CacheEvict(value = "stateMachines", allEntries = true)
    public void delete(UUID id) {
        StateMachine stateMachine = stateMachineRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("State machine not found: " + id));
        
        stateMachine.setDeleted(true);
        stateMachine.setUpdatedBy(jwtAuthenticationService.getRequiredUserId());
        stateMachineRepository.save(stateMachine);
    }

    private StateMachineResponse toResponse(StateMachine stateMachine) {
        List<StateSpecResponse> stateResponses = stateMachine.getStates().stream()
            .map(s -> StateSpecResponse.builder()
                .id(s.getId())
                .name(s.getName())
                .description(s.getDescription())
                .type(s.getType())
                .build())
            .collect(Collectors.toList());

        List<TransitionSpecResponse> transitionResponses = stateMachine.getTransitions().stream()
            .map(t -> TransitionSpecResponse.builder()
                .id(t.getId())
                .event(t.getEvent())
                .description(t.getDescription())
                .fromStateName(t.getFromState().getName())
                .toStateName(t.getToState().getName())
                .build())
            .collect(Collectors.toList());

        return StateMachineResponse.builder()
            .id(stateMachine.getId())
            .name(stateMachine.getName())
            .description(stateMachine.getDescription())
            .entityTypeId(stateMachine.getEntityType().getId())
            .entityTypeName(stateMachine.getEntityType().getName())
            .states(stateResponses)
            .transitions(transitionResponses)
            .createdAt(stateMachine.getCreatedAt())
            .createdBy(stateMachine.getCreatedBy())
            .updatedAt(stateMachine.getUpdatedAt())
            .updatedBy(stateMachine.getUpdatedBy())
            .build();
    }
}
