package com.photon.identity.authentication.handler.idp.api;

import com.photon.dto.ApiResponseDto;
import com.photon.dto.AuthResponseDto;
import com.photon.dto.ReportingParamsDto;
import com.photon.enums.ExceptionEnum;
import com.photon.enums.SuccessEnum;
import com.photon.exception.ApplicationException;
import com.photon.identity.authentication.entity.UserDevice;
import com.photon.identity.authentication.repository.UserDeviceRepository;
import com.photon.identity.authentication.service.RefreshTokenService;
import com.photon.identity.idp.dto.IdentityProviderDto;
import com.photon.utils.HttpRequestManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;

import java.time.Duration;
import java.time.Instant;

public abstract class IdentityProvider {

    protected final StringRedisTemplate stringRedisTemplate;
    protected final UserDeviceRepository userDeviceRepository;
    protected final RefreshTokenService refreshTokenService;

    protected IdentityProvider(StringRedisTemplate stringRedisTemplate, UserDeviceRepository userDeviceRepository, RefreshTokenService refreshTokenService) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.userDeviceRepository = userDeviceRepository;
        this.refreshTokenService = refreshTokenService;
    }

    public abstract ApiResponseDto<AuthResponseDto> authenticate(IdentityProviderDto identityProvider, String authType, Object authRequest) throws ApplicationException;

    public ApiResponseDto<?> logOut() throws ApplicationException {
        ReportingParamsDto reportingParams = HttpRequestManager.getReportingParams();
        String jti = HttpRequestManager.getTokenId();

        UserDevice userDevice = this.userDeviceRepository.findByUserId(HttpRequestManager.getUserId())
                .filter(device -> device.getDeviceId().equals(reportingParams.getDeviceId()))
                .orElseThrow(() -> new ApplicationException(ExceptionEnum.ERR_1016.getErrorResponseBody("Invalid device Id supplied. No matching device found for the given user "), HttpStatus.EXPECTATION_FAILED));

        userDevice.setIsRefreshActive(false);
        userDevice.setUserId(null);

        refreshTokenService.delete(userDevice.getRefreshToken());

        String revokedKey = "revoked:" + jti;
        long expiryEpochSeconds = Long.parseLong(HttpRequestManager.getTokenExpiry());
        long nowEpochSeconds = Instant.now().getEpochSecond();
        long ttlSeconds = Math.max(0, expiryEpochSeconds - nowEpochSeconds);

        this.stringRedisTemplate.opsForValue().set(revokedKey, "1", Duration.ofSeconds(ttlSeconds));
        return SuccessEnum.SUCCESS.getSuccessResponseBody("Successfully Logged out.");
    }

}