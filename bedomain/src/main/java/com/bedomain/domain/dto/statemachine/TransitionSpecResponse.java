package com.bedomain.domain.dto.statemachine;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class TransitionSpecResponse {
    private UUID id;
    private String event;
    private String description;
    private String fromStateName;
    private String toStateName;
}
