package com.photon.identity.onboarding.dto;

import lombok.*;

@Builder
@Getter
@Setter
public class GroupFieldMapDto {
    private Long fieldId;
    private boolean isRequired;
}