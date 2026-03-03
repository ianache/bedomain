package com.bedomain.controller;

import com.bedomain.domain.dto.statemachine.CreateStateMachineRequest;
import com.bedomain.domain.dto.statemachine.StateMachineResponse;
import com.bedomain.domain.dto.statemachine.UpdateStateMachineRequest;
import com.bedomain.service.StateMachineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/state-machines")
@RequiredArgsConstructor
public class StateMachineController {

    private final StateMachineService stateMachineService;

    @PostMapping
    public ResponseEntity<StateMachineResponse> create(@Valid @RequestBody CreateStateMachineRequest request) {
        StateMachineResponse response = stateMachineService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<StateMachineResponse>> findAll(Pageable pageable) {
        return ResponseEntity.ok(stateMachineService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StateMachineResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(stateMachineService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StateMachineResponse> update(
            @PathVariable UUID id,
            @RequestBody UpdateStateMachineRequest request) {
        return ResponseEntity.ok(stateMachineService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        stateMachineService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
