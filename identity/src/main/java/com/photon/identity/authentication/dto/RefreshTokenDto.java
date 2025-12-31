package com.photon.identity.authentication.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenDto {
    private Long id;
    private String token;
    private UserDeviceDto userDevice;
    private Long refreshCount;
    private Instant expiryDate;
}
