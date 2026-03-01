package com.bedomain.domain.dto.property;

import com.bedomain.domain.enums.DataType;
import lombok.Data;
import java.util.Optional;

@Data
public class UpdatePropertyRequest {
    private Optional<String> name;
    private Optional<String> description;
    private Optional<DataType> dataType;
}
