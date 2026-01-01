package com.photon.endpoint.dto.mapper;

import com.photon.endpoint.dto.*;
import com.photon.endpoint.dto.ActionInfoDto;
import com.photon.endpoint.dto.FeatureInfoDto;
import com.photon.endpoint.dto.ModelDescriptionDto;
import com.photon.endpoint.dto.ModelFieldDto;
import com.photon.endpoint.enums.BaseType;
import com.photon.grpc.endpoint.*;
import com.photon.enums.SecurityLevel;
import lombok.experimental.UtilityClass;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.*;
import java.util.stream.Collectors;

@UtilityClass
public class ProtoToDtoMapper {

    public static EndpointDetailsDto fromProto(EndpointDetails proto) {
        EndpointDetailsDto dto = EndpointDetailsDto.builder()
                .id(emptyToNull(proto.getId()))
                .name(emptyToNull(proto.getName()))
                .clientId(emptyToNull(proto.getClientId()))
                .clientSecret(emptyToNull(proto.getClientSecret()))
                .build();

        if (proto.getFeaturesCount() > 0) {
            Set<FeatureInfoDto> features = proto.getFeaturesList().stream()
                    .map(ProtoToDtoMapper::featureFromProto)
                    .collect(Collectors.toSet());
            dto.setFeatures(features);
        } else {
            dto.setFeatures(Collections.emptySet());
        }

        if (proto.getModelsCount() > 0) {
            Set<ModelDescriptionDto> models = proto.getModelsList().stream()
                    .map(ProtoToDtoMapper::modelFromProto)
                    .collect(Collectors.toSet());
            dto.setModels(models);
        } else {
            dto.setModels(Collections.emptySet());
        }

        return dto;
    }

    private static FeatureInfoDto featureFromProto(FeatureInfo proto) {
        FeatureInfoDto dto = FeatureInfoDto.builder()
                .id(proto.getId().isEmpty() ? null : UUID.fromString(proto.getId()))
                .featureId(emptyToNull(proto.getFeatureId()))
                .path(emptyToNull(proto.getPath()))
                .name(emptyToNull(proto.getName()))
                .description(emptyToNull(proto.getDescription()))
                .moduleName(emptyToNull(proto.getModuleName()))
                .build();

        if (proto.getActionsCount() > 0) {
            Set<ActionInfoDto> actions = proto.getActionsList().stream()
                    .map(ProtoToDtoMapper::actionFromProto)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            dto.setActions(actions);
        } else {
            dto.setActions(Collections.emptySet());
        }
        return dto;
    }

    private static ActionInfoDto actionFromProto(ActionInfo proto) {
        ActionInfoDto dto = ActionInfoDto.builder()
                .id(proto.getId().isEmpty() ? null : UUID.fromString(proto.getId()))
                .actionId(emptyToNull(proto.getActionId()))
                .securityLevel(proto.getSecurityLevel() == com.photon.grpc.endpoint.SecurityLevel.SEC_UNKNOWN ? null : SecurityLevel.valueOf(proto.getSecurityLevel().name()))
                .accessLevel(proto.getAccessLevel() == com.photon.grpc.endpoint.AccessLevel.ACCESS_UNKNOWN ? null : com.photon.endpoint.enums.AccessLevel.valueOf(proto.getAccessLevel().name()))
                .path(emptyToNull(proto.getPath()))
                .name(emptyToNull(proto.getName()))
                .description(emptyToNull(proto.getDescription()))
                .featureId(emptyToNull(proto.getFeatureId()))
                .operationName(emptyToNull(proto.getOperationName()))
                .requestModel(actionModelFromProto(proto.getRequestModel()))
                .responseModel(actionModelFromProto(proto.getResponseModel()))
                .build();

        if(!proto.getRequestMultipartList().isEmpty()) {
            Set<ActionMultipartDto> dtoSet = new LinkedHashSet<>();
            proto.getRequestMultipartList()
                    .forEach(actionMultipart -> {
                        dtoSet.add(actionMultipartFromProto(actionMultipart));
                    });
            dto.setRequestMultipart(dtoSet);
        }

        if(!proto.getRequestParamsList().isEmpty()) {
            Set<ActionParamDto> dtoSet = new LinkedHashSet<>();
            proto.getRequestParamsList()
                    .forEach(actionParam -> {
                        dtoSet.add(actionParamFromProto(actionParam));
                    });
            dto.setRequestParams(dtoSet);
        }

        if (proto.hasUserRoles()) {
            Map<String, Map<Long, String>> userRoles = new HashMap<>();
            proto.getUserRoles().getEntriesMap().forEach((key, longStringMap) -> {
                Map<Long, String> inner = new HashMap<>(longStringMap.getEntriesMap());
                userRoles.put(key, inner);
            });
            dto.setUserRoles(userRoles);
        } else {
            dto.setUserRoles(Collections.emptyMap());
        }

        com.photon.grpc.endpoint.RequestMethod rm = proto.getRequestMethod();
        if (rm != com.photon.grpc.endpoint.RequestMethod.RM_UNKNOWN) {
            try {
                dto.setRequestMethod(RequestMethod.valueOf(rm.name()));
            } catch (IllegalArgumentException ignore) {
            }
        }

        return dto;
    }

