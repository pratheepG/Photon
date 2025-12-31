package com.photon.identity.onboarding.dto;

import com.photon.identity.onboarding.enums.FieldType;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldDto {
    private Long id;
    private String name;
    private FieldType type;
    private boolean isCollection;
    private Object config; // actual config object (deserialized)
}