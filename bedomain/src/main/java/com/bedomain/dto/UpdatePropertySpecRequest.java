package com.bedomain.dto;

import com.bedomain.enums.DataType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePropertySpecRequest {

    private String name;
    private String description;
    private DataType dataType;
}
