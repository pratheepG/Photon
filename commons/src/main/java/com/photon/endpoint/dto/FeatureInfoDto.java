package com.photon.endpoint.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FeatureInfoDto {
    private UUID id;
    private String featureId;
    private String path;
    private String name;
    private String description;
    private Set<ActionInfoDto> actions;
    private EndpointDetailsDto endpointDetails;

    private String moduleName;
}