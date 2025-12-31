package com.photon.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;

import java.util.Map;
import java.util.Set;

@Value
@Builder
public class RoleFeatureActionRequestDto {
    Long id;
    @NotNull(message = "Role id can't be Null")
    @NotEmpty(message = "Role id can't be Empty")
    @NotBlank(message = "Role id can't be Blank")
    String roleId;
    Map<String, Set<String>> featureActions;
}