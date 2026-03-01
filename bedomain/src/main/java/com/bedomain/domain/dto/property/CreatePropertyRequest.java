package com.bedomain.domain.dto.property;

import com.bedomain.domain.enums.DataType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreatePropertyRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotNull(message = "Data type is required")
    private DataType dataType;
}
