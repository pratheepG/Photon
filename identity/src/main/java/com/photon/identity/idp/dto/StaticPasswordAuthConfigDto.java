package com.photon.identity.idp.dto;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class StaticPasswordAuthConfigDto implements AuthTypeConfigDto, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long maxLoginAttempts;
    private Long captchaNeededOnAttempts;
    private Boolean isCaptchaEnabled;
    private Boolean isAutoUnlockEnabled;
    private Long autoUnLockAfterInMinutes;
    private Long passwordExpiryInMinutes;

}