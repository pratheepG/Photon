package com.photon.identity.authentication.handler.otphandler.api;

import com.photon.dto.ApiResponseDto;
import com.photon.dto.AuthResponseDto;
import com.photon.exception.ApplicationException;
import com.photon.identity.idp.dto.IdentityProviderDto;

public interface OtpHandler {
    public ApiResponseDto<AuthResponseDto> requestOtp(String authType, IdentityProviderDto identityProviderDto, Object authRequest) throws ApplicationException;
}