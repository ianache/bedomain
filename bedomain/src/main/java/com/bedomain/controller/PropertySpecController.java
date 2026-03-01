package com.bedomain.controller;

import com.bedomain.dto.*;
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
    public ResponseEntity<PropertySpecResponse> create(
            @PathVariable UUID entityTypeId,
            @Valid @RequestBody CreatePropertySpecRequest request) {
        PropertySpecResponse response = propertySpecService.create(entityTypeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<PropertySpecResponse>> findByEntityTypeId(@PathVariable UUID entityTypeId) {
        return ResponseEntity.ok(propertySpecService.findByEntityTypeId(entityTypeId));
    }

    @PutMapping("/{propertyId}")
    public ResponseEntity<PropertySpecResponse> update(
            @PathVariable UUID entityTypeId,
            @PathVariable UUID propertyId,
            @RequestBody UpdatePropertySpecRequest request) {
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
