package com.photon.identity.onboarding.dto.request;

import com.photon.identity.onboarding.dto.FormStructureDto;
import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class FormRequestDto {
    private String name;
    private List<FormStructureDto> fields;
}