package com.photon.identity.idp.dto.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.photon.enums.ExceptionEnum;
import com.photon.exception.ApplicationException;
import com.photon.identity.commons.enums.AuthAdaptor;
import com.photon.identity.idp.dto.*;
import com.photon.identity.idp.entity.AuthType;
import org.springframework.http.HttpStatus;

public class AuthTypeMapper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static AuthTypeDto toDto(AuthType authType) {
        if (authType == null) {
            return null;
        }

        return new AuthTypeDto(
                authType.getId(),
                authType.getName(),
                authType.getDescription(),
                authType.getIsActive(),
                authType.getAuthAdapter(),
                getConfig(authType.getAuthAdapter(), authType.getConfig())
        );
    }

    public static AuthTypeDto toResponseDto(AuthType authType) {
        if (authType == null) {
            return null;
        }

        return new AuthTypeDto(
                authType.getId(),
                authType.getName(),
                authType.getDescription(),
                authType.getIsActive(),
                authType.getAuthAdapter(),
                getConfig(authType.getAuthAdapter(), authType.getConfig())
        );
    }

    public static AuthType toEntity(AuthTypeDto authTypeDto) {
        ObjectMapper mapper = new ObjectMapper();
        if (authTypeDto == null) {
            return null;
        }

        AuthType authType = new AuthType();
        authType.setId(authTypeDto.getId());
        authType.setName(authTypeDto.getName());
        authType.setDescription(authTypeDto.getDescription());
        authType.setIsActive(authTypeDto.getIsActive());
        authType.setAuthAdapter(authTypeDto.getAuthAdapter());
        try {
            authType.setConfig(mapper.writeValueAsString(authTypeDto.getConfig()));
        } catch (JsonProcessingException e) {
            throw new ApplicationException(ExceptionEnum.ERR_1032.getErrorResponseBody(e.getMessage()), HttpStatus.EXPECTATION_FAILED);
        }

        return authType;
    }

    public static AuthType partialUpdate(AuthTypeDto authTypeDto, AuthType authType) {
        ObjectMapper mapper = new ObjectMapper();
        if (authTypeDto == null || authType == null) {
            return authType;
        }

        if (authTypeDto.getId() != null) {
            authType.setId(authTypeDto.getId());
        }
        if (authTypeDto.getName() != null) {
            authType.setName(authTypeDto.getName());
        }
        if (authTypeDto.getDescription() != null) {
            authType.setDescription(authTypeDto.getDescription());
        }
        if (authTypeDto.getIsActive() != null) {
            authType.setIsActive(authTypeDto.getIsActive());
        }
        if (authTypeDto.getAuthAdapter() != null) {
            authType.setAuthAdapter(authTypeDto.getAuthAdapter());
        }
        if (authTypeDto.getConfig() != null) {
            try {
                authType.setConfig(mapper.writeValueAsString(authTypeDto.getConfig()));
            } catch (JsonProcessingException e) {
                throw new ApplicationException(ExceptionEnum.ERR_1032.getErrorResponseBody(e.getMessage()), HttpStatus.EXPECTATION_FAILED);
            }
        }

        return authType;
    }

    private static AuthTypeConfigDto getConfig(AuthAdaptor adaptor, String configStr) {
        try {
            return switch (adaptor) {
                case EML_OTP -> objectMapper.readValue(configStr, EmailAuthConfigDto.class);
                case SMS_OTP -> objectMapper.readValue(configStr, SmsAuthConfigDto.class);
                case STATIC_PWD -> objectMapper.readValue(configStr, StaticPasswordAuthConfigDto.class);
                default ->
                        throw new ApplicationException(ExceptionEnum.ERR_1032.getErrorResponseBody(), HttpStatus.EXPECTATION_FAILED);
            };
        } catch (JsonProcessingException e) {
            throw new ApplicationException(ExceptionEnum.ERR_1032.getErrorResponseBody(e.getMessage()), HttpStatus.EXPECTATION_FAILED);
        }
    }
}