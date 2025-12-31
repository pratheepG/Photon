package com.photon.identity.onboarding.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.photon.identity.onboarding.enums.StructureItemType;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FormElementDto {
    private StructureItemType type;
    private boolean isRequired;
    private boolean isCollection;
    private String dtoFieldName;
    private FieldDto field;
    private GroupDto group;
}