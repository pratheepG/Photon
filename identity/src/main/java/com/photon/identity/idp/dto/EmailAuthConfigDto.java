package com.photon.identity.idp.dto;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmailAuthConfigDto implements AuthTypeConfigDto, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long maxOtpAttempts;
    private Long resendOtpInSeconds;
    private Long otpExpiryInMinutes;
    private Long maxAttemptResetAfterMinutes;
    private Long otpLength;

}