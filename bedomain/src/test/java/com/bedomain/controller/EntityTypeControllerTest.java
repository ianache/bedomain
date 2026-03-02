package com.bedomain.controller;

import com.bedomain.domain.dto.entitytype.CreateEntityTypeRequest;
import com.bedomain.domain.dto.entitytype.EntityTypeResponse;
import com.bedomain.domain.dto.entitytype.UpdateEntityTypeRequest;
import com.bedomain.exception.EntityNotFoundException;
import com.bedomain.service.EntityTypeService;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EntityTypeControllerTest {

    @Mock
    private EntityTypeService entityTypeService;

    @InjectMocks
    private EntityTypeController entityTypeController;

    private UUID testId;
    private EntityTypeResponse testResponse;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        
        testResponse = EntityTypeResponse.builder()
            .id(testId)
            .name("TestEntity")
            .description("Test Description")
            .createdAt(Instant.now())
            .createdBy("test-user")
            .updatedAt(Instant.now())
            .updatedBy("test-user")
            .build();
    }

    @Test
    void create_shouldReturn201_whenValidRequest() {
        // Arrange
        CreateEntityTypeRequest request = new CreateEntityTypeRequest();
        request.setName("TestEntity");
        request.setDescription("Test Description");
        
        when(entityTypeService.create(any(CreateEntityTypeRequest.class))).thenReturn(testResponse);

        // Act
        ResponseEntity<EntityTypeResponse> response = entityTypeController.create(request);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testId, response.getBody().getId());
        assertEquals("TestEntity", response.getBody().getName());
    }

    @Test
    void findAll_shouldReturn200_withPagedResults() {
        // Arrange
        PageRequest pageable = PageRequest.of(0, 10);
        Page<EntityTypeResponse> page = new PageImpl<>(List.of(testResponse));
        when(entityTypeService.findAll(any(PageRequest.class))).thenReturn(page);

        // Act
        ResponseEntity<Page<EntityTypeResponse>> response = entityTypeController.findAll(pageable);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    void findById_shouldReturn200_whenExists() {
        // Arrange
        when(entityTypeService.findById(testId)).thenReturn(testResponse);

        // Act
        ResponseEntity<EntityTypeResponse> response = entityTypeController.findById(testId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testId, response.getBody().getId());
    }

    @Test
    void findById_shouldThrowException_whenNotFound() {
        // Arrange
        UUID notFoundId = UUID.randomUUID();
        when(entityTypeService.findById(notFoundId))
            .thenThrow(new EntityNotFoundException("Entity type not found: " + notFoundId));

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> entityTypeController.findById(notFoundId));
    }

    @Test
    void update_shouldReturn200_whenValidRequest() {
        // Arrange
        UpdateEntityTypeRequest request = new UpdateEntityTypeRequest();
        request.setName(Optional.of("UpdatedName"));
        request.setDescription(Optional.of("Updated Description"));

        EntityTypeResponse updatedResponse = EntityTypeResponse.builder()
            .id(testId)
            .name("UpdatedName")
            .description("Updated Description")
            .build();

        when(entityTypeService.update(any(UUID.class), any(UpdateEntityTypeRequest.class)))
            .thenReturn(updatedResponse);

        // Act
        ResponseEntity<EntityTypeResponse> response = entityTypeController.update(testId, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("UpdatedName", response.getBody().getName());
    }

    @Test
    void update_shouldThrowException_whenNotFound() {
        // Arrange
        UUID notFoundId = UUID.randomUUID();
        UpdateEntityTypeRequest request = new UpdateEntityTypeRequest();
        request.setName(Optional.of("UpdatedName"));

        when(entityTypeService.update(any(UUID.class), any(UpdateEntityTypeRequest.class)))
            .thenThrow(new EntityNotFoundException("Entity type not found: " + notFoundId));

        // Act & Assert
        assertThrows(EntityNotFoundException.class, 
            () -> entityTypeController.update(notFoundId, request));
    }

    @Test
    void delete_shouldReturn204_whenExists() {
        // Arrange
        doNothing().when(entityTypeService).delete(testId);

        // Act
        ResponseEntity<Void> response = entityTypeController.delete(testId);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void delete_shouldThrowException_whenNotFound() {
        // Arrange
        UUID notFoundId = UUID.randomUUID();
        doThrow(new EntityNotFoundException("Entity type not found: " + notFoundId))
            .when(entityTypeService).delete(notFoundId);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> entityTypeController.delete(notFoundId));
    }
}
