package com.bedomain.domain.dto.statemachine;

import com.bedomain.domain.entity.StateSpec;
import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class StateSpecResponse {
    private UUID id;
    private String name;
    private String description;
    private StateSpec.StateType type;
}
