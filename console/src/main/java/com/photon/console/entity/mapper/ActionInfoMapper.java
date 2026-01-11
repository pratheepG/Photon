package com.photon.console.entity.mapper;

import com.photon.console.entity.ActionInfo;
import com.photon.endpoint.dto.ActionInfoDto;
import com.photon.endpoint.enums.BaseType;

public class ActionInfoMapper {

    public static ActionInfoDto toDto(ActionInfo actionInfo) {
        if (actionInfo == null) {
            return null;
        }

        return ActionInfoDto.builder()
                .id(actionInfo.getId())
                .actionId(actionInfo.getActionId())
                .featureId(actionInfo.getFeatureId())
                .name(actionInfo.getName())
                .path(actionInfo.getPath())
                .securityLevel(actionInfo.getSecurityLevel())
                .accessLevel(actionInfo.getAccessLevel())
                .requestMethod(actionInfo.getRequestMethod())
                .description(actionInfo.getDescription())
                .userRoles(actionInfo.getUserRoles())
                .multipartSchema(actionInfo.getMultipartSchema())
                .requestSchema(actionInfo.getRequestSchema())
                .requestParams(actionInfo.getRequestParams())
                .responseSchema(actionInfo.getResponseSchema())
                .requestHeaders(actionInfo.getRequestHeaders())
                .operationName(actionInfo.getOperationName())
                .build();
    }

    public static ActionInfo toEntity(ActionInfoDto actionInfoDto) {
        if (actionInfoDto == null) {
            return null;
        }

        ActionInfo a = ActionInfo.builder()
                .id(actionInfoDto.getId())
                .actionId(actionInfoDto.getActionId())
                .name(actionInfoDto.getName())
                .path(actionInfoDto.getPath())
                .securityLevel(actionInfoDto.getSecurityLevel())
                .accessLevel(actionInfoDto.getAccessLevel())
                .requestMethod(actionInfoDto.getRequestMethod())
                .description(actionInfoDto.getDescription())
                .featureId(actionInfoDto.getFeatureId())
                .userRoles(actionInfoDto.getUserRoles())
                .multipartSchema(actionInfoDto.getMultipartSchema())
                .requestParams(actionInfoDto.getRequestParams())
                .requestHeaders(actionInfoDto.getRequestHeaders())
                .operationName(actionInfoDto.getOperationName())
                .build();
        if(!actionInfoDto.getRequestSchema().getType().equals(BaseType.UNKNOWN))
            a.setRequestSchema(actionInfoDto.getRequestSchema());
        if(!actionInfoDto.getResponseSchema().getType().equals(BaseType.UNKNOWN))
            a.setResponseSchema(actionInfoDto.getResponseSchema());
        return a;
    }

    public static ActionInfo updateEntityFromDto(ActionInfoDto actionInfoDto, ActionInfo actionInfo) {
        if (actionInfoDto == null || actionInfo == null) {
            return null;
        }

        if (actionInfoDto.getSecurityLevel() != null) {
            actionInfo.setSecurityLevel(actionInfoDto.getSecurityLevel());
        }
        if (actionInfoDto.getAccessLevel() != null) {
            actionInfo.setAccessLevel(actionInfoDto.getAccessLevel());
        }
        if (actionInfoDto.getUserRoles() != null) {
            actionInfo.setUserRoles(actionInfoDto.getUserRoles());
        }

        return actionInfo;
    }
}