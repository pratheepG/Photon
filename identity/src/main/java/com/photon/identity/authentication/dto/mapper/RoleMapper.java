package com.photon.identity.authentication.dto.mapper;

import com.photon.identity.authentication.dto.RoleDto;
import com.photon.identity.authentication.entity.Role;

public class RoleMapper {

    public static RoleDto toDto(Role role) {
        if (role == null) {
            return null;
        }

        return new RoleDto(
                role.getId(),
                role.getRoleId(),
                role.getName(),
                role.getDescription(),
                role.isActive(),
                role.getAccessLevel(),
                role.getIdp(),
                role.getFeatureActions()
        );
    }

    public static Role toEntity(RoleDto roleDto) {
        if (roleDto == null) {
            return null;
        }

        Role role = new Role();
        role.setRoleId(roleDto.getRoleId());
        role.setName(roleDto.getName());
        role.setActive(roleDto.getIsActive());
        role.setIdp(roleDto.getIdp());
        role.setDescription(roleDto.getDescription());
        role.setAccessLevel(roleDto.getAccessLevel());

        if (roleDto.getFeatureActions() != null) {
            role.setFeatureActions(roleDto.getFeatureActions());
        }

        return role;
    }

    public static Role partialUpdate(RoleDto roleDto, Role role) {
        if (roleDto == null || role == null) {
            return role;
        }

        if (roleDto.getRoleId() != null) {
            role.setRoleId(roleDto.getRoleId());
        }
        if (roleDto.getName() != null) {
            role.setName(roleDto.getName());
        }
        if (roleDto.getDescription() != null) {
            role.setDescription(roleDto.getDescription());
        }
        if (roleDto.getIdp() != null) {
            role.setIdp(roleDto.getIdp());
        }
        if (roleDto.getFeatureActions() != null) {
            role.setFeatureActions(roleDto.getFeatureActions());
        }
        if (roleDto.getAccessLevel() != null) {
            role.setAccessLevel(roleDto.getAccessLevel());
        }
        if (roleDto.getIsActive() != null) {
            role.setActive(roleDto.getIsActive());
        }

        return role;
    }
}