package com.bedomain.service;

import com.bedomain.domain.dto.entityinstance.EntityInstanceResponse;
import com.bedomain.domain.dto.statemachine.StateHistoryResponse;
import com.bedomain.domain.entity.EntityInstance;
import com.bedomain.domain.entity.EntityType;
import com.bedomain.domain.entity.StateHistory;
import com.bedomain.repository.EntityInstanceRepository;
import com.bedomain.repository.StateHistoryRepository;
import com.bedomain.security.JwtAuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StateHistoryServiceTest {

    @Mock
    private StateHistoryRepository stateHistoryRepository;

    @Mock
    private EntityInstanceRepository entityInstanceRepository;

    @Mock
    private JwtAuthenticationService jwtAuthenticationService;

    @InjectMocks
    private StateHistoryService stateHistoryService;

    private UUID entityId;
    private UUID entityTypeId;
    private EntityType testEntityType;
    private EntityInstance testEntity;

    @BeforeEach
    void setUp() {
        entityId = UUID.randomUUID();
        entityTypeId = UUID.randomUUID();

        testEntityType = EntityType.builder()
                .id(entityTypeId)
                .name("TestEntity")
                .build();

        testEntity = EntityInstance.builder()
                .id(entityId)
                .entityType(testEntityType)
                .attributes(new HashMap<>())
                .build();
    }

    @Test
    void record_CreatesHistory() {
        when(stateHistoryRepository.save(any(StateHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StateHistory result = stateHistoryService.record(testEntity, "DRAFT", "ACTIVE", "ACTIVATE");

        assertNotNull(result);
        assertEquals(testEntity, result.getEntityInstance());
        assertEquals("DRAFT", result.getFromState());
        assertEquals("ACTIVE", result.getToState());
        assertEquals("ACTIVATE", result.getEvent());
    }

    @Test
    void getHistoryForEntity_ReturnsOrderedList() {
        StateHistory history1 = StateHistory.builder()
                .id(UUID.randomUUID())
                .entityInstance(testEntity)
                .fromState("DRAFT")
                .toState("ACTIVE")
                .event("ACTIVATE")
                .triggeredBy("user1")
                .timestamp(Instant.now().minusSeconds(100))
                .build();

        StateHistory history2 = StateHistory.builder()
                .id(UUID.randomUUID())
                .entityInstance(testEntity)
                .fromState("ACTIVE")
                .toState("ARCHIVED")
                .event("ARCHIVE")
                .triggeredBy("user2")
                .timestamp(Instant.now())
                .build();

        when(stateHistoryRepository.findByEntityInstanceIdOrderByTimestampDesc(entityId))
                .thenReturn(List.of(history2, history1));

        List<StateHistoryResponse> result = stateHistoryService.getHistoryForEntity(entityId);

        assertEquals(2, result.size());
        assertEquals("ARCHIVED", result.get(0).getToState());
        assertEquals("ACTIVE", result.get(1).getToState());
    }

    @Test
    void getHistoryForEntity_EmptyList() {
        when(stateHistoryRepository.findByEntityInstanceIdOrderByTimestampDesc(entityId))
                .thenReturn(Collections.emptyList());

        List<StateHistoryResponse> result = stateHistoryService.getHistoryForEntity(entityId);

        assertTrue(result.isEmpty());
    }

    @Test
    void getCurrentState_FromHistory() {
        StateHistory latestHistory = StateHistory.builder()
                .id(UUID.randomUUID())
                .entityInstance(testEntity)
                .fromState("DRAFT")
                .toState("ACTIVE")
                .event("ACTIVATE")
                .build();

        when(stateHistoryRepository.findFirstByEntityInstanceIdOrderByTimestampDesc(entityId))
                .thenReturn(Optional.of(latestHistory));

        String result = stateHistoryService.getCurrentState(entityId);

        assertEquals("ACTIVE", result);
    }

    @Test
    void getCurrentState_FromEntity() {
        testEntity.setCurrentState("DRAFT");

        when(stateHistoryRepository.findFirstByEntityInstanceIdOrderByTimestampDesc(entityId))
                .thenReturn(Optional.empty());
        when(entityInstanceRepository.findByIdAndDeletedFalse(entityId))
                .thenReturn(Optional.of(testEntity));

        String result = stateHistoryService.getCurrentState(entityId);

        assertEquals("DRAFT", result);
    }

    @Test
    void getCurrentState_NoHistoryOrEntity() {
        when(stateHistoryRepository.findFirstByEntityInstanceIdOrderByTimestampDesc(entityId))
                .thenReturn(Optional.empty());
        when(entityInstanceRepository.findByIdAndDeletedFalse(entityId))
                .thenReturn(Optional.of(testEntity));

        String result = stateHistoryService.getCurrentState(entityId);

        assertNull(result);
    }
}
