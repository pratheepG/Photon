package com.photon.identity.authentication.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDeviceRegistrationResponseDto {
    private String sessionId;
    private Long id;
}