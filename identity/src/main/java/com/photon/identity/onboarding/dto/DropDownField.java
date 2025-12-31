package com.photon.identity.onboarding.dto;

import com.photon.identity.onboarding.enums.CheckBoxSelectionType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class DropDownField implements FieldConfig {
    private String lookupData;
    private CheckBoxSelectionType selectionType;
    private boolean isCollection;
}