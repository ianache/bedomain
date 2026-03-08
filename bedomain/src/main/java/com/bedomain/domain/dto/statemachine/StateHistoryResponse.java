package com.bedomain.domain.dto.statemachine;

import com.bedomain.domain.dto.entityinstance.EntityInstanceResponse;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StateHistoryResponse {

    private UUID id;
    private String fromState;
    private String toState;
    private String event;
    private String triggeredBy;
    private Instant timestamp;
    private EntityInstanceResponse currentSnapshot;
    private boolean hookExecuted;
    private String hookType;
    private String hookScriptHash;
    private String hookError;
}
