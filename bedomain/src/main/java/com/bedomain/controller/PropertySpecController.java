package com.bedomain.controller;

import com.bedomain.domain.dto.property.CreatePropertyRequest;
import com.bedomain.domain.dto.property.PropertyResponse;
import com.bedomain.domain.dto.property.UpdatePropertyRequest;
import com.bedomain.service.PropertySpecService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/entity-types/{entityTypeId}/properties")
@RequiredArgsConstructor
public class PropertySpecController {

    private final PropertySpecService propertySpecService;

    @PostMapping
    public ResponseEntity<PropertyResponse> create(
            @PathVariable UUID entityTypeId,
            @Valid @RequestBody CreatePropertyRequest request) {
        PropertyResponse response = propertySpecService.create(entityTypeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<PropertyResponse>> findByEntityTypeId(@PathVariable UUID entityTypeId) {
        return ResponseEntity.ok(propertySpecService.findByEntityTypeId(entityTypeId));
    }

    @PutMapping("/{propertyId}")
    public ResponseEntity<PropertyResponse> update(
            @PathVariable UUID entityTypeId,
            @PathVariable UUID propertyId,
            @RequestBody UpdatePropertyRequest request) {
        return ResponseEntity.ok(propertySpecService.update(entityTypeId, propertyId, request));
    }

    @DeleteMapping("/{propertyId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID entityTypeId,
            @PathVariable UUID propertyId) {
        propertySpecService.delete(entityTypeId, propertyId);
        return ResponseEntity.noContent().build();
    }
}
