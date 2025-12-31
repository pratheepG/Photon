package com.photon.identity.authentication.dto.mapper;

import com.photon.identity.authentication.dto.TenantDto;
import com.photon.identity.authentication.dto.request.TenantRequestDto;
import com.photon.identity.authentication.entity.Tenant;

public class TenantMapper {

    /**
     * Converts a Tenant Entity to a TenantDto (for API response).
     * @param tenant The Tenant entity.
     * @return The corresponding TenantDto.
     */
    public static TenantDto toDto(Tenant tenant) {
        if (tenant == null) {
            return null;
        }

        return TenantDto.builder()
                .tenantId(tenant.getTenantId())
                .name(tenant.getName())
                .description(tenant.getDescription())
                .parentTenantId(tenant.getParent() != null ? tenant.getParent().getTenantId() : null)
                .build();
    }

    /**
     * Converts a TenantRequestDto to a Tenant Entity (for creation/update).
     * Note: This does not set the parent Tenant entity itself; that is handled in the Service layer.
     * @param requestDto The Tenant request DTO.
     * @return The corresponding Tenant entity.
     */
    public static Tenant toEntity(TenantRequestDto requestDto) {
        if (requestDto == null) {
            return null;
        }
        Tenant tenant = new Tenant();
        tenant.setName(requestDto.getName());
        tenant.setDescription(requestDto.getDescription());
        return tenant;
    }

    /**
     * Updates an existing Tenant entity with non-null values from a TenantRequestDto.
     * @param requestDto The Tenant request DTO containing updates.
     * @param tenant The existing Tenant entity to update.
     * @return The updated Tenant entity.
     */
    public static Tenant partialUpdate(TenantRequestDto requestDto, Tenant tenant) {
        if (requestDto == null || tenant == null) {
            return tenant;
        }

        if (requestDto.getName() != null) {
            tenant.setName(requestDto.getName());
        }
        if (requestDto.getDescription() != null) {
            tenant.setDescription(requestDto.getDescription());
        }
        return tenant;
    }
}