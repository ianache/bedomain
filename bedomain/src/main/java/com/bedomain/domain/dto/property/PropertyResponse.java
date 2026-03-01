package com.bedomain.domain.dto.property;

import com.bedomain.domain.enums.DataType;
import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class PropertyResponse {
    private UUID id;
    private String name;
    private String description;
    private DataType dataType;
}
