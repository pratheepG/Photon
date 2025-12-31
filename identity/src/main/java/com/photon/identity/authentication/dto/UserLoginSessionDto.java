package com.photon.identity.authentication.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class UserLoginSessionDto {
    private String idpCode;
    private UserLoginHistoryDto userLogin;
    private Set<String> requiredSecondFactors = new HashSet<>();
}