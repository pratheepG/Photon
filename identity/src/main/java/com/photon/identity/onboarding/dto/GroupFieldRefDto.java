package com.photon.identity.onboarding.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupFieldRefDto {
    private String fieldId;
    private boolean required;
}
