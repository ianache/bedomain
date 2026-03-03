package com.bedomain.domain.dto.statemachine;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateTransitionSpecRequest {

    @NotBlank(message = "Event is required")
    @Size(max = 100, message = "Event must not exceed 100 characters")
    private String event;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotBlank(message = "From state is required")
    private String fromStateName;

    @NotBlank(message = "To state is required")
    private String toStateName;
}
