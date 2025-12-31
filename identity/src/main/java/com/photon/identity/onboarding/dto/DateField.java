package com.photon.identity.onboarding.dto;

import com.photon.identity.onboarding.enums.DateFormat;
import lombok.*;

@Builder
@Getter
@Setter
public class DateField implements FieldConfig {
    private DateFormat dateFormat;
    private boolean isCollection;
}