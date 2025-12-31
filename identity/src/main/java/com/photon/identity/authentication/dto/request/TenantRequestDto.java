package com.photon.identity.authentication.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Setter
@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantRequestDto {
    @NotNull(message = "tenant name can't be null")
    @NotEmpty(message = "tenant name can't be empty")
    private String name;
    private String description;
    private String parentId;
}