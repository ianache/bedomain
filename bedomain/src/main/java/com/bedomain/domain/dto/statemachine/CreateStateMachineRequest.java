package com.bedomain.domain.dto.statemachine;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class CreateStateMachineRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotNull(message = "Entity type ID is required")
    private UUID entityTypeId;

    private List<CreateStateSpecRequest> states;

    private List<CreateTransitionSpecRequest> transitions;
}
