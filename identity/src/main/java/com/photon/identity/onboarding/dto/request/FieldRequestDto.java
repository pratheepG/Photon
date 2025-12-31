package com.photon.identity.onboarding.dto.request;

import com.photon.identity.onboarding.enums.FieldType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FieldRequestDto {
    private String name;
    private FieldType type;
    private Object fieldConfig;
}