package com.photon.identity.onboarding.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.photon.identity.onboarding.dto.FormElementDto;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FormPreviewDto {
    private Long id;
    private String name;
    private List<FormElementDto> fields;
}