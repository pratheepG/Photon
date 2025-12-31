package com.photon.identity.onboarding.dto;

import com.photon.identity.onboarding.enums.TbxInputType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Builder
@Getter
@Setter
public class TextField implements FieldConfig {
    private String regex;
    private Integer maxLength;
    private Integer minLength;
    private TbxInputType tbxInputType;
}