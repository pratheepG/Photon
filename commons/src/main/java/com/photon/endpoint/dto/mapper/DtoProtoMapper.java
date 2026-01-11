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
                .setSecurityLevel(EnumMapper.toProto(dto.getSecurityLevel()))
                .setAccessLevel(EnumMapper.toProto(dto.getAccessLevel()))
                .setRequestMethod(EnumMapper.toProto(dto.getRequestMethod()));


        /**
         * @Note:- Mapping Request schema
         */
        if (dto.getRequestSchema() != null) {
            b.setRequestSchema(toProto(dto.getRequestSchema()));
        }

        /**
         * @Note:- Mapping Response schema
         */
        if (dto.getResponseSchema() != null) {
            b.setResponseSchema(toProto(dto.getResponseSchema()));
        }

        /**
         * @Note:- Mapping Request param
         */
        if (dto.getRequestParams() != null) {
            dto.getRequestParams().forEach(p -> {
                if (p != null) {
                    b.addRequestParams(toProto(p));
                }
            });
        }

        /**
         * @Note:- Mapping Header param
         */
        if (dto.getRequestHeaders() != null) {
            dto.getRequestHeaders().forEach(p -> {
                if (p != null) {
                    b.addRequestHeaders(toProto(p));
                }
            });
        }

        /**
         * @Note:- Mapping Multipart schema
         */
        if (dto.getMultipartSchema() != null) {
            dto.getMultipartSchema().forEach(m -> {
                if (m != null) {
                    b.addMultipartSchema(toProto(m));
                }
            });
        }

        /**
         * @Note:- Mapping User roles
         */
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

//    private static ApiTypeNode toProto(ApiTypeNodeDto dto) {
//        if (dto == null)
//            return null;
//        return ApiTypeNode.newBuilder()
//                .setModelId(nullToEmpty(dto.getModelId()))
//                .setKey(nullToEmpty(dto.getKey()))
//                .setType(toProtoBaseType(dto.getType()))
//                .setElement(toProto(dto.getElement()))
//                .setMap(toProto(dto.getMap()))
//                .setRequired(dto.isRequired())
//                .build();
//    }

    private static ApiTypeNode toProto(ApiTypeNodeDto dto) {
        if (dto == null) return null;

        ApiTypeNode.Builder b = ApiTypeNode.newBuilder()
                .setKey(nullToEmpty(dto.getKey()))
                .setType(toProtoBaseType(dto.getType()))
                .setRequired(dto.isRequired());

        if (dto.getModelId() != null) {
            b.setModelId(dto.getModelId());
        }

        // ✅ ONLY set when non-null
        if (dto.getElement() != null) {
            b.setElement(toProto(dto.getElement()));
        }

        if (dto.getMap() != null) {
            b.setMap(toProto(dto.getMap()));
        }

        return b.build();
    }


    private static MapNode toProto(MapNodeDto dto) {
        if (dto == null) return null;

        MapNode.Builder b = MapNode.newBuilder();

        if (dto.getKey() != null) {
            b.setKey(toProto(dto.getKey()));
        }

        if (dto.getValue() != null) {
            b.setValue(toProto(dto.getValue()));
        }

        return b.build();
    }

//    private static MapNode toProto(MapNodeDto dto) {
//        if (dto == null)
//            return null;
//        return MapNode.newBuilder()
//                .setKey(toProto(dto.getKey()))
//                .setValue(toProto(dto.getValue()))
//                .build();
//    }

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

    private static com.photon.grpc.endpoint.BaseType toProtoBaseType(com.photon.endpoint.enums.BaseType type) {
        if (type == null) {
            return com.photon.grpc.endpoint.BaseType.BASE_UNKNOWN;
        }

        return switch (type) {
            case STRING   -> com.photon.grpc.endpoint.BaseType.BASE_STRING;
            case INTEGER  -> com.photon.grpc.endpoint.BaseType.BASE_INTEGER;
            case LONG     -> com.photon.grpc.endpoint.BaseType.BASE_LONG;
            case BOOLEAN  -> com.photon.grpc.endpoint.BaseType.BASE_BOOLEAN;
            case FLOAT    -> com.photon.grpc.endpoint.BaseType.BASE_FLOAT;
            case DOUBLE   -> com.photon.grpc.endpoint.BaseType.BASE_DOUBLE;
            case DATE     -> com.photon.grpc.endpoint.BaseType.BASE_DATE;
            case OBJECT   -> com.photon.grpc.endpoint.BaseType.BASE_OBJECT;
            case LIST     -> com.photon.grpc.endpoint.BaseType.BASE_LIST;
            case SET      -> com.photon.grpc.endpoint.BaseType.BASE_SET;
            case MAP      -> com.photon.grpc.endpoint.BaseType.BASE_MAP;
            case DTO      -> com.photon.grpc.endpoint.BaseType.BASE_DTO;
            case MULTIPART-> com.photon.grpc.endpoint.BaseType.BASE_MULTIPART;
            default       -> com.photon.grpc.endpoint.BaseType.BASE_UNKNOWN;
        };
    }

}