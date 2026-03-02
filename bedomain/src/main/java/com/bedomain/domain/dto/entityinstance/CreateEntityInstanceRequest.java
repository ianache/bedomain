package com.bedomain.domain.dto.entityinstance;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateEntityInstanceRequest {

    @NotNull(message = "Entity type ID is required")
    private UUID entityTypeId;

    private Map<String, Object> attributes;
}
