package com.bedomain.service;

import com.bedomain.dto.*;
import com.bedomain.entity.EntityType;
import com.bedomain.entity.PropertySpec;
import com.bedomain.exception.EntityNotFoundException;
import com.bedomain.repository.EntityTypeRepository;
import com.bedomain.repository.PropertySpecRepository;
import com.bedomain.security.JwtAuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PropertySpecService {

    private final PropertySpecRepository propertySpecRepository;
    private final EntityTypeRepository entityTypeRepository;
    private final JwtAuthenticationService jwtAuthenticationService;

    @Transactional
    public PropertySpecResponse create(UUID entityTypeId, CreatePropertySpecRequest request) {
        validateEntityTypeExists(entityTypeId);

        if (propertySpecRepository.existsByEntityTypeIdAndName(entityTypeId, request.getName())) {
            throw new IllegalArgumentException("Property with name '" + request.getName() + "' already exists for this entity type");
        }

        PropertySpec propertySpec = PropertySpec.builder()
            .entityTypeId(entityTypeId)
            .name(request.getName())
            .description(request.getDescription())
            .dataType(request.getDataType())
            .createdBy(jwtAuthenticationService.getRequiredUserId())
            .build();

        propertySpec = propertySpecRepository.save(propertySpec);
        return toResponse(propertySpec);
    }

    @Transactional(readOnly = true)
    public List<PropertySpecResponse> findByEntityTypeId(UUID entityTypeId) {
        validateEntityTypeExists(entityTypeId);
        return propertySpecRepository.findByEntityTypeId(entityTypeId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public PropertySpecResponse update(UUID entityTypeId, UUID propertyId, UpdatePropertySpecRequest request) {
        validateEntityTypeExists(entityTypeId);

        PropertySpec propertySpec = propertySpecRepository.findById(propertyId)
            .orElseThrow(() -> new EntityNotFoundException("Property specification not found: " + propertyId));

        if (!propertySpec.getEntityTypeId().equals(entityTypeId)) {
            throw new IllegalArgumentException("Property does not belong to the specified entity type");
        }

        if (request.getName() != null && !request.getName().equals(propertySpec.getName())) {
            if (propertySpecRepository.existsByEntityTypeIdAndName(entityTypeId, request.getName())) {
                throw new IllegalArgumentException("Property with name '" + request.getName() + "' already exists");
            }
            propertySpec.setName(request.getName());
        }

        if (request.getDescription() != null) {
            propertySpec.setDescription(request.getDescription());
        }

        if (request.getDataType() != null) {
            propertySpec.setDataType(request.getDataType());
        }

        propertySpec.setUpdatedBy(jwtAuthenticationService.getRequiredUserId());
        propertySpec = propertySpecRepository.save(propertySpec);
        return toResponse(propertySpec);
    }

    @Transactional
    public void delete(UUID entityTypeId, UUID propertyId) {
        validateEntityTypeExists(entityTypeId);

        PropertySpec propertySpec = propertySpecRepository.findById(propertyId)
            .orElseThrow(() -> new EntityNotFoundException("Property specification not found: " + propertyId));

        if (!propertySpec.getEntityTypeId().equals(entityTypeId)) {
            throw new IllegalArgumentException("Property does not belong to the specified entity type");
        }

        propertySpecRepository.deleteById(propertyId);
    }

    private void validateEntityTypeExists(UUID entityTypeId) {
        if (!entityTypeRepository.existsById(entityTypeId)) {
            throw new EntityNotFoundException("Entity type not found: " + entityTypeId);
        }
    }

    private PropertySpecResponse toResponse(PropertySpec propertySpec) {
        return PropertySpecResponse.builder()
            .id(propertySpec.getId())
            .entityTypeId(propertySpec.getEntityTypeId())
            .name(propertySpec.getName())
            .description(propertySpec.getDescription())
            .dataType(propertySpec.getDataType())
            .createdAt(propertySpec.getCreatedAt())
            .createdBy(propertySpec.getCreatedBy())
            .updatedAt(propertySpec.getUpdatedAt())
            .updatedBy(propertySpec.getUpdatedBy())
            .build();
    }
}
