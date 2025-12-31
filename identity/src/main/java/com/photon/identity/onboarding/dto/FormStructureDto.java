package com.photon.identity.onboarding.dto;

import com.photon.identity.onboarding.enums.StructureItemType;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FormStructureDto {
    private StructureItemType type; // FIELD or GROUP
    private String dtoFieldName;
    private boolean isCollection;
    private boolean required;
    // For Field
    private Long referenceId; // Field ID

    // For Group
    private List<FormStructureDto> data; // Nested FIELDs inside GROUP
}
