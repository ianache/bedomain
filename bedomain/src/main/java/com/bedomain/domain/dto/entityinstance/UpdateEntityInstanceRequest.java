package com.bedomain.domain.dto.entityinstance;

import lombok.*;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateEntityInstanceRequest {

    private Map<String, Object> attributes;
}
