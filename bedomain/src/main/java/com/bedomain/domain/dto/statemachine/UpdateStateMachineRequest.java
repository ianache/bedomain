package com.bedomain.domain.dto.statemachine;

import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.Optional;
import java.util.UUID;

@Data
public class UpdateStateMachineRequest {

    @Size(max = 100, message = "Name must not exceed 100 characters")
    private Optional<String> name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private Optional<String> description;

    // For adding/removing states and transitions
    private java.util.List<CreateStateSpecRequest> addStates;
    private java.util.List<UUID> removeStateIds;

    private java.util.List<CreateTransitionSpecRequest> addTransitions;
    private java.util.List<UUID> removeTransitionIds;
}
