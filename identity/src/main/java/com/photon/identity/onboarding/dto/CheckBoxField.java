package com.photon.identity.onboarding.dto;

import lombok.*;


@Builder
@Getter
@Setter
public class CheckBoxField implements FieldConfig {
    private String lookupData;
    private int minSelection;
    private int maxSelection;
    private boolean isCollection;
}