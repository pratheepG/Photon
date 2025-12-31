package com.photon.identity.idp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class IdentityProviderAuthTypeDto {
    private Long id;
    private String name;
    private boolean isActive;
    @NotNull(message = "Identity Provider firstFactor can't be Null")
    @NotEmpty(message = "Identity Provider firstFactor can't be Empty")
    @NotBlank(message = "Identity Provider firstFactor can't be Blank")
    private AuthTypeDto firstFactor;
    private Set<AuthTypeDto> secondFactors;
    private String identityProviderId;

    /**
     * Validates that the firstFactor is not present in secondFactors.
     *
     * @return true if valid, false otherwise
     */
    public boolean isValidAuthTypeSelection() {
        if (firstFactor == null || secondFactors == null || secondFactors.isEmpty()) {
            return true;
        }
        return secondFactors.stream().noneMatch(authType -> authType.getId().equals(firstFactor.getId()));
    }

    /**
     * Returns validation errors if any.
     *
     * @return validation message or null if valid.
     */
    public String validate() {
        if (!isValidAuthTypeSelection()) {
            return "The firstFactor cannot be present in secondFactors.";
        }
        return null;
    }
}