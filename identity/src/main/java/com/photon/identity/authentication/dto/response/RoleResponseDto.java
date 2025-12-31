package com.photon.identity.authentication.dto.response;

import com.photon.endpoint.enums.AccessLevel;
import com.photon.identity.authentication.entity.Role;
import lombok.Builder;
import lombok.Value;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

@Value
@Builder
public class RoleResponseDto implements Serializable {
    Long id;
    String roleId;
    String name;
    String description;
    boolean isActive;
    String idp;
    AccessLevel accessLevel;
    Map<String, Set<String>> featureActions;

    public static RoleResponseDto toDto(Role role) {
       return RoleResponseDto.builder()
               .id(role.getId())
               .roleId(role.getRoleId())
               .name(role.getName())
               .description(role.getDescription())
               .idp(role.getIdp())
               .accessLevel(role.getAccessLevel())
               .featureActions(role.getFeatureActions())
               .isActive(role.isActive())
               .build();
    }
}