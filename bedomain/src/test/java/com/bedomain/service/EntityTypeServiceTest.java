package com.bedomain.service;

import com.bedomain.domain.dto.entitytype.CreateEntityTypeRequest;
import com.bedomain.domain.dto.entitytype.EntityTypeResponse;
import com.bedomain.domain.dto.entitytype.UpdateEntityTypeRequest;
import com.bedomain.domain.entity.EntityType;
import com.bedomain.exception.EntityNotFoundException;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EntityTypeServiceTest {

    @Mock
    private EntityTypeRepository entityTypeRepository;

    @Mock
    private JwtAuthenticationService jwtAuthenticationService;

    @InjectMocks
    private EntityTypeService entityTypeService;

    private UUID testId;
    private EntityType testEntityType;
    private CreateEntityTypeRequest createRequest;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        
        testEntityType = EntityType.builder()
            .id(testId)
            .name("TestEntity")
            .description("Test Description")
            .deleted(false)
            .createdAt(Instant.now())
            .createdBy("test-user")
            .updatedAt(Instant.now())
            .updatedBy("test-user")
            .build();

        createRequest = new CreateEntityTypeRequest();
        createRequest.setName("TestEntity");
        createRequest.setDescription("Test Description");
    }

    @Test
    void create_shouldReturnResponse_whenValidRequest() {
        // Arrange
        when(entityTypeRepository.existsByNameAndDeletedFalse("TestEntity")).thenReturn(false);
        when(entityTypeRepository.save(any(EntityType.class))).thenReturn(testEntityType);
        when(jwtAuthenticationService.getRequiredUserId()).thenReturn("test-user");

        // Act
        EntityTypeResponse response = entityTypeService.create(createRequest);

        // Assert
        assertNotNull(response);
        assertEquals("TestEntity", response.getName());
        verify(entityTypeRepository).save(any(EntityType.class));
    }

    @Test
    void create_shouldThrowException_whenNameExists() {
        // Arrange
        when(entityTypeRepository.existsByNameAndDeletedFalse("TestEntity")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> entityTypeService.create(createRequest)
        );
        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    void findAll_shouldReturnPagedResults() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<EntityType> page = new PageImpl<>(List.of(testEntityType));
        when(entityTypeRepository.findByDeletedFalse(pageable)).thenReturn(page);

        // Act
        Page<EntityTypeResponse> result = entityTypeService.findAll(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("TestEntity", result.getContent().get(0).getName());
    }

    @Test
    void findById_shouldThrowException_whenNotFound() {
        // Arrange
        UUID notFoundId = UUID.randomUUID();
        when(entityTypeRepository.findById(notFoundId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> entityTypeService.findById(notFoundId)
        );
        assertTrue(exception.getMessage().contains("Entity type not found"));
    }

    @Test
    void findById_shouldReturnResponse_whenExists() {
        // Arrange
        when(entityTypeRepository.findById(testId)).thenReturn(Optional.of(testEntityType));

        // Act
        EntityTypeResponse response = entityTypeService.findById(testId);

        // Assert
        assertNotNull(response);
        assertEquals(testId, response.getId());
        assertEquals("TestEntity", response.getName());
    }

    @Test
    void update_shouldThrowException_whenNameExists() {
        // Arrange
        UpdateEntityTypeRequest updateRequest = new UpdateEntityTypeRequest();
        updateRequest.setName(Optional.of("ExistingName"));
        
        when(entityTypeRepository.findById(testId)).thenReturn(Optional.of(testEntityType));
        when(entityTypeRepository.existsByNameAndDeletedFalse("ExistingName")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> entityTypeService.update(testId, updateRequest)
        );
        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    void update_shouldReturnResponse_whenValidRequest() {
        // Arrange
        UpdateEntityTypeRequest updateRequest = new UpdateEntityTypeRequest();
        updateRequest.setName(Optional.of("UpdatedName"));
        updateRequest.setDescription(Optional.of("Updated Description"));

        when(entityTypeRepository.findById(testId)).thenReturn(Optional.of(testEntityType));
        when(entityTypeRepository.existsByNameAndDeletedFalse("UpdatedName")).thenReturn(false);
        when(entityTypeRepository.save(any(EntityType.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtAuthenticationService.getRequiredUserId()).thenReturn("test-user");

        // Act
        EntityTypeResponse response = entityTypeService.update(testId, updateRequest);

        // Assert
        assertNotNull(response);
        assertEquals("UpdatedName", response.getName());
    }

    @Test
    void delete_shouldSoftDelete_whenExists() {
        // Arrange
        when(entityTypeRepository.findById(testId)).thenReturn(Optional.of(testEntityType));
        when(jwtAuthenticationService.getRequiredUserId()).thenReturn("test-user");
        
        EntityType savedEntityType = EntityType.builder()
            .id(testId)
            .name("TestEntity")
            .deleted(true)
            .updatedBy("test-user")
            .build();
        when(entityTypeRepository.save(any(EntityType.class))).thenReturn(savedEntityType);

        // Act
        entityTypeService.delete(testId);

        // Assert
        verify(entityTypeRepository).save(argThat(entity -> entity.isDeleted()));
    }

    @Test
    void existsById_shouldReturnTrue_whenExists() {
        // Arrange
        when(entityTypeRepository.existsById(testId)).thenReturn(true);

        // Act
        boolean result = entityTypeService.existsById(testId);

        // Assert
        assertTrue(result);
    }

    @Test
    void existsById_shouldReturnFalse_whenNotExists() {
        // Arrange
        UUID notFoundId = UUID.randomUUID();
        when(entityTypeRepository.existsById(notFoundId)).thenReturn(false);

        // Act
        boolean result = entityTypeService.existsById(notFoundId);

        // Assert
        assertFalse(result);
    }

    @Test
    void create_unitTypeWithAttributes_shouldCreateSuccessfully() {
        // Arrange: Unit Type with possible values: vehiculo, moto, bicicleta, scotter, persona, paquete
        // Attributes: id (UUID), code (external code), date (creation date)
        
        CreateEntityTypeRequest unitTypeRequest = new CreateEntityTypeRequest();
        unitTypeRequest.setName("Unit Type");
        unitTypeRequest.setDescription("Unidad que puede ser: vehiculo, moto, bicicleta, scotter, persona, paquete");

        EntityType unitTypeEntity = EntityType.builder()
            .id(UUID.randomUUID())
            .name("Unit Type")
            .description("Unidad que puede ser: vehiculo, moto, bicicleta, scotter, persona, paquete")
            .deleted(false)
            .createdAt(Instant.now())
            .createdBy("test-user")
            .updatedAt(Instant.now())
            .updatedBy("test-user")
            .build();

        when(entityTypeRepository.existsByNameAndDeletedFalse("Unit Type")).thenReturn(false);
        when(entityTypeRepository.save(any(EntityType.class))).thenReturn(unitTypeEntity);
        when(jwtAuthenticationService.getRequiredUserId()).thenReturn("test-user");

        // Act
        EntityTypeResponse response = entityTypeService.create(unitTypeRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Unit Type", response.getName());
        assertEquals("Unidad que puede ser: vehiculo, moto, bicicleta, scotter, persona, paquete", response.getDescription());
        verify(entityTypeRepository).save(any(EntityType.class));
    }
}
