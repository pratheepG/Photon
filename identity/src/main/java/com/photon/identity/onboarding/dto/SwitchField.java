package com.photon.identity.onboarding.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class SwitchField implements FieldConfig{
    private String onValue;
    private String offValue;
}