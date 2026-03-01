package com.bedomain.domain.dto.entitytype;

import lombok.Data;
import java.util.Optional;

@Data
public class UpdateEntityTypeRequest {
    private Optional<String> name;
    private Optional<String> description;
}
