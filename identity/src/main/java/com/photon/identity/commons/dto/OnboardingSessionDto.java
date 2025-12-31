package com.photon.identity.commons.dto;

import com.photon.identity.commons.enums.AuthAdaptor;
import lombok.*;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingSessionDto {
    private AuthAdaptor adaptor;
    private String email;
    private String phone;
    private String countryCode;
}