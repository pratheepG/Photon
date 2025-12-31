package com.photon.identity.authentication.dto.mapper;

import com.photon.identity.authentication.dto.UserDeviceDto;
import com.photon.identity.authentication.entity.UserDevice;

public class UserDeviceMapper {

    public static UserDeviceDto toDto(UserDevice userDevice) {
        if (userDevice == null) {
            return null;
        }

        return new UserDeviceDto(
                userDevice.getId(),
                userDevice.getUserId(),
                userDevice.getDeviceType(),
                userDevice.getDeviceId(),
                userDevice.getIsActive(),
                RefreshTokenMapper.toDto(userDevice.getRefreshToken()),
                userDevice.getIsRefreshActive(),
                userDevice.getCreatedOn(),
                userDevice.getModifiedOn()
        );
    }

    public static UserDevice toEntity(UserDeviceDto userDeviceDto) {
        if (userDeviceDto == null) {
            return null;
        }

        UserDevice userDevice = new UserDevice();
        userDevice.setId(userDeviceDto.getId());
        userDevice.setUserId(userDeviceDto.getUserId());
        userDevice.setDeviceType(userDeviceDto.getDeviceType());
        userDevice.setDeviceId(userDeviceDto.getDeviceId());
        userDevice.setIsActive(userDeviceDto.getIsActive());
        userDevice.setRefreshToken(RefreshTokenMapper.toEntity(userDeviceDto.getRefreshToken()));
        userDevice.setIsRefreshActive(userDeviceDto.getIsRefreshActive());
        userDevice.setCreatedOn(userDeviceDto.getCreatedOn());
        userDevice.setModifiedOn(userDeviceDto.getModifiedOn());

        return userDevice;
    }

    public static UserDevice partialUpdate(UserDeviceDto userDeviceDto, UserDevice userDevice) {
        if (userDeviceDto == null || userDevice == null) {
            return userDevice;
        }

        if (userDeviceDto.getId() != null) {
            userDevice.setId(userDeviceDto.getId());
        }
        if (userDeviceDto.getUserId() != null) {
            userDevice.setUserId(userDeviceDto.getUserId());
        }
        if (userDeviceDto.getDeviceType() != null) {
            userDevice.setDeviceType(userDeviceDto.getDeviceType());
        }
        if (userDeviceDto.getDeviceId() != null) {
            userDevice.setDeviceId(userDeviceDto.getDeviceId());
        }
        if (userDeviceDto.getIsActive() != null) {
            userDevice.setIsActive(userDeviceDto.getIsActive());
        }
        if (userDeviceDto.getRefreshToken() != null) {
            userDevice.setRefreshToken(RefreshTokenMapper.toEntity(userDeviceDto.getRefreshToken()));
        }
        if (userDeviceDto.getIsRefreshActive() != null) {
            userDevice.setIsRefreshActive(userDeviceDto.getIsRefreshActive());
        }
        if (userDeviceDto.getCreatedOn() != null) {
            userDevice.setCreatedOn(userDeviceDto.getCreatedOn());
        }
        if (userDeviceDto.getModifiedOn() != null) {
            userDevice.setModifiedOn(userDeviceDto.getModifiedOn());
        }

        return userDevice;
    }
}
