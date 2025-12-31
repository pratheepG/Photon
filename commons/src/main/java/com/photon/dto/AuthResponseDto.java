package com.photon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.Map;
import java.util.Set;

@Setter
@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponseDto {
    private String idpCode;
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiryDuration;
    private Map<String,Object> userAttributes;
    private Map<String,Object> securityAttributes;

    private String message;
    private String authType;
    private boolean isAuthenticated;
    private boolean isMfaEnabled;
    private boolean isTempCredential;
    private String securityKey;
    private Set<String> secondFactors;
}