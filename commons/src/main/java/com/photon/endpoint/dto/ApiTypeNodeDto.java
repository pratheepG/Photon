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
public class ApiTypeNodeDto {
    private String key;

    /**
     * Note: STRING, DTO, LIST, MAP, etc.
     */
    private BaseType type;

    private String modelId;

    /**
     * Note: LIST / SET element
     */
    private ApiTypeNodeDto element;

    /**
     * Note: key-value structure
     */
    private MapNodeDto map;

    private boolean required;
}