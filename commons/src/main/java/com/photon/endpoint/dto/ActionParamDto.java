package com.photon.endpoint.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.photon.endpoint.enums.BaseType;
import lombok.*;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActionParamDto {
    private String key;
    private BaseType type;
    private boolean isCollection;
}