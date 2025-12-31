package com.photon.identity.idp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdentityProviderAuthTypeRequestDto {
    private Long id;
    private String name;
    private boolean isActive;
    @NotNull(message = "Identity Provider firstFactor can't be Null")
    @NotEmpty(message = "Identity Provider firstFactor can't be Empty")
    @NotBlank(message = "Identity Provider firstFactor can't be Blank")
    private String firstFactor;
    private Set<String> secondFactors;
    private String identityProviderId;
}