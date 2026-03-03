package com.bedomain.domain.dto.statemachine;

import com.bedomain.domain.entity.StateSpec;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class StateMachineResponse {
    private UUID id;
    private String name;
    private String description;
    private UUID entityTypeId;
    private String entityTypeName;
    private List<StateSpecResponse> states;
    private List<TransitionSpecResponse> transitions;
    private Instant createdAt;
    private String createdBy;
    private Instant updatedAt;
    private String updatedBy;
}
