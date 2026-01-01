package com.photon.console.entity.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.photon.endpoint.dto.ActionModelDto;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;

@Converter
public class ActionModelConverter implements AttributeConverter<ActionModelDto, String> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(ActionModelDto actionModelDtoSet) {
        try {
            return actionModelDtoSet == null ? null : objectMapper.writeValueAsString(actionModelDtoSet);
        } catch (IOException e) {
            throw new RuntimeException("Error converting Set to JSON", e);
        }
    }

    @Override
    public ActionModelDto convertToEntityAttribute(String s) {
        try {
            return s == null ? null : objectMapper.readValue(s, new TypeReference<ActionModelDto>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error converting JSON to Set", e);
        }
    }
}