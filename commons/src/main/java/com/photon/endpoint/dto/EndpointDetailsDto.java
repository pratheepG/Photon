package com.photon.endpoint.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.Set;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EndpointDetailsDto {
    private String id;
    private String name;
    private String clientId;
    private String clientSecret;
    private Set<FeatureInfoDto> features;
    private Set<ModelDescriptionDto> models;
}