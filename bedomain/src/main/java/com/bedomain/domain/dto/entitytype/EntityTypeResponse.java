package com.bedomain.domain.dto.entitytype;

import com.bedomain.domain.dto.property.PropertyResponse;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class EntityTypeResponse {
    private UUID id;
    private String name;
    private String description;
    private List<PropertyResponse> properties;
    private Instant createdAt;
    private String createdBy;
}
