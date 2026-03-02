package com.bedomain.domain.dto.entityinstance;

import lombok.*;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntityInstanceResponse {

    private UUID id;
    private UUID entityTypeId;
    private String entityTypeName;
    private Map<String, Object> attributes;
    private Instant createdAt;
    private String createdBy;
    private Instant updatedAt;
    private String updatedBy;
}
