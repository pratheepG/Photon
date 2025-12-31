package com.photon.identity.authentication.dto.request;

import com.photon.identity.commons.enums.AuthAdaptor;
import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ActivateUserAuthenticationDto {
    private String principle;
    private String credential;
    private AuthAdaptor authAdaptor;
}