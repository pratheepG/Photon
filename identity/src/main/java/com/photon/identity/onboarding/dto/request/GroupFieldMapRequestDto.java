package com.photon.identity.onboarding.dto.request;

import lombok.Data;

@Data
public class GroupFieldMapRequestDto {
    private Long fieldId;
    private boolean isRequired;
}