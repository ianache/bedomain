package com.bedomain.service;

import com.bedomain.domain.dto.statemachine.*;
import com.bedomain.domain.entity.*;
import com.bedomain.exception.EntityNotFoundException;
import com.bedomain.repository.*;
import com.bedomain.security.JwtAuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StateMachineServiceTest {

    @Mock
    private StateMachineRepository stateMachineRepository;

    @Mock
    private StateSpecRepository stateSpecRepository;

    @Mock
    private TransitionSpecRepository transitionSpecRepository;

    @Mock
    private EntityTypeRepository entityTypeRepository;

    @Mock
    private JwtAuthenticationService jwtAuthenticationService;

    @InjectMocks
    private StateMachineService stateMachineService;

    private UUID testId;
    private UUID entityTypeId;
    private EntityType testEntityType;
    private StateMachine testStateMachine;
    private CreateStateMachineRequest createRequest;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        entityTypeId = UUID.randomUUID();

        testEntityType = EntityType.builder()
            .id(entityTypeId)
            .name("TestEntityType")
            .description("Test Entity Type")
            .deleted(false)
            .build();

        testStateMachine = StateMachine.builder()
            .id(testId)
            .name("TestStateMachine")
            .description("Test State Machine")
            .entityType(testEntityType)
            .deleted(false)
            .createdAt(Instant.now())
            .createdBy("test-user")
            .updatedAt(Instant.now())
            .updatedBy("test-user")
            .build();

        createRequest = new CreateStateMachineRequest();
        createRequest.setName("TestStateMachine");
        createRequest.setDescription("Test State Machine");
        createRequest.setEntityTypeId(entityTypeId);
    }

    @Test
    void create_shouldReturnResponse_whenValidRequest() {
        // Arrange
        when(entityTypeRepository.findById(entityTypeId)).thenReturn(Optional.of(testEntityType));
        when(jwtAuthenticationService.getRequiredUserId()).thenReturn("test-user");
        when(stateMachineRepository.save(any(StateMachine.class))).thenReturn(testStateMachine);

        // Act
        StateMachineResponse response = stateMachineService.create(createRequest);

        // Assert
        assertNotNull(response);
        assertEquals("TestStateMachine", response.getName());
        verify(stateMachineRepository).save(any(StateMachine.class));
    }

    @Test
    void create_shouldThrowException_whenEntityTypeNotFound() {
        // Arrange
        UUID invalidEntityTypeId = UUID.randomUUID();
        createRequest.setEntityTypeId(invalidEntityTypeId);
        when(entityTypeRepository.findById(invalidEntityTypeId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> stateMachineService.create(createRequest)
        );
        assertTrue(exception.getMessage().contains("Entity type not found"));
    }

    @Test
    void findAll_shouldReturnPagedResults() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<StateMachine> page = new PageImpl<>(List.of(testStateMachine));
        when(stateMachineRepository.findByDeletedFalse(pageable)).thenReturn(page);

        // Act
        Page<StateMachineResponse> result = stateMachineService.findAll(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("TestStateMachine", result.getContent().get(0).getName());
    }

    @Test
    void findById_shouldThrowException_whenNotFound() {
        // Arrange
        UUID notFoundId = UUID.randomUUID();
        when(stateMachineRepository.findById(notFoundId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> stateMachineService.findById(notFoundId)
        );
        assertTrue(exception.getMessage().contains("State machine not found"));
    }

    @Test
    void findById_shouldReturnResponse_whenExists() {
        // Arrange
        when(stateMachineRepository.findById(testId)).thenReturn(Optional.of(testStateMachine));

        // Act
        StateMachineResponse response = stateMachineService.findById(testId);

        // Assert
        assertNotNull(response);
        assertEquals(testId, response.getId());
        assertEquals("TestStateMachine", response.getName());
    }

    @Test
    void findById_shouldThrowException_whenDeleted() {
        // Arrange
        testStateMachine.setDeleted(true);
        when(stateMachineRepository.findById(testId)).thenReturn(Optional.of(testStateMachine));

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> stateMachineService.findById(testId)
        );
        assertTrue(exception.getMessage().contains("State machine not found"));
    }

    @Test
    void update_shouldReturnResponse_whenValidRequest() {
        // Arrange
        UpdateStateMachineRequest updateRequest = new UpdateStateMachineRequest();
        updateRequest.setName(Optional.of("UpdatedStateMachine"));
        updateRequest.setDescription(Optional.of("Updated Description"));

        when(stateMachineRepository.findById(testId)).thenReturn(Optional.of(testStateMachine));
        when(jwtAuthenticationService.getRequiredUserId()).thenReturn("test-user");
        when(stateMachineRepository.save(any(StateMachine.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        StateMachineResponse response = stateMachineService.update(testId, updateRequest);

        // Assert
        assertNotNull(response);
        assertEquals("UpdatedStateMachine", response.getName());
    }

    @Test
    void update_shouldThrowException_whenNotFound() {
        // Arrange
        UUID notFoundId = UUID.randomUUID();
        UpdateStateMachineRequest updateRequest = new UpdateStateMachineRequest();
        updateRequest.setName(Optional.of("UpdatedName"));
        
        when(stateMachineRepository.findById(notFoundId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> stateMachineService.update(notFoundId, updateRequest)
        );
        assertTrue(exception.getMessage().contains("State machine not found"));
    }

    @Test
    void delete_shouldSoftDelete_whenExists() {
        // Arrange
        when(stateMachineRepository.findById(testId)).thenReturn(Optional.of(testStateMachine));
        when(jwtAuthenticationService.getRequiredUserId()).thenReturn("test-user");

        StateMachine deletedStateMachine = StateMachine.builder()
            .id(testId)
            .name("TestStateMachine")
            .deleted(true)
            .updatedBy("test-user")
            .build();
        when(stateMachineRepository.save(any(StateMachine.class))).thenReturn(deletedStateMachine);

        // Act
        stateMachineService.delete(testId);

        // Assert
        verify(stateMachineRepository).save(argThat(entity -> entity.isDeleted()));
    }

    @Test
    void delete_shouldThrowException_whenNotFound() {
        // Arrange
        UUID notFoundId = UUID.randomUUID();
        when(stateMachineRepository.findById(notFoundId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> stateMachineService.delete(notFoundId)
        );
        assertTrue(exception.getMessage().contains("State machine not found"));
    }
}
