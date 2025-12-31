package com.photon.identity.idp.dto.mapper;

import com.photon.identity.idp.dto.IdentityProviderAuthTypeDto;
import com.photon.identity.idp.entity.IdentityProviderAuthType;

import java.util.stream.Collectors;

public class IdentityProviderAuthTypeMapper {

    public static IdentityProviderAuthTypeDto toDto(IdentityProviderAuthType entity) {
        if (entity == null) {
            return null;
        }

        return IdentityProviderAuthTypeDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .isActive(entity.isActive())
                .firstFactor(AuthTypeMapper.toDto(entity.getFirstFactor()))
                .secondFactors(entity.getSecondFactors() != null
                        ? entity.getSecondFactors().stream()
                        .map(AuthTypeMapper::toDto)
                        .collect(Collectors.toSet())
                        : null)
                .identityProviderId(entity.getIdentityProvider() != null ? entity.getIdentityProvider().getId() : null)
                .build();
    }

    public static IdentityProviderAuthType toEntity(IdentityProviderAuthTypeDto dto) {
        if (dto == null) {
            return null;
        }

        IdentityProviderAuthType entity = new IdentityProviderAuthType();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setActive(dto.isActive());
        entity.setFirstFactor(AuthTypeMapper.toEntity(dto.getFirstFactor()));

        if (dto.getSecondFactors() != null) {
            entity.setSecondFactors(dto.getSecondFactors().stream()
                    .map(AuthTypeMapper::toEntity)
                    .collect(Collectors.toSet()));
        }

        // Note: IdentityProvider should be set explicitly via service/repository layer to avoid fetching issues
        return entity;
    }

    public static IdentityProviderAuthType partialUpdate(IdentityProviderAuthTypeDto dto, IdentityProviderAuthType entity) {
        if (dto == null || entity == null) {
            return entity;
        }

        if (dto.getId() != null) {
            entity.setId(dto.getId());
        }
        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }
        entity.setActive(dto.isActive());
        if (dto.getFirstFactor() != null) {
            entity.setFirstFactor(AuthTypeMapper.toEntity(dto.getFirstFactor()));
        }
        if (dto.getSecondFactors() != null) {
            entity.setSecondFactors(dto.getSecondFactors().stream()
                    .map(AuthTypeMapper::toEntity)
                    .collect(Collectors.toSet()));
        }

        return entity;
    }
}