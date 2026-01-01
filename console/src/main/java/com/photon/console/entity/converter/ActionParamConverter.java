package com.photon.console.entity.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.photon.endpoint.dto.ActionParamDto;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;
import java.util.Set;

@Converter
public class ActionParamConverter implements AttributeConverter<Set<ActionParamDto>, String> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Set<ActionParamDto> actionParamDto) {
        try {
            return actionParamDto == null ? null : objectMapper.writeValueAsString(actionParamDto);
        } catch (IOException e) {
            throw new RuntimeException("Error converting Set to JSON", e);
        }
    }

    @Override
    public Set<ActionParamDto> convertToEntityAttribute(String s) {
        try {
            return s == null ? null : objectMapper.readValue(s, new TypeReference<Set<ActionParamDto>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error converting JSON to Set", e);
        }
    }
}
