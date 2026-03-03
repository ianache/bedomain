package com.bedomain.domain.dto.statemachine;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TriggerTransitionRequest {

    @NotBlank(message = "Event is required")
    private String event;

    private String reason;
}
