package com.photon.alerts.dto.mapper;

import com.photon.alerts.dto.request.DeviceInfoRequestDto;
import com.photon.alerts.dto.response.DeviceInfoResponseDto;
import com.photon.alerts.entity.DeviceInfo;

import java.time.LocalDateTime;

public class DeviceInfoMapper {

    public static DeviceInfo toEntity(DeviceInfoRequestDto dto) {
        return DeviceInfo.builder()
                .deviceType(dto.getDeviceType())
                .deviceId(dto.getDeviceId())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .regId(dto.getRegId())
                .platform(dto.getPlatform())
                .osVersion(dto.getOsVersion())
                .appVersion(dto.getAppVersion())
                .deviceModel(dto.getDeviceModel())
                .deviceBrand(dto.getDeviceBrand())
                .browserVersion(dto.getBrowserVersion())
                .screenResolution(dto.getScreenResolution())
                .ipAddress(dto.getIpAddress())
                .userAgent(dto.getUserAgent())
                .lastActive(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();
    }

    public static DeviceInfoResponseDto toDto(DeviceInfo entity) {
        return DeviceInfoResponseDto.builder()
                .id(entity.getId())
                .deviceType(entity.getDeviceType())
                .deviceId(entity.getDeviceId())
                .isActive(entity.getIsActive())
                .regId(entity.getRegId())
                .platform(entity.getPlatform())
                .osVersion(entity.getOsVersion())
                .appVersion(entity.getAppVersion())
                .deviceModel(entity.getDeviceModel())
                .deviceBrand(entity.getDeviceBrand())
                .browserVersion(entity.getBrowserVersion())
                .screenResolution(entity.getScreenResolution())
                .ipAddress(entity.getIpAddress())
                .userAgent(entity.getUserAgent())
                .lastActive(entity.getLastActive())
                .createdAt(entity.getCreatedAt())
                .modifiedAt(entity.getModifiedAt())
                .build();
    }
}
