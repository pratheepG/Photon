package com.photon.identity.idp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.photon.identity.commons.enums.IdentityProviderType;
import com.photon.identity.idp.entity.IdentityProvider;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serializable;
import java.util.Set;

/**
 * DTO for {@link IdentityProvider}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IdentityProviderDto implements Serializable {
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
    private CertificateDto certificate;
    private Integer sessionTimeoutMinutes;
    private Integer sessionIdleTimeoutMinutes;
    private Integer activeSessions;
    private Integer refreshTokenSessionTimeoutMinutes;
    private Set<IdentityProviderAuthTypeDto> identityProviderAuthTypes;
    private Boolean isMfaEnabled;
    private Boolean isMfaRequiredForEveryLogin;
    private Boolean isMfaConditionCheckEnabled;
    private Boolean includeX509InJwt;
    private Long lastValidateMfaExpiresInMinutes;
    private MFAConditionSetDto mfaCondition;

    public IdentityProviderDto(String id, String name, String description, Boolean isActive) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.isActive = isActive;
    }
}