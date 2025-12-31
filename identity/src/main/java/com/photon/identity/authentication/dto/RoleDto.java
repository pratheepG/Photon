package com.photon.identity.authentication.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.photon.endpoint.enums.AccessLevel;
import com.photon.identity.authentication.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * DTO for {@link Role}
 */
@Value
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleDto implements Serializable {

    Long id;
    @NotNull(message = "roleId can't be Null")
    @NotEmpty(message = "roleId can't be Empty")
    @NotBlank(message = "roleId can't be Blank")
    String roleId;
    @NotNull(message = "name can't be Null")
    @NotEmpty(message = "name can't be Empty")
    @NotBlank(message = "name can't be Blank")
    String name;
    String description;
    Boolean isActive;
    AccessLevel accessLevel;
    @NotNull(message = "idp can't be Null")
    @NotEmpty(message = "idp can't be Empty")
    @NotBlank(message = "idp can't be Blank")
    String idp;
    Map<String, Set<String>> featureActions;
}