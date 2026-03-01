package com.bedomain.controller;

import com.bedomain.dto.*;
import com.bedomain.service.EntityInstanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/entities")
@RequiredArgsConstructor
public class EntityInstanceController {

    private final EntityInstanceService entityInstanceService;

    @PostMapping
    public ResponseEntity<EntityInstanceResponse> create(@Valid @RequestBody CreateEntityInstanceRequest request) {
        EntityInstanceResponse response = entityInstanceService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<EntityInstanceResponse>> findAll(
            @RequestParam(required = false) UUID entityTypeId,
            Pageable pageable) {
        return ResponseEntity.ok(entityInstanceService.findAll(entityTypeId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityInstanceResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(entityInstanceService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityInstanceResponse> update(
            @PathVariable UUID id,
            @RequestBody UpdateEntityInstanceRequest request) {
        return ResponseEntity.ok(entityInstanceService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        entityInstanceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
