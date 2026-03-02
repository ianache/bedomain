package com.bedomain.service;

import com.bedomain.domain.dto.entityinstance.CreateEntityInstanceRequest;
import com.bedomain.domain.dto.entityinstance.EntityInstanceResponse;
import com.bedomain.domain.dto.entityinstance.UpdateEntityInstanceRequest;
import com.bedomain.domain.entity.EntityInstance;
import com.bedomain.domain.entity.EntityType;
import com.bedomain.exception.EntityNotFoundException;
import com.bedomain.repository.EntityInstanceRepository;
import com.bedomain.repository.EntityTypeRepository;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EntityInstanceServiceTest {

    @Mock
    private EntityInstanceRepository entityInstanceRepository;

    @Mock
    private EntityTypeRepository entityTypeRepository;

    @Mock
    private JwtAuthenticationService jwtAuthenticationService;

    @InjectMocks
    private EntityInstanceService entityInstanceService;

    private UUID entityTypeId;
    private UUID instanceId;
    private EntityType testEntityType;
    private EntityInstance testInstance;

    @BeforeEach
    void setUp() {
        entityTypeId = UUID.randomUUID();
        instanceId = UUID.randomUUID();

        testEntityType = EntityType.builder()
            .id(entityTypeId)
            .name("TestEntity")
            .description("Test Description")
            .deleted(false)
            .createdAt(Instant.now())
            .createdBy("test-user")
            .build();

        Map<String, Object> attributes = Map.of("field1", "value1");

        testInstance = EntityInstance.builder()
            .id(instanceId)
            .entityType(testEntityType)
            .attributes(attributes)
            .deleted(false)
            .createdAt(Instant.now())
            .createdBy("test-user")
            .updatedAt(Instant.now())
            .updatedBy("test-user")
            .build();
    }

    @Test
    void create_shouldThrowException_whenEntityTypeNotFound() {
        // Arrange
        UUID notFoundId = UUID.randomUUID();
        CreateEntityInstanceRequest request = new CreateEntityInstanceRequest();
        request.setEntityTypeId(notFoundId);
        request.setAttributes(Map.of("field1", "value1"));
        
        when(entityTypeRepository.findById(notFoundId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> entityInstanceService.create(request)
        );
        assertTrue(exception.getMessage().contains("Entity type not found"));
    }

    @Test
    void create_shouldReturnResponse_whenValidRequest() {
        // Arrange
        CreateEntityInstanceRequest request = new CreateEntityInstanceRequest();
        request.setEntityTypeId(entityTypeId);
        request.setAttributes(Map.of("field1", "value1"));

        when(entityTypeRepository.findById(entityTypeId)).thenReturn(Optional.of(testEntityType));
        when(entityInstanceRepository.save(any(EntityInstance.class))).thenAnswer(invocation -> {
            EntityInstance e = invocation.getArgument(0);
            e.setId(instanceId);
            return e;
        });
        when(jwtAuthenticationService.getRequiredUserId()).thenReturn("test-user");

        // Act
        EntityInstanceResponse response = entityInstanceService.create(request);

        // Assert
        assertNotNull(response);
        assertEquals(entityTypeId, response.getEntityTypeId());
    }

    @Test
    void findAll_shouldFilterByEntityTypeId() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<EntityInstance> page = new PageImpl<>(List.of(testInstance));
        
        when(entityInstanceRepository.findByEntityTypeIdAndDeletedFalse(entityTypeId, pageable)).thenReturn(page);

        // Act
        Page<EntityInstanceResponse> result = entityInstanceService.findAll(entityTypeId, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(entityInstanceRepository).findByEntityTypeIdAndDeletedFalse(entityTypeId, pageable);
    }

    @Test
    void findAll_shouldReturnAll_whenNoFilter() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<EntityInstance> page = new PageImpl<>(List.of(testInstance));
        
        when(entityInstanceRepository.findByDeletedFalse(pageable)).thenReturn(page);

        // Act
        Page<EntityInstanceResponse> result = entityInstanceService.findAll(null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(entityInstanceRepository).findByDeletedFalse(pageable);
    }

    @Test
    void findById_shouldThrowException_whenNotFound() {
        // Arrange
        UUID notFoundId = UUID.randomUUID();
        when(entityInstanceRepository.findByIdAndDeletedFalse(notFoundId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> entityInstanceService.findById(notFoundId)
        );
        assertTrue(exception.getMessage().contains("Entity instance not found"));
    }

    @Test
    void findById_shouldReturnResponse_whenExists() {
        // Arrange
        when(entityInstanceRepository.findByIdAndDeletedFalse(instanceId)).thenReturn(Optional.of(testInstance));

        // Act
        EntityInstanceResponse response = entityInstanceService.findById(instanceId);

        // Assert
        assertNotNull(response);
        assertEquals(instanceId, response.getId());
    }

    @Test
    void update_shouldThrowException_whenNotFound() {
        // Arrange
        UUID notFoundId = UUID.randomUUID();
        UpdateEntityInstanceRequest request = new UpdateEntityInstanceRequest();
        request.setAttributes(Map.of("updatedField", "updatedValue"));
        
        when(entityInstanceRepository.findByIdAndDeletedFalse(notFoundId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> entityInstanceService.update(notFoundId, request)
        );
        assertTrue(exception.getMessage().contains("Entity instance not found"));
    }

    @Test
    void update_shouldReturnResponse_whenValidRequest() {
        // Arrange
        UpdateEntityInstanceRequest request = new UpdateEntityInstanceRequest();
        request.setAttributes(Map.of("updatedField", "updatedValue"));

        when(entityInstanceRepository.findByIdAndDeletedFalse(instanceId)).thenReturn(Optional.of(testInstance));
        when(entityInstanceRepository.save(any(EntityInstance.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtAuthenticationService.getRequiredUserId()).thenReturn("test-user");

        // Act
        EntityInstanceResponse response = entityInstanceService.update(instanceId, request);

        // Assert
        assertNotNull(response);
    }

    @Test
    void delete_shouldSoftDelete_whenExists() {
        // Arrange
        when(entityInstanceRepository.findByIdAndDeletedFalse(instanceId)).thenReturn(Optional.of(testInstance));
        when(jwtAuthenticationService.getRequiredUserId()).thenReturn("test-user");
        
        EntityInstance deletedInstance = EntityInstance.builder()
            .id(instanceId)
            .entityType(testEntityType)
            .deleted(true)
            .updatedBy("test-user")
            .build();
        when(entityInstanceRepository.save(any(EntityInstance.class))).thenReturn(deletedInstance);

        // Act
        entityInstanceService.delete(instanceId);

        // Assert
        verify(entityInstanceRepository).save(argThat(entity -> entity.isDeleted()));
    }
}
