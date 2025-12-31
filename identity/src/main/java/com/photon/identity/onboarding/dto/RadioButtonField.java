package com.photon.identity.onboarding.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.photon.identity.onboarding.enums.CheckBoxSelectionType;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RadioButtonField implements FieldConfig {
    private String lookupData;
    @JsonIgnore
    private final CheckBoxSelectionType selectionType = CheckBoxSelectionType.SINGLE_SELECT;
    @JsonIgnore
    private final boolean isCollection = false;
}