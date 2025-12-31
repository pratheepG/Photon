package com.photon.identity.authentication.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class RoleSummary {
    Long id;
    String name;
}