package com.bedomain.service;

import com.bedomain.domain.dto.property.CreatePropertyRequest;
import com.bedomain.domain.dto.property.PropertyResponse;
import com.bedomain.domain.dto.property.UpdatePropertyRequest;
import com.bedomain.domain.entity.EntityType;
import com.bedomain.domain.entity.Property;
import com.bedomain.domain.enums.DataType;
import com.bedomain.exception.EntityNotFoundException;
import com.bedomain.repository.EntityTypeRepository;
import com.bedomain.repository.PropertyRepository;
import com.bedomain.security.JwtAuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PropertySpecServiceTest {

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private EntityTypeRepository entityTypeRepository;

    @Mock
    private JwtAuthenticationService jwtAuthenticationService;

    @InjectMocks
    private PropertySpecService propertySpecService;

    private UUID entityTypeId;
    private UUID propertyId;
    private EntityType testEntityType;
    private Property testProperty;

    @BeforeEach
    void setUp() {
        entityTypeId = UUID.randomUUID();
        propertyId = UUID.randomUUID();

        testEntityType = EntityType.builder()
            .id(entityTypeId)
            .name("TestEntity")
            .description("Test Description")
            .deleted(false)
            .createdAt(Instant.now())
            .createdBy("test-user")
            .build();

        testProperty = Property.builder()
            .id(propertyId)
            .name("testProperty")
            .description("Test Property Description")
            .dataType(DataType.STRING)
            .entityType(testEntityType)
            .createdAt(Instant.now())
            .createdBy("test-user")
            .build();
    }

    @Test
    void create_shouldThrowException_whenEntityTypeNotFound() {
        // Arrange
        UUID notFoundId = UUID.randomUUID();
        CreatePropertyRequest request = new CreatePropertyRequest();
        request.setName("testProperty");
        request.setDataType(DataType.STRING);
        
        when(entityTypeRepository.findById(notFoundId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> propertySpecService.create(notFoundId, request)
        );
        assertTrue(exception.getMessage().contains("Entity type not found"));
    }

    @Test
    void create_shouldThrowException_whenPropertyNameExists() {
        // Arrange
        CreatePropertyRequest request = new CreatePropertyRequest();
        request.setName("testProperty");
        request.setDataType(DataType.STRING);
        
        when(entityTypeRepository.findById(entityTypeId)).thenReturn(Optional.of(testEntityType));
        when(propertyRepository.existsByEntityTypeIdAndName(entityTypeId, "testProperty")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> propertySpecService.create(entityTypeId, request)
        );
        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    void create_shouldReturnResponse_whenValidRequest() {
        // Arrange
        CreatePropertyRequest request = new CreatePropertyRequest();
        request.setName("newProperty");
        request.setDataType(DataType.NUMBER);
        request.setDescription("New Property");

        when(entityTypeRepository.findById(entityTypeId)).thenReturn(Optional.of(testEntityType));
        when(propertyRepository.existsByEntityTypeIdAndName(entityTypeId, "newProperty")).thenReturn(false);
        when(propertyRepository.save(any(Property.class))).thenAnswer(invocation -> {
            Property p = invocation.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });
        when(jwtAuthenticationService.getRequiredUserId()).thenReturn("test-user");

        // Act
        PropertyResponse response = propertySpecService.create(entityTypeId, request);

        // Assert
        assertNotNull(response);
        assertEquals("newProperty", response.getName());
    }

    @Test
    void findByEntityTypeId_shouldThrowException_whenEntityTypeNotFound() {
        // Arrange
        UUID notFoundId = UUID.randomUUID();
        when(entityTypeRepository.existsById(notFoundId)).thenReturn(false);

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> propertySpecService.findByEntityTypeId(notFoundId)
        );
        assertTrue(exception.getMessage().contains("Entity type not found"));
    }

    @Test
    void findByEntityTypeId_shouldReturnPropertiesList_whenExists() {
        // Arrange
        when(entityTypeRepository.existsById(entityTypeId)).thenReturn(true);
        when(propertyRepository.findByEntityTypeId(entityTypeId)).thenReturn(List.of(testProperty));

        // Act
        List<PropertyResponse> result = propertySpecService.findByEntityTypeId(entityTypeId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testProperty", result.get(0).getName());
    }

    @Test
    void update_shouldThrowException_whenEntityTypeNotFound() {
        // Arrange
        UUID notFoundId = UUID.randomUUID();
        UpdatePropertyRequest request = new UpdatePropertyRequest();
        
        when(entityTypeRepository.existsById(notFoundId)).thenReturn(false);

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> propertySpecService.update(notFoundId, propertyId, request)
        );
        assertTrue(exception.getMessage().contains("Entity type not found"));
    }

    @Test
    void update_shouldThrowException_whenPropertyNotFound() {
        // Arrange
        UUID notFoundPropertyId = UUID.randomUUID();
        UpdatePropertyRequest request = new UpdatePropertyRequest();
        
        when(entityTypeRepository.existsById(entityTypeId)).thenReturn(true);
        when(propertyRepository.findById(notFoundPropertyId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> propertySpecService.update(entityTypeId, notFoundPropertyId, request)
        );
        assertTrue(exception.getMessage().contains("Property specification not found"));
    }

    @Test
    void update_shouldThrowException_whenPropertyDoesNotBelongToEntityType() {
        // Arrange
        EntityType differentEntityType = EntityType.builder()
            .id(UUID.randomUUID())
            .name("DifferentEntity")
            .build();
        
        Property propertyWithDifferentOwner = Property.builder()
            .id(propertyId)
            .name("testProperty")
            .entityType(differentEntityType)
            .build();
        
        UpdatePropertyRequest request = new UpdatePropertyRequest();

        when(entityTypeRepository.existsById(entityTypeId)).thenReturn(true);
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(propertyWithDifferentOwner));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> propertySpecService.update(entityTypeId, propertyId, request)
        );
        assertTrue(exception.getMessage().contains("does not belong to"));
    }

    @Test
    void update_shouldReturnResponse_whenValidRequest() {
        // Arrange
        UpdatePropertyRequest request = new UpdatePropertyRequest();
        request.setName(Optional.of("updatedProperty"));
        request.setDescription(Optional.of("Updated Description"));
        request.setDataType(Optional.of(DataType.NUMBER));
        
        when(entityTypeRepository.existsById(entityTypeId)).thenReturn(true);
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(testProperty));
        when(propertyRepository.existsByEntityTypeIdAndName(entityTypeId, "updatedProperty")).thenReturn(false);
        when(propertyRepository.save(any(Property.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtAuthenticationService.getRequiredUserId()).thenReturn("test-user");

        // Act
        PropertyResponse response = propertySpecService.update(entityTypeId, propertyId, request);

        // Assert
        assertNotNull(response);
        assertEquals("updatedProperty", response.getName());
    }

    @Test
    void delete_shouldThrowException_whenEntityTypeNotFound() {
        // Arrange
        UUID notFoundId = UUID.randomUUID();
        when(entityTypeRepository.existsById(notFoundId)).thenReturn(false);

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> propertySpecService.delete(notFoundId, propertyId)
        );
        assertTrue(exception.getMessage().contains("Entity type not found"));
    }

    @Test
    void delete_shouldThrowException_whenPropertyNotFound() {
        // Arrange
        UUID notFoundPropertyId = UUID.randomUUID();
        
        when(entityTypeRepository.existsById(entityTypeId)).thenReturn(true);
        when(propertyRepository.findById(notFoundPropertyId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> propertySpecService.delete(entityTypeId, notFoundPropertyId)
        );
        assertTrue(exception.getMessage().contains("Property specification not found"));
    }

    @Test
    void delete_shouldDeleteProperty_whenExists() {
        // Arrange
        when(entityTypeRepository.existsById(entityTypeId)).thenReturn(true);
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(testProperty));

        // Act
        propertySpecService.delete(entityTypeId, propertyId);

        // Assert
        verify(propertyRepository).deleteById(propertyId);
    }
}
