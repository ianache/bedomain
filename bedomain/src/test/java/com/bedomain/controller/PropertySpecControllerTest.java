package com.bedomain.controller;

import com.bedomain.domain.dto.property.CreatePropertyRequest;
import com.bedomain.domain.dto.property.PropertyResponse;
import com.bedomain.domain.dto.property.UpdatePropertyRequest;
import com.bedomain.domain.enums.DataType;
import com.bedomain.exception.EntityNotFoundException;
import com.bedomain.service.PropertySpecService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PropertySpecControllerTest {

    @Mock
    private PropertySpecService propertySpecService;

    @InjectMocks
    private PropertySpecController propertySpecController;

    private UUID entityTypeId;
    private UUID propertyId;
    private PropertyResponse testResponse;

    @BeforeEach
    void setUp() {
        entityTypeId = UUID.randomUUID();
        propertyId = UUID.randomUUID();

        testResponse = PropertyResponse.builder()
            .id(propertyId)
            .name("testProperty")
            .description("Test Property Description")
            .dataType(DataType.STRING)
            .build();
    }

    @Test
    void create_shouldReturn201_whenValidRequest() {
        // Arrange
        CreatePropertyRequest request = new CreatePropertyRequest();
        request.setName("testProperty");
        request.setDescription("Test Property Description");
        request.setDataType(DataType.STRING);

        when(propertySpecService.create(any(UUID.class), any(CreatePropertyRequest.class)))
            .thenReturn(testResponse);

        // Act
        ResponseEntity<PropertyResponse> response = propertySpecController.create(entityTypeId, request);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(propertyId, response.getBody().getId());
    }

    @Test
    void create_shouldThrowException_whenEntityTypeNotFound() {
        // Arrange
        UUID notFoundId = UUID.randomUUID();
        CreatePropertyRequest request = new CreatePropertyRequest();
        request.setName("testProperty");
        request.setDataType(DataType.STRING);

        when(propertySpecService.create(any(UUID.class), any(CreatePropertyRequest.class)))
            .thenThrow(new EntityNotFoundException("Entity type not found: " + notFoundId));

        // Act & Assert
        assertThrows(EntityNotFoundException.class, 
            () -> propertySpecController.create(notFoundId, request));
    }

    @Test
    void findByEntityTypeId_shouldReturn200_withProperties() {
        // Arrange
        when(propertySpecService.findByEntityTypeId(entityTypeId))
            .thenReturn(List.of(testResponse));

        // Act
        ResponseEntity<List<PropertyResponse>> response = propertySpecController.findByEntityTypeId(entityTypeId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void update_shouldReturn200_whenValidRequest() {
        // Arrange
        UpdatePropertyRequest request = new UpdatePropertyRequest();
        request.setName(Optional.of("updatedProperty"));

        PropertyResponse updatedResponse = PropertyResponse.builder()
            .id(propertyId)
            .name("updatedProperty")
            .description("Test Property Description")
            .dataType(DataType.STRING)
            .build();

        when(propertySpecService.update(any(UUID.class), any(UUID.class), any(UpdatePropertyRequest.class)))
            .thenReturn(updatedResponse);

        // Act
        ResponseEntity<PropertyResponse> response = propertySpecController.update(entityTypeId, propertyId, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("updatedProperty", response.getBody().getName());
    }

    @Test
    void delete_shouldReturn204_whenExists() {
        // Arrange
        doNothing().when(propertySpecService).delete(entityTypeId, propertyId);

        // Act
        ResponseEntity<Void> response = propertySpecController.delete(entityTypeId, propertyId);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void delete_shouldThrowException_whenPropertyNotFound() {
        // Arrange
        UUID notFoundPropertyId = UUID.randomUUID();
        doThrow(new EntityNotFoundException("Property specification not found: " + notFoundPropertyId))
            .when(propertySpecService).delete(entityTypeId, notFoundPropertyId);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, 
            () -> propertySpecController.delete(entityTypeId, notFoundPropertyId));
    }
}
