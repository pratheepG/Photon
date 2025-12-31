package com.photon.identity.onboarding.dto;

import com.photon.identity.onboarding.enums.StructureItemType;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FormStructureMapDto {
    private String referenceId;
    private StructureItemType type;
    private boolean required;
}