package com.photon.identity.onboarding.dto.response;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FormListItemDto {
    private Long id;
    private String name;
}