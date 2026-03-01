package com.bedomain.dto;

import com.bedomain.enums.DataType;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertySpecResponse {

    private UUID id;
    private UUID entityTypeId;
    private String name;
    private String description;
    private DataType dataType;
    private Instant createdAt;
    private String createdBy;
    private Instant updatedAt;
    private String updatedBy;
}
