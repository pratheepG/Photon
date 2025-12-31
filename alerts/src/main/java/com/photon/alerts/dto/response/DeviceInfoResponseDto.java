package com.photon.alerts.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceInfoResponseDto {

    private UUID id;
    private String deviceType;
    private String deviceId;
    private Boolean isActive;
    private String regId;
    private String platform;
    private String osVersion;
    private String appVersion;
    private String deviceModel;
    private String deviceBrand;
    private String browserVersion;
    private String screenResolution;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime lastActive;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
