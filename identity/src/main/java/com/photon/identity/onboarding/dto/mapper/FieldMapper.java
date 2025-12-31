package com.photon.identity.onboarding.dto.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.photon.enums.ExceptionEnum;
import com.photon.exception.ApplicationException;
import com.photon.identity.onboarding.dto.FieldDto;
import com.photon.identity.onboarding.entity.Field;
import org.springframework.http.HttpStatus;

public class FieldMapper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static FieldDto toDto(Field field) {
        Object config = null;
        try {
            if (field.getField() != null) {
                config = objectMapper.readValue(field.getField(), Object.class);
            }
        } catch (JsonProcessingException e) {
            throw new ApplicationException(
                    ExceptionEnum.ERR_1003.getErrorResponseBody("Invalid field config JSON"),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

        return FieldDto.builder()
                .id(field.getId())
                .name(field.getName())
                .type(field.getType())
                .isCollection(field.isCollection())
                .config(config)
                .build();
    }

    public static Field toEntity(FieldDto dto) {
        String json;
        try {
            json = objectMapper.writeValueAsString(dto.getConfig());
        } catch (JsonProcessingException e) {
            throw new ApplicationException(
                    ExceptionEnum.ERR_1003.getErrorResponseBody("Invalid field config data"),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

        return Field.builder()
                .id(dto.getId())
                .name(dto.getName())
                .type(dto.getType())
                .isCollection(dto.isCollection())
                .field(json)
                .build();
    }
}