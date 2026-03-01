package com.bedomain.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateEntityTypeRequest {

    private String name;
    private String description;
}
