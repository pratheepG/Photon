package com.photon.console.entity.mapper;

import com.photon.console.entity.Model;
import com.photon.endpoint.dto.ModelDescriptionDto;
import com.photon.endpoint.dto.ModelFieldDto;
import com.photon.endpoint.enums.BaseType;

import java.util.*;
import java.util.stream.Collectors;

public class ModelMapper {

    public static ModelDescriptionDto toDto(Model model) {
        if (model == null) {
            return null;
        }

        List<ModelFieldDto> fields = model.getFields().stream()
                .map(fieldMap -> {
                    ModelFieldDto fieldDto = new ModelFieldDto();
                    fieldDto.setName(fieldMap.getOrDefault("name", ""));
                    fieldDto.setReferenceType(fieldMap.get("referenceType"));
                    fieldDto.setType(BaseType.valueOf(fieldMap.getOrDefault("type", "STRING"))); // default to STRING
                    return fieldDto;
                })
                .collect(Collectors.toList());

        ModelDescriptionDto dto = new ModelDescriptionDto();
        dto.setId(model.getModelId());
        dto.setName(model.getName());
        dto.setFields(fields);

        return dto;
    }

    public static Model toEntity(ModelDescriptionDto dto) {
        if (dto == null) {
            return null;
        }

        Set<Map<String, String>> fields = dto.getFields().stream()
                .map(fieldDto -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("name", fieldDto.getName());
                    map.put("type", fieldDto.getType().name());
                    if (fieldDto.getReferenceType() != null) {
                        map.put("referenceType", fieldDto.getReferenceType());
                    }
                    return map;
                })
                .collect(Collectors.toSet());

        return Model.builder()
                .modelId(dto.getId())
                .name(dto.getName())
                .fields(fields)
                .build();
    }
}
