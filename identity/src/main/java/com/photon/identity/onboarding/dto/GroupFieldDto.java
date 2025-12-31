package com.photon.identity.onboarding.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupFieldDto {
    private boolean required;
    private FieldDto field;
}