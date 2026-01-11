package com.photon.endpoint.dto.mapper;

import com.photon.endpoint.dto.*;
import com.photon.endpoint.dto.ActionInfoDto;
import com.photon.endpoint.dto.FeatureInfoDto;
import com.photon.endpoint.dto.ModelDescriptionDto;
import com.photon.endpoint.dto.ModelFieldDto;
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
                .requestSchema(apiTypeNodeFromProto(proto.getRequestSchema()))
                .responseSchema(apiTypeNodeFromProto(proto.getResponseSchema()))
                .build();

        /**
         * @Note:- Mapping Multipart schema
         */
        if(!proto.getMultipartSchemaList().isEmpty()) {
            Set<ApiTypeNodeDto> dtoSet = new LinkedHashSet<>();
            proto.getMultipartSchemaList().forEach(n -> dtoSet.add(apiTypeNodeFromProto(n)));
            dto.setMultipartSchema(dtoSet);
        }

        /**
         * @Note:- Mapping Header param
         */
        if(!proto.getRequestHeadersList().isEmpty()) {
            Set<ApiTypeNodeDto> dtoSet = new LinkedHashSet<>();
            proto.getRequestHeadersList().forEach(n -> dtoSet.add(apiTypeNodeFromProto(n)));
            dto.setRequestHeaders(dtoSet);
        }

        /**
         * @Note:- Mapping Request param
         */
        if(!proto.getRequestParamsList().isEmpty()) {
            Set<ApiTypeNodeDto> dtoSet = new LinkedHashSet<>();
            proto.getRequestParamsList().forEach(n -> dtoSet.add(apiTypeNodeFromProto(n)));
            dto.setRequestParams(dtoSet);
        }

        /**
         * @Note:- Mapping User roles
         */
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

//    private static ApiTypeNodeDto apiTypeNodeFromProto(ApiTypeNode proto) {
//        if (proto == null)
//            return null;
//        ApiTypeNodeDto dto = new ApiTypeNodeDto();
//        dto.setModelId(emptyToNull(proto.getModelId()));
//        dto.setKey(emptyToNull(proto.getKey()));
//        dto.setType(resolveFieldType(proto.getType()));
//        dto.setRequired(proto.getRequired());
//        dto.setMap(mapNodeFromProto(proto.getMap()));
//        dto.setElement(apiTypeNodeFromProto(proto.getElement()));
//        return dto;
//    }

    private static ApiTypeNodeDto apiTypeNodeFromProto(ApiTypeNode proto) {
        if (proto == null) return null;

        com.photon.endpoint.enums.BaseType type = fromProtoBaseType(proto.getType());

        ApiTypeNodeDto.ApiTypeNodeDtoBuilder b = ApiTypeNodeDto.builder()
                .key(emptyToNull(proto.getKey()))
                .type(type)
                .modelId(emptyToNull(proto.getModelId()))
                .required(proto.getRequired());

        // 🛑 HARD STOP — no recursion beyond these
        switch (type) {
            case STRING:
            case INTEGER:
            case LONG:
            case BOOLEAN:
            case FLOAT:
            case DOUBLE:
            case DATE:
            case OBJECT:
            case MULTIPART:
            case UNKNOWN:
                return b.build();
        }

        // ✅ LIST / SET
        if ((type == com.photon.endpoint.enums.BaseType.LIST || type == com.photon.endpoint.enums.BaseType.SET)
                && proto.hasElement()
                && proto.getElement().getType() != com.photon.grpc.endpoint.BaseType.BASE_UNKNOWN) {

            ApiTypeNodeDto element = apiTypeNodeFromProto(proto.getElement());
            if (element != null) {
                b.element(element);
            }
        }

        // DTO
        if ((type == com.photon.endpoint.enums.BaseType.DTO)
                && proto.hasElement()
                && proto.getElement().getType() != com.photon.grpc.endpoint.BaseType.BASE_UNKNOWN) {

            ApiTypeNodeDto element = apiTypeNodeFromProto(proto.getElement());
            if (element != null) {
                b.element(element);
            }
        }

        // ✅ MAP
        if (type == com.photon.endpoint.enums.BaseType.MAP && proto.hasMap()) {
            MapNodeDto map = mapNodeFromProto(proto.getMap());
            if (map != null) {
                b.map(map);
            }
        }

        return b.build();
    }

//    private static MapNodeDto mapNodeFromProto(MapNode proto) {
//        if (proto == null)
//            return null;
//        MapNodeDto dto = new MapNodeDto();
//        dto.setKey(apiTypeNodeFromProto(proto.getKey()));
//        dto.setValue(apiTypeNodeFromProto(proto.getValue()));
//        return dto;
//    }

//    private static MapNodeDto mapNodeFromProto(MapNode proto) {
//        if (proto == null) return null;
//
//        MapNodeDto.MapNodeDtoBuilder b = MapNodeDto.builder();
//
//        if (proto.hasKey()) {
//            b.key(apiTypeNodeFromProto(proto.getKey()));
//        }
//
//        if (proto.hasValue()) {
//            b.value(apiTypeNodeFromProto(proto.getValue()));
//        }
//
//        return b.build();
//    }

    private static MapNodeDto mapNodeFromProto(MapNode proto) {
        if (proto == null) return null;

        ApiTypeNodeDto key = null;
        ApiTypeNodeDto value = null;

        if (proto.hasKey() && proto.getKey().getType() != com.photon.grpc.endpoint.BaseType.BASE_UNKNOWN) {
            key = apiTypeNodeFromProto(proto.getKey());
        }

        if (proto.hasValue() && proto.getValue().getType() != com.photon.grpc.endpoint.BaseType.BASE_UNKNOWN) {
            value = apiTypeNodeFromProto(proto.getValue());
        }

        if (key == null && value == null) {
            return null;
        }

        return MapNodeDto.builder()
                .key(key)
                .value(value)
                .build();
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

    private static com.photon.endpoint.enums.BaseType fromProtoBaseType(com.photon.grpc.endpoint.BaseType proto) {
        if (proto == null || proto == com.photon.grpc.endpoint.BaseType.BASE_UNKNOWN) {
            return com.photon.endpoint.enums.BaseType.UNKNOWN;
        }

        try {
            return com.photon.endpoint.enums.BaseType.valueOf(proto.name().replace("BASE_", ""));
        } catch (Exception e) {
            return com.photon.endpoint.enums.BaseType.UNKNOWN;
        }
    }
}
