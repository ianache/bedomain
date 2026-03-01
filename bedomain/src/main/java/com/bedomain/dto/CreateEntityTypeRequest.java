package com.bedomain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateEntityTypeRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;
}
