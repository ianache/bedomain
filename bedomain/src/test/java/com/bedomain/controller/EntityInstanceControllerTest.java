package com.bedomain.controller;

import com.bedomain.domain.dto.entityinstance.CreateEntityInstanceRequest;
import com.bedomain.domain.dto.entityinstance.EntityInstanceResponse;
import com.bedomain.domain.dto.entityinstance.UpdateEntityInstanceRequest;
import com.bedomain.exception.EntityNotFoundException;
import com.bedomain.service.EntityInstanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EntityInstanceControllerTest {

    @Mock
    private EntityInstanceService entityInstanceService;

    @InjectMocks
    private EntityInstanceController entityInstanceController;

    private UUID entityTypeId;
    private UUID instanceId;
    private EntityInstanceResponse testResponse;

    @BeforeEach
    void setUp() {
        entityTypeId = UUID.randomUUID();
        instanceId = UUID.randomUUID();

        testResponse = EntityInstanceResponse.builder()
            .id(instanceId)
            .entityTypeId(entityTypeId)
            .entityTypeName("TestEntity")
            .attributes(Map.of("field1", "value1"))
            .createdAt(Instant.now())
            .createdBy("test-user")
            .updatedAt(Instant.now())
            .updatedBy("test-user")
            .build();
    }

    @Test
    void create_shouldReturn201_whenValidRequest() {
        // Arrange
        CreateEntityInstanceRequest request = new CreateEntityInstanceRequest();
        request.setEntityTypeId(entityTypeId);
        request.setAttributes(Map.of("field1", "value1"));

        when(entityInstanceService.create(any(CreateEntityInstanceRequest.class)))
            .thenReturn(testResponse);

        // Act
        ResponseEntity<EntityInstanceResponse> response = entityInstanceController.create(request);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(instanceId, response.getBody().getId());
    }

    @Test
    void create_shouldThrowException_whenEntityTypeNotFound() {
        // Arrange
        UUID notFoundId = UUID.randomUUID();
        CreateEntityInstanceRequest request = new CreateEntityInstanceRequest();
        request.setEntityTypeId(notFoundId);
        request.setAttributes(Map.of("field1", "value1"));

        when(entityInstanceService.create(any(CreateEntityInstanceRequest.class)))
            .thenThrow(new EntityNotFoundException("Entity type not found: " + notFoundId));

        // Act & Assert
        assertThrows(EntityNotFoundException.class, 
            () -> entityInstanceController.create(request));
    }

    @Test
    void findAll_shouldReturn200_withPagedResults() {
        // Arrange
        PageRequest pageable = PageRequest.of(0, 10);
        Page<EntityInstanceResponse> page = new PageImpl<>(List.of(testResponse));
        when(entityInstanceService.findAll(any(), any(PageRequest.class))).thenReturn(page);

        // Act
        ResponseEntity<Page<EntityInstanceResponse>> response = entityInstanceController.findAll(null, pageable);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    void findAll_shouldFilterByEntityTypeId() {
        // Arrange
        PageRequest pageable = PageRequest.of(0, 10);
        Page<EntityInstanceResponse> page = new PageImpl<>(List.of(testResponse));
        when(entityInstanceService.findAll(any(UUID.class), any(PageRequest.class))).thenReturn(page);

        // Act
        ResponseEntity<Page<EntityInstanceResponse>> response = entityInstanceController.findAll(entityTypeId, pageable);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(entityTypeId, response.getBody().getContent().get(0).getEntityTypeId());
    }

    @Test
    void findById_shouldReturn200_whenExists() {
        // Arrange
        when(entityInstanceService.findById(instanceId)).thenReturn(testResponse);

        // Act
        ResponseEntity<EntityInstanceResponse> response = entityInstanceController.findById(instanceId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(instanceId, response.getBody().getId());
    }

    @Test
    void findById_shouldThrowException_whenNotFound() {
        // Arrange
        UUID notFoundId = UUID.randomUUID();
        when(entityInstanceService.findById(notFoundId))
            .thenThrow(new EntityNotFoundException("Entity instance not found: " + notFoundId));

        // Act & Assert
        assertThrows(EntityNotFoundException.class, 
            () -> entityInstanceController.findById(notFoundId));
    }

    @Test
    void update_shouldReturn200_whenValidRequest() {
        // Arrange
        UpdateEntityInstanceRequest request = new UpdateEntityInstanceRequest();
        request.setAttributes(Map.of("updatedField", "updatedValue"));

        EntityInstanceResponse updatedResponse = EntityInstanceResponse.builder()
            .id(instanceId)
            .entityTypeId(entityTypeId)
            .entityTypeName("TestEntity")
            .attributes(Map.of("updatedField", "updatedValue"))
            .build();

        when(entityInstanceService.update(any(UUID.class), any(UpdateEntityInstanceRequest.class)))
            .thenReturn(updatedResponse);

        // Act
        ResponseEntity<EntityInstanceResponse> response = entityInstanceController.update(instanceId, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("updatedValue", response.getBody().getAttributes().get("updatedField"));
    }

    @Test
    void delete_shouldReturn204_whenExists() {
        // Arrange
        doNothing().when(entityInstanceService).delete(instanceId);

        // Act
        ResponseEntity<Void> response = entityInstanceController.delete(instanceId);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void delete_shouldThrowException_whenNotFound() {
        // Arrange
        UUID notFoundId = UUID.randomUUID();
        doThrow(new EntityNotFoundException("Entity instance not found: " + notFoundId))
            .when(entityInstanceService).delete(notFoundId);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, 
            () -> entityInstanceController.delete(notFoundId));
    }
}
