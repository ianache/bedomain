package com.bedomain.controller;

import com.bedomain.domain.dto.entityinstance.EntityInstanceResponse;
import com.bedomain.domain.dto.statemachine.StateHistoryResponse;
import com.bedomain.domain.dto.statemachine.TriggerTransitionRequest;
import com.bedomain.service.StateHistoryService;
import com.bedomain.service.StateTransitionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/entity-instances")
@RequiredArgsConstructor
public class EntityStateController {

    private final StateTransitionService stateTransitionService;
    private final StateHistoryService stateHistoryService;

    @PostMapping("/{id}/transitions")
    public ResponseEntity<EntityInstanceResponse> triggerTransition(
            @PathVariable UUID id,
            @Valid @RequestBody TriggerTransitionRequest request) {
        EntityInstanceResponse response = stateTransitionService.triggerTransition(id, request.getEvent());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<StateHistoryResponse>> getHistory(@PathVariable UUID id) {
        List<StateHistoryResponse> history = stateHistoryService.getHistoryForEntity(id);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/{id}/state")
    public ResponseEntity<String> getCurrentState(@PathVariable UUID id) {
        String currentState = stateTransitionService.getCurrentState(id);
        return ResponseEntity.ok(currentState);
    }
}
