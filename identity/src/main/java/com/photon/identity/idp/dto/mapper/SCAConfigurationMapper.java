package com.photon.identity.idp.dto.mapper;

import com.photon.identity.idp.dto.SCAConfigurationDto;
import com.photon.identity.idp.entity.AuthType;
import com.photon.identity.idp.entity.SCAConfiguration;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class SCAConfigurationMapper {

    public static SCAConfiguration toEntity(SCAConfigurationDto dto, AuthType firstFactor, Set<AuthType> secondFactors) {
        if (dto == null) {
            return null;
        }

        SCAConfiguration entity = new SCAConfiguration();
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setActive(dto.isActive());
        entity.setPreLogin(dto.isPreLogin());
        entity.setFirstFactor(firstFactor);
        entity.setSecondFactors(secondFactors);

        return entity;
    }

    // Convert entity to response DTO
    public static SCAConfigurationDto toDto(SCAConfiguration entity) {
        if (entity == null) {
            return null;
        }

        return new SCAConfigurationDto(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.isActive(),
                entity.isPreLogin(),
                entity.getFirstFactor().getId(),
                entity.getSecondFactors().stream().map(AuthType::getId).collect(Collectors.toSet())
        );
    }


    public static SCAConfiguration partialUpdate(SCAConfigurationDto dto, SCAConfiguration entity, AuthType firstFactor, Set<AuthType> secondFactors) {
        if (dto == null || entity == null) {
            return entity;
        }

        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
        entity.setActive(dto.isActive());
        entity.setPreLogin(dto.isPreLogin());

        if (!Objects.isNull(firstFactor)) {
            entity.setFirstFactor(firstFactor);
        }
        if (!Objects.isNull(secondFactors)) {
            entity.setSecondFactors(secondFactors);
        }

        return entity;
    }
}
