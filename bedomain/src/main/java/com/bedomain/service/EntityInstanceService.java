package com.bedomain.service;

import com.bedomain.dto.*;
import com.bedomain.entity.EntityInstance;
import com.bedomain.entity.EntityType;
import com.bedomain.exception.EntityNotFoundException;
import com.bedomain.repository.EntityInstanceRepository;
import com.bedomain.repository.EntityTypeRepository;
import com.bedomain.security.JwtAuthenticationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EntityInstanceService {

    private final EntityInstanceRepository entityInstanceRepository;
    private final EntityTypeRepository entityTypeRepository;
    private final JwtAuthenticationService jwtAuthenticationService;
    private final ObjectMapper objectMapper;

    @Transactional
    public EntityInstanceResponse create(CreateEntityInstanceRequest request) {
        EntityType entityType = entityTypeRepository.findById(request.getEntityTypeId())
            .orElseThrow(() -> new EntityNotFoundException("Entity type not found: " + request.getEntityTypeId()));

        String attributesJson = serializeAttributes(request.getAttributes());

        EntityInstance entityInstance = EntityInstance.builder()
            .entityTypeId(request.getEntityTypeId())
            .attributes(attributesJson)
            .createdBy(jwtAuthenticationService.getRequiredUserId())
            .build();

        entityInstance = entityInstanceRepository.save(entityInstance);
        return toResponse(entityInstance, entityType);
    }

    @Transactional(readOnly = true)
    public Page<EntityInstanceResponse> findAll(UUID entityTypeId, Pageable pageable) {
        Page<EntityInstance> entities;
        if (entityTypeId != null) {
            entities = entityInstanceRepository.findByEntityTypeId(entityTypeId, pageable);
        } else {
            entities = entityInstanceRepository.findAll(pageable);
        }

        return entities.map(entity -> {
            EntityType entityType = entityTypeRepository.findById(entity.getEntityTypeId()).orElse(null);
            return toResponse(entity, entityType);
        });
    }

    @Transactional(readOnly = true)
    public EntityInstanceResponse findById(UUID id) {
        EntityInstance entityInstance = entityInstanceRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Entity instance not found: " + id));

        EntityType entityType = entityTypeRepository.findById(entityInstance.getEntityTypeId()).orElse(null);
        return toResponse(entityInstance, entityType);
    }

    @Transactional
    public EntityInstanceResponse update(UUID id, UpdateEntityInstanceRequest request) {
        EntityInstance entityInstance = entityInstanceRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Entity instance not found: " + id));

        if (request.getAttributes() != null) {
            entityInstance.setAttributes(serializeAttributes(request.getAttributes()));
        }

        entityInstance.setUpdatedBy(jwtAuthenticationService.getRequiredUserId());
        entityInstance = entityInstanceRepository.save(entityInstance);

        EntityType entityType = entityTypeRepository.findById(entityInstance.getEntityTypeId()).orElse(null);
        return toResponse(entityInstance, entityType);
    }

    @Transactional
    public void delete(UUID id) {
        if (!entityInstanceRepository.existsById(id)) {
            throw new EntityNotFoundException("Entity instance not found: " + id);
        }
        entityInstanceRepository.deleteById(id);
    }

    private String serializeAttributes(Map<String, Object> attributes) {
        if (attributes == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attributes);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize attributes", e);
        }
    }

    private Map<String, Object> deserializeAttributes(String attributesJson) {
        if (attributesJson == null || attributesJson.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(attributesJson, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to deserialize attributes", e);
        }
    }

    private EntityInstanceResponse toResponse(EntityInstance entityInstance, EntityType entityType) {
        return EntityInstanceResponse.builder()
            .id(entityInstance.getId())
            .entityTypeId(entityInstance.getEntityTypeId())
            .attributes(deserializeAttributes(entityInstance.getAttributes()))
            .createdAt(entityInstance.getCreatedAt())
            .createdBy(entityInstance.getCreatedBy())
            .updatedAt(entityInstance.getUpdatedAt())
            .updatedBy(entityInstance.getUpdatedBy())
            .build();
    }
}
