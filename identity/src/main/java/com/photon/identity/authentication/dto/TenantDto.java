package com.photon.identity.authentication.dto;

import lombok.*;

import java.util.UUID;

@Setter
@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantDto {
    private UUID tenantId;
    private String name;
    private String description;
    private UUID parentTenantId;
}