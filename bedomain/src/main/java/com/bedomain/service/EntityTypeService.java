package com.bedomain.service;

import com.bedomain.domain.dto.entitytype.CreateEntityTypeRequest;
import com.bedomain.domain.dto.entitytype.EntityTypeResponse;
import com.bedomain.domain.dto.entitytype.UpdateEntityTypeRequest;
import com.bedomain.domain.entity.EntityType;
import com.bedomain.exception.EntityNotFoundException;
import com.bedomain.repository.EntityTypeRepository;
import com.bedomain.security.JwtAuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EntityTypeService {

    private final EntityTypeRepository entityTypeRepository;
    private final JwtAuthenticationService jwtAuthenticationService;

    @Transactional
    @CacheEvict(value = "entityTypes", allEntries = true)
    public EntityTypeResponse create(CreateEntityTypeRequest request) {
        if (entityTypeRepository.existsByNameAndDeletedFalse(request.getName())) {
            throw new IllegalArgumentException("Entity type with name '" + request.getName() + "' already exists");
        }

        EntityType entityType = EntityType.builder()
            .name(request.getName())
            .description(request.getDescription())
            .createdBy(jwtAuthenticationService.getRequiredUserId())
            .build();

        entityType = entityTypeRepository.save(entityType);
        return toResponse(entityType);
    }

    @Transactional(readOnly = true)
    public Page<EntityTypeResponse> findAll(Pageable pageable) {
        return entityTypeRepository.findByDeletedFalse(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "entityTypes", key = "#id")
    public EntityTypeResponse findById(UUID id) {
        EntityType entityType = entityTypeRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Entity type not found: " + id));
        return toResponse(entityType);
    }

    @Transactional
    @CacheEvict(value = "entityTypes", allEntries = true)
    public EntityTypeResponse update(UUID id, UpdateEntityTypeRequest request) {
        EntityType entityType = entityTypeRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Entity type not found: " + id));

        if (request.getName().isPresent() && !request.getName().get().equals(entityType.getName())) {
            if (entityTypeRepository.existsByNameAndDeletedFalse(request.getName().get())) {
                throw new IllegalArgumentException("Entity type with name '" + request.getName().get() + "' already exists");
            }
            entityType.setName(request.getName().get());
        }

        if (request.getDescription().isPresent()) {
            entityType.setDescription(request.getDescription().get());
        }

        entityType.setUpdatedBy(jwtAuthenticationService.getRequiredUserId());
        entityType = entityTypeRepository.save(entityType);
        return toResponse(entityType);
    }

    @Transactional
    @CacheEvict(value = "entityTypes", allEntries = true)
    public void delete(UUID id) {
        EntityType entityType = entityTypeRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Entity type not found: " + id));
        entityType.setDeleted(true);
        entityType.setUpdatedBy(jwtAuthenticationService.getRequiredUserId());
        entityTypeRepository.save(entityType);
    }

    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return entityTypeRepository.existsById(id);
    }

    private EntityTypeResponse toResponse(EntityType entityType) {
        return EntityTypeResponse.builder()
            .id(entityType.getId())
            .name(entityType.getName())
            .description(entityType.getDescription())
            .createdAt(entityType.getCreatedAt())
            .createdBy(entityType.getCreatedBy())
            .updatedAt(entityType.getUpdatedAt())
            .updatedBy(entityType.getUpdatedBy())
            .build();
    }
}