    private static ActionModelDto actionModelFromProto(ActionModel proto) {
        ActionModelDto dto = new ActionModelDto();
        dto.setModelId(emptyToNull(proto.getModelId()));
        dto.setKey(emptyToNull(proto.getKey()));
        dto.setCollection(proto.getIsCollection());
        return dto;
    }

    private static ActionMultipartDto actionMultipartFromProto(ActionMultipart proto) {
        ActionMultipartDto dto = new ActionMultipartDto();
        dto.setKey(emptyToNull(proto.getKey()));
        dto.setCollection(proto.getIsCollection());
        return dto;
    }

    private static ActionParamDto actionParamFromProto(ActionParam proto) {
        ActionParamDto dto = new ActionParamDto();
        dto.setKey(emptyToNull(proto.getKey()));
        dto.setType(resolveFieldType(proto.getType()));
        dto.setRequired(proto.getRequired());
        dto.setCollection(proto.getIsCollection());
        return dto;
    }

    private static ModelDescriptionDto modelFromProto(ModelDescription proto) {
        ModelDescriptionDto dto = new ModelDescriptionDto();
        dto.setId(emptyToNull(proto.getId()));
        dto.setName(emptyToNull(proto.getName()));
        if (proto.getFieldsCount() > 0) {
            List<ModelFieldDto> fields = proto.getFieldsList().stream()
                    .map(ProtoToDtoMapper::modelFieldFromProto)
                    .collect(Collectors.toList());
            dto.setFields(fields);
        } else {
            dto.setFields(Collections.emptyList());
        }
        return dto;
    }

    private static ModelFieldDto modelFieldFromProto(ModelField proto) {
        ModelFieldDto dto = new ModelFieldDto();
        dto.setName(emptyToNull(proto.getName()));
        dto.setReferenceType(emptyToNull(proto.getReferenceType()));
        // map BaseType (proto BaseType -> your BaseType enum)
        if (proto.getType() != com.photon.grpc.endpoint.BaseType.BASE_UNKNOWN) {
            try {
                dto.setType(com.photon.endpoint.enums.BaseType.valueOf(proto.getType().name().replace("BASE_", "")));
            } catch (Exception e) {
                // fallback to OBJECT or UNKNOWN
                dto.setType(com.photon.endpoint.enums.BaseType.UNKNOWN);
            }
        } else {
            dto.setType(com.photon.endpoint.enums.BaseType.UNKNOWN);
        }
        return dto;
    }

    private static String emptyToNull(String s) {
        return (s == null || s.isEmpty()) ? null : s;
    }

    private static com.photon.endpoint.enums.BaseType resolveFieldType(com.photon.grpc.endpoint.BaseType proto) {
        if (proto != com.photon.grpc.endpoint.BaseType.BASE_UNKNOWN) {
            try {
                return com.photon.endpoint.enums.BaseType.valueOf(proto.name().replace("BASE_", ""));
            } catch (Exception e) {
                return com.photon.endpoint.enums.BaseType.UNKNOWN;
            }
        } else {
            return com.photon.endpoint.enums.BaseType.UNKNOWN;
        }
    }
}
