package com.bedomain.service;

import com.bedomain.domain.dto.property.CreatePropertyRequest;
import com.bedomain.domain.dto.property.PropertyResponse;
import com.bedomain.domain.dto.property.UpdatePropertyRequest;
import com.bedomain.domain.entity.EntityType;
import com.bedomain.domain.entity.Property;
import com.bedomain.exception.EntityNotFoundException;
import com.bedomain.repository.EntityTypeRepository;
import com.bedomain.repository.PropertyRepository;
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

    private final PropertyRepository propertyRepository;
    private final EntityTypeRepository entityTypeRepository;
    private final JwtAuthenticationService jwtAuthenticationService;

    @Transactional
    public PropertyResponse create(UUID entityTypeId, CreatePropertyRequest request) {
        EntityType entityType = entityTypeRepository.findById(entityTypeId)
            .orElseThrow(() -> new EntityNotFoundException("Entity type not found: " + entityTypeId));

        if (propertyRepository.existsByEntityTypeIdAndName(entityTypeId, request.getName())) {
            throw new IllegalArgumentException("Property with name '" + request.getName() + "' already exists for this entity type");
        }

        Property property = Property.builder()
            .name(request.getName())
            .description(request.getDescription())
            .dataType(request.getDataType())
            .entityType(entityType)
            .createdBy(jwtAuthenticationService.getRequiredUserId())
            .build();

        property = propertyRepository.save(property);
        return toResponse(property);
    }

    @Transactional(readOnly = true)
    public List<PropertyResponse> findByEntityTypeId(UUID entityTypeId) {
        if (!entityTypeRepository.existsById(entityTypeId)) {
            throw new EntityNotFoundException("Entity type not found: " + entityTypeId);
        }
        return propertyRepository.findByEntityTypeId(entityTypeId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public PropertyResponse update(UUID entityTypeId, UUID propertyId, UpdatePropertyRequest request) {
        if (!entityTypeRepository.existsById(entityTypeId)) {
            throw new EntityNotFoundException("Entity type not found: " + entityTypeId);
        }

        Property property = propertyRepository.findById(propertyId)
            .orElseThrow(() -> new EntityNotFoundException("Property specification not found: " + propertyId));

        if (!property.getEntityType().getId().equals(entityTypeId)) {
            throw new IllegalArgumentException("Property does not belong to the specified entity type");
        }

        if (request.getName().isPresent() && !request.getName().get().equals(property.getName())) {
            if (propertyRepository.existsByEntityTypeIdAndName(entityTypeId, request.getName().get())) {
                throw new IllegalArgumentException("Property with name '" + request.getName().get() + "' already exists");
            }
            property.setName(request.getName().get());
        }

        if (request.getDescription().isPresent()) {
            property.setDescription(request.getDescription().get());
        }

        if (request.getDataType().isPresent()) {
            property.setDataType(request.getDataType().get());
        }

        property.setUpdatedBy(jwtAuthenticationService.getRequiredUserId());
        property = propertyRepository.save(property);
        return toResponse(property);
    }

    @Transactional
    public void delete(UUID entityTypeId, UUID propertyId) {
        if (!entityTypeRepository.existsById(entityTypeId)) {
            throw new EntityNotFoundException("Entity type not found: " + entityTypeId);
        }

        Property property = propertyRepository.findById(propertyId)
            .orElseThrow(() -> new EntityNotFoundException("Property specification not found: " + propertyId));

        if (!property.getEntityType().getId().equals(entityTypeId)) {
            throw new IllegalArgumentException("Property does not belong to the specified entity type");
        }

        propertyRepository.deleteById(propertyId);
    }

    private PropertyResponse toResponse(Property property) {
        return PropertyResponse.builder()
            .id(property.getId())
            .name(property.getName())
            .description(property.getDescription())
            .dataType(property.getDataType())
            .build();
    }
}
