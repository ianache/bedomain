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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EntityInstanceService {

    private final EntityInstanceRepository entityInstanceRepository;
    private final EntityTypeRepository entityTypeRepository;
    private final JwtAuthenticationService jwtAuthenticationService;

    @Transactional
    public EntityInstanceResponse create(CreateEntityInstanceRequest request) {
        EntityType entityType = entityTypeRepository.findById(request.getEntityTypeId())
            .orElseThrow(() -> new EntityNotFoundException("Entity type not found: " + request.getEntityTypeId()));

        EntityInstance entityInstance = EntityInstance.builder()
            .entityType(entityType)
            .attributes(request.getAttributes())
            .createdBy(jwtAuthenticationService.getRequiredUserId())
            .build();

        entityInstance = entityInstanceRepository.save(entityInstance);
        return toResponse(entityInstance);
    }

    @Transactional(readOnly = true)
    public Page<EntityInstanceResponse> findAll(UUID entityTypeId, Pageable pageable) {
        Page<EntityInstance> entities;
        if (entityTypeId != null) {
            entities = entityInstanceRepository.findByEntityTypeIdAndDeletedFalse(entityTypeId, pageable);
        } else {
            entities = entityInstanceRepository.findByDeletedFalse(pageable);
        }

        return entities.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public EntityInstanceResponse findById(UUID id) {
        EntityInstance entityInstance = entityInstanceRepository.findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new EntityNotFoundException("Entity instance not found: " + id));
        return toResponse(entityInstance);
    }

    @Transactional
    public EntityInstanceResponse update(UUID id, UpdateEntityInstanceRequest request) {
        EntityInstance entityInstance = entityInstanceRepository.findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new EntityNotFoundException("Entity instance not found: " + id));

        if (request.getAttributes() != null) {
            entityInstance.setAttributes(request.getAttributes());
        }

        entityInstance.setUpdatedBy(jwtAuthenticationService.getRequiredUserId());
        entityInstance = entityInstanceRepository.save(entityInstance);

        return toResponse(entityInstance);
    }

    @Transactional
    public void delete(UUID id) {
        EntityInstance entityInstance = entityInstanceRepository.findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new EntityNotFoundException("Entity instance not found: " + id));
        
        entityInstance.setDeleted(true);
        entityInstance.setUpdatedBy(jwtAuthenticationService.getRequiredUserId());
        entityInstanceRepository.save(entityInstance);
    }

    private EntityInstanceResponse toResponse(EntityInstance entityInstance) {
        return EntityInstanceResponse.builder()
            .id(entityInstance.getId())
            .entityTypeId(entityInstance.getEntityType().getId())
            .entityTypeName(entityInstance.getEntityType().getName())
            .attributes(entityInstance.getAttributes())
            .createdAt(entityInstance.getCreatedAt())
            .createdBy(entityInstance.getCreatedBy())
            .updatedAt(entityInstance.getUpdatedAt())
            .updatedBy(entityInstance.getUpdatedBy())
            .build();
    }
}
