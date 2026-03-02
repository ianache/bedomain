package com.bedomain.controller;

import com.bedomain.domain.dto.entitytype.CreateEntityTypeRequest;
import com.bedomain.domain.dto.entitytype.EntityTypeResponse;
import com.bedomain.domain.dto.entitytype.UpdateEntityTypeRequest;
import com.bedomain.service.EntityTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/entity-types")
@RequiredArgsConstructor
public class EntityTypeController {

    private final EntityTypeService entityTypeService;

    @PostMapping
    public ResponseEntity<EntityTypeResponse> create(@Valid @RequestBody CreateEntityTypeRequest request) {
        EntityTypeResponse response = entityTypeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<EntityTypeResponse>> findAll(Pageable pageable) {
        return ResponseEntity.ok(entityTypeService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityTypeResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(entityTypeService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityTypeResponse> update(
            @PathVariable UUID id,
            @RequestBody UpdateEntityTypeRequest request) {
        return ResponseEntity.ok(entityTypeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        entityTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
