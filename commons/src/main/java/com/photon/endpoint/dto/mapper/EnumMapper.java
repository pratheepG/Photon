package com.photon.endpoint.dto.mapper;


import com.photon.endpoint.enums.AccessLevel;
import com.photon.enums.SecurityLevel;
import org.springframework.web.bind.annotation.RequestMethod;

public final class EnumMapper {

    public static com.photon.grpc.endpoint.SecurityLevel toProto(SecurityLevel s) {
        if (s == null) return com.photon.grpc.endpoint.SecurityLevel.SEC_UNKNOWN;
        return switch (s) {
            case PUBLIC -> com.photon.grpc.endpoint.SecurityLevel.PUBLIC;
            case AUTHENTICATED -> com.photon.grpc.endpoint.SecurityLevel.AUTHENTICATED;
            case ANONYMOUS -> com.photon.grpc.endpoint.SecurityLevel.ANONYMOUS;
            case PRIVATE -> com.photon.grpc.endpoint.SecurityLevel.PRIVATE;
        };
    }

    public static SecurityLevel fromProto(com.photon.grpc.endpoint.SecurityLevel p) {
        if (p == null) return null;
        return switch (p) {
            case PUBLIC -> SecurityLevel.PUBLIC;
            case AUTHENTICATED -> SecurityLevel.AUTHENTICATED;
            case ANONYMOUS -> SecurityLevel.ANONYMOUS;
            case PRIVATE -> SecurityLevel.PRIVATE;
            default -> null;
        };
    }

    public static com.photon.grpc.endpoint.AccessLevel toProto(AccessLevel a) {
        if (a == null) return com.photon.grpc.endpoint.AccessLevel.ACCESS_UNKNOWN;
        return switch (a) {
            case NONE -> com.photon.grpc.endpoint.AccessLevel.NONE;
            case VIEWER -> com.photon.grpc.endpoint.AccessLevel.VIEWER;
            case EDITOR -> com.photon.grpc.endpoint.AccessLevel.EDITOR;
            case OWNER -> com.photon.grpc.endpoint.AccessLevel.OWNER;
            case ADMIN -> com.photon.grpc.endpoint.AccessLevel.ADMIN;
            case TENANT_ADMIN -> com.photon.grpc.endpoint.AccessLevel.TENANT_ADMIN;
        };
    }

    public static AccessLevel fromProto(com.photon.grpc.endpoint.AccessLevel p) {
        if (p == null) return null;
        return switch (p) {
            case NONE -> AccessLevel.NONE;
            case VIEWER -> AccessLevel.VIEWER;
            case EDITOR -> AccessLevel.EDITOR;
            case OWNER -> AccessLevel.OWNER;
            case ADMIN -> AccessLevel.ADMIN;
            case TENANT_ADMIN -> AccessLevel.TENANT_ADMIN;
            default -> null;
        };
    }

    public static com.photon.grpc.endpoint.RequestMethod toProto(RequestMethod rm) {
        if (rm == null) return com.photon.grpc.endpoint.RequestMethod.RM_UNKNOWN;
        return switch (rm) {
            case GET -> com.photon.grpc.endpoint.RequestMethod.GET;
            case HEAD -> com.photon.grpc.endpoint.RequestMethod.HEAD;
            case POST -> com.photon.grpc.endpoint.RequestMethod.POST;
            case PUT -> com.photon.grpc.endpoint.RequestMethod.PUT;
            case PATCH -> com.photon.grpc.endpoint.RequestMethod.PATCH;
            case DELETE -> com.photon.grpc.endpoint.RequestMethod.DELETE;
            case OPTIONS -> com.photon.grpc.endpoint.RequestMethod.OPTIONS;
            case TRACE -> com.photon.grpc.endpoint.RequestMethod.TRACE;
            default -> com.photon.grpc.endpoint.RequestMethod.RM_UNKNOWN;
        };
    }

    public static RequestMethod fromProto(com.photon.grpc.endpoint.RequestMethod p) {
        if (p == null) return null;
        return switch (p) {
            case GET -> RequestMethod.GET;
            case HEAD -> RequestMethod.HEAD;
            case POST -> RequestMethod.POST;
            case PUT -> RequestMethod.PUT;
            case PATCH -> RequestMethod.PATCH;
            case DELETE -> RequestMethod.DELETE;
            case OPTIONS -> RequestMethod.OPTIONS;
            case TRACE -> RequestMethod.TRACE;
            default -> null;
        };
    }
}