package com.photon.console.entity.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.photon.endpoint.dto.ActionMultipartDto;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;
import java.util.Set;

@Converter
public class ActionMultipartConverter implements AttributeConverter<Set<ActionMultipartDto>, String> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Set<ActionMultipartDto> actionMultipartDtos) {
        try {
            return actionMultipartDtos == null ? null : objectMapper.writeValueAsString(actionMultipartDtos);
        } catch (IOException e) {
            throw new RuntimeException("Error converting Set to JSON", e);
        }
    }

    @Override
    public Set<ActionMultipartDto> convertToEntityAttribute(String s) {
        try {
            return s == null ? null : objectMapper.readValue(s, new TypeReference<Set<ActionMultipartDto>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error converting JSON to Set", e);
        }
    }
}
