package com.bedomain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateEntityInstanceRequest {

    @NotNull(message = "Entity type ID is required")
    private java.util.UUID entityTypeId;

    private Map<String, Object> attributes;
}
