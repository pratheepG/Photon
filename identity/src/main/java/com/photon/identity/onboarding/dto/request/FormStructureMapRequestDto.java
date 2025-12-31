package com.photon.identity.onboarding.dto.request;

import com.photon.identity.onboarding.enums.StructureItemType;
import lombok.Data;

@Data
public class FormStructureMapRequestDto {
    private Long referenceId;
    private StructureItemType type;
    private boolean required;
}