package com.photon.console.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.photon.endpoint.dto.ActionInfoDto;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;


@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateActionInfoRequestDto {

    UUID id;
    ActionInfoDto actionInfo;

}