package com.photon.alerts.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceInfoRequestDto {

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
}