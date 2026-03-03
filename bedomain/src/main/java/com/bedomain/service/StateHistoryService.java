package com.bedomain.service;

import com.bedomain.domain.dto.entityinstance.EntityInstanceResponse;
import com.bedomain.domain.dto.statemachine.StateHistoryResponse;
import com.bedomain.domain.entity.EntityInstance;
import com.bedomain.domain.entity.StateHistory;
import com.bedomain.repository.EntityInstanceRepository;
import com.bedomain.repository.StateHistoryRepository;
import com.bedomain.security.JwtAuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StateHistoryService {

    private final StateHistoryRepository stateHistoryRepository;
    private final EntityInstanceRepository entityInstanceRepository;
    private final JwtAuthenticationService jwtAuthenticationService;

    @Transactional
    public StateHistory record(EntityInstance entityInstance, String fromState, String toState, String event) {
        StateHistory history = StateHistory.builder()
                .entityInstance(entityInstance)
                .fromState(fromState)
                .toState(toState)
                .event(event)
                .triggeredBy(jwtAuthenticationService.getRequiredUserId())
                .build();

        return stateHistoryRepository.save(history);
    }

    @Transactional(readOnly = true)
    public List<StateHistoryResponse> getHistoryForEntity(UUID entityId) {
        List<StateHistory> historyList = stateHistoryRepository.findByEntityInstanceIdOrderByTimestampDesc(entityId);
        
        return historyList.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public String getCurrentState(UUID entityId) {
        return stateHistoryRepository.findFirstByEntityInstanceIdOrderByTimestampDesc(entityId)
                .map(StateHistory::getToState)
                .orElseGet(() -> entityInstanceRepository.findByIdAndDeletedFalse(entityId)
                        .map(EntityInstance::getCurrentState)
                        .orElse(null));
    }

    private StateHistoryResponse toResponse(StateHistory history) {
        EntityInstance entityInstance = history.getEntityInstance();
        
        EntityInstanceResponse snapshot = EntityInstanceResponse.builder()
                .id(entityInstance.getId())
                .entityTypeId(entityInstance.getEntityType().getId())
                .entityTypeName(entityInstance.getEntityType().getName())
                .attributes(entityInstance.getAttributes())
                .createdAt(entityInstance.getCreatedAt())
                .createdBy(entityInstance.getCreatedBy())
                .updatedAt(entityInstance.getUpdatedAt())
                .updatedBy(entityInstance.getUpdatedBy())
                .build();

        return StateHistoryResponse.builder()
                .id(history.getId())
                .fromState(history.getFromState())
                .toState(history.getToState())
                .event(history.getEvent())
                .triggeredBy(history.getTriggeredBy())
                .timestamp(history.getTimestamp())
                .currentSnapshot(snapshot)
                .build();
    }
}
