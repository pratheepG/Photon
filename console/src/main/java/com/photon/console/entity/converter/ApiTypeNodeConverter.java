package com.photon.console.entity.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.photon.endpoint.dto.ApiTypeNodeDto;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;

@Converter
public class ApiTypeNodeConverter implements AttributeConverter<ApiTypeNodeDto, String> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(ApiTypeNodeDto actionModelDtoSet) {
        try {
            return actionModelDtoSet == null ? null : objectMapper.writeValueAsString(actionModelDtoSet);
        } catch (IOException e) {
            throw new RuntimeException("Error converting Set to JSON", e);
        }
    }

    @Override
    public ApiTypeNodeDto convertToEntityAttribute(String s) {
        try {
            return s == null ? null : objectMapper.readValue(s, new TypeReference<ApiTypeNodeDto>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error converting JSON to Set", e);
        }
    }
}