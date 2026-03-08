package com.bedomain.domain.dto.statemachine;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateStateSpecRequest {

    @NotBlank(message = "State name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotBlank(message = "State type is required")
    private String type; // INITIAL, FINAL, INTERMEDIATE

    @Size(max = 10000, message = "Script must not exceed 10000 characters")
    private String onEnterScript;

    @Size(max = 10000, message = "Script must not exceed 10000 characters")
    private String onExitScript;
}
