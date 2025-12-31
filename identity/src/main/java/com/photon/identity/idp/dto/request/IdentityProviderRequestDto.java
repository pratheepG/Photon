package com.photon.identity.idp.dto.request;

import com.photon.identity.commons.enums.IdentityProviderType;
import io.jsonwebtoken.SignatureAlgorithm;
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
public class IdentityProviderRequestDto {
    @NotNull(message = "Identity Provider id can't be Null")
    @NotEmpty(message = "Identity Provider id can't be Empty")
    @NotBlank(message = "Identity Provider id can't be Blank")
    private String id;
    private String name;
    private String description;
    private Boolean isActive;
    private Long idpDefaultRole;
    private IdentityProviderType identityProviderType;
    private SignatureAlgorithm signatureAlgorithm;
    private Long certificate;
    private Integer sessionTimeoutMinutes;
    private Integer sessionIdleTimeoutMinutes;
    private Integer activeSessions;
    private Integer refreshTokenSessionTimeoutMinutes;
    private Set<IdentityProviderAuthTypeRequestDto> identityProviderAuthTypes;
    private Boolean isMfaEnabled;
    private Boolean isMfaRequiredForEveryLogin;
    private Boolean isMfaConditionCheckEnabled;
    private Boolean includeX509InJwt;
    private Long lastValidateMfaExpiresInMinutes;
    private Long mfaCondition;
}