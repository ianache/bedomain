package com.bedomain.event;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class EntityEvent {
    private UUID entityId;
    private String entityType;
    private Map<String, Object> attributes;
    private String previousState;
    private String newState;
    private Instant timestamp;
    private String user;
}
