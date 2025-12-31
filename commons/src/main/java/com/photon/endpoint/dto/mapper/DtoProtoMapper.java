package com.photon.endpoint.dto.mapper;

import com.photon.endpoint.dto.*;
import com.photon.grpc.endpoint.*;

public final class DtoProtoMapper {

    public static EndpointDetails toProto(EndpointDetailsDto dto) {
        EndpointDetails.Builder b = EndpointDetails.newBuilder()
                .setId(nullToEmpty(dto.getId()))
                .setName(nullToEmpty(dto.getName()))
                .setClientId(nullToEmpty(dto.getClientId()))
                .setClientSecret(nullToEmpty(dto.getClientSecret()));

        if (dto.getFeatures() != null) {
            dto.getFeatures().forEach(f -> b.addFeatures(toProto(f)));
        }
        if (dto.getModels() != null) {
            dto.getModels().forEach(m -> b.addModels(toProto(m)));
        }
        return b.build();
    }

    private static FeatureInfo toProto(FeatureInfoDto dto) {
        FeatureInfo.Builder b = FeatureInfo.newBuilder()
                .setId(nullToEmpty(dto.getId() == null ? null : dto.getId().toString()))
                .setFeatureId(nullToEmpty(dto.getFeatureId()))
                .setPath(nullToEmpty(dto.getPath()))
                .setName(nullToEmpty(dto.getName()))
                .setDescription(nullToEmpty(dto.getDescription()))
                .setModuleName(nullToEmpty(dto.getModuleName()));

        if (dto.getActions() != null) {
            dto.getActions().forEach(a -> b.addActions(toProto(a)));
        }
        return b.build();
    }

    private static ActionInfo toProto(ActionInfoDto dto) {
        ActionInfo.Builder b = ActionInfo.newBuilder()
                .setId(nullToEmpty(dto.getId() == null ? null : dto.getId().toString()))
                .setActionId(nullToEmpty(dto.getActionId()))
                .setPath(nullToEmpty(dto.getPath()))
                .setName(nullToEmpty(dto.getName()))
                .setDescription(nullToEmpty(dto.getDescription()))
                .setFeatureId(nullToEmpty(dto.getFeatureId()))
                .setOperationName(nullToEmpty(dto.getOperationName()))
                .setRequestBodyModelId(nullToEmpty(dto.getRequestBodyModelId()))
                .setIsRequestBodyCollection(dto.isRequestBodyCollection())
                .setResponseBodyModelId(nullToEmpty(dto.getResponseBodyModelId()))
                .setSecurityLevel(EnumMapper.toProto(dto.getSecurityLevel()))
                .setAccessLevel(EnumMapper.toProto(dto.getAccessLevel()))
                .setRequestMethod(EnumMapper.toProto(dto.getRequestMethod()))
                .setIsResponseBodyCollection(dto.isResponseBodyCollection());

        if (dto.getUserRoles() != null) {
            UserRolesMap.Builder urm = UserRolesMap.newBuilder();
            dto.getUserRoles().forEach((k, inner) -> {
                LongStringMap.Builder lsm = LongStringMap.newBuilder();
                if (inner != null) {
                    inner.forEach(lsm::putEntries);
                }
                urm.putEntries(k, lsm.build());
            });
            b.setUserRoles(urm.build());
        }

        return b.build();
    }

    private static ModelDescription toProto(ModelDescriptionDto dto) {
        ModelDescription.Builder b = ModelDescription.newBuilder()
                .setId(nullToEmpty(dto.getId()))
                .setName(nullToEmpty(dto.getName()));
        if (dto.getFields() != null) {
            dto.getFields().forEach(f -> b.addFields(toProto(f)));
        }
        return b.build();
    }

    private static ModelField toProto(ModelFieldDto dto) {
        return ModelField.newBuilder()
                .setName(nullToEmpty(dto.getName()))
                .setType(dto.getType() == null ? BaseType.BASE_UNKNOWN : BaseType.valueOf("BASE_" + dto.getType().name()))
                .setReferenceType(nullToEmpty(dto.getReferenceType()))
                .build();
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}