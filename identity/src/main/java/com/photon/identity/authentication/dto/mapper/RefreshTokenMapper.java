package com.photon.identity.authentication.dto.mapper;

import com.photon.identity.authentication.dto.RefreshTokenDto;
import com.photon.identity.authentication.entity.RefreshToken;

public class RefreshTokenMapper {

    public static RefreshTokenDto toDto(RefreshToken refreshToken) {
        if (refreshToken == null) {
            return null;
        }

        return new RefreshTokenDto(
                refreshToken.getId(),
                refreshToken.getToken(),
                UserDeviceMapper.toDto(refreshToken.getUserDevice()),
                refreshToken.getRefreshCount(),
                refreshToken.getExpiryDate()
        );
    }

    public static RefreshToken toEntity(RefreshTokenDto refreshTokenDto) {
        if (refreshTokenDto == null) {
            return null;
        }

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(refreshTokenDto.getId());
        refreshToken.setToken(refreshTokenDto.getToken());
        refreshToken.setUserDevice(UserDeviceMapper.toEntity(refreshTokenDto.getUserDevice())); // Assuming UserDevice mapping
        refreshToken.setRefreshCount(refreshTokenDto.getRefreshCount());
        refreshToken.setExpiryDate(refreshTokenDto.getExpiryDate());

        return refreshToken;
    }

    public static RefreshToken partialUpdate(RefreshTokenDto refreshTokenDto, RefreshToken refreshToken) {
        if (refreshTokenDto == null || refreshToken == null) {
            return refreshToken;
        }

        if (refreshTokenDto.getId() != null) {
            refreshToken.setId(refreshTokenDto.getId());
        }
        if (refreshTokenDto.getToken() != null) {
            refreshToken.setToken(refreshTokenDto.getToken());
        }
        if (refreshTokenDto.getUserDevice() != null) {
            refreshToken.setUserDevice(UserDeviceMapper.toEntity(refreshTokenDto.getUserDevice()));
        }
        if (refreshTokenDto.getRefreshCount() != null) {
            refreshToken.setRefreshCount(refreshTokenDto.getRefreshCount());
        }
        if (refreshTokenDto.getExpiryDate() != null) {
            refreshToken.setExpiryDate(refreshTokenDto.getExpiryDate());
        }

        return refreshToken;
    }
}
