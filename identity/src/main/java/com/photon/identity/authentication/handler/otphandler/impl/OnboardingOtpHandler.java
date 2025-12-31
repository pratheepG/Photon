package com.photon.identity.authentication.handler.otphandler.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.photon.dto.ApiResponseDto;
import com.photon.dto.AuthResponseDto;
import com.photon.dto.ReportingParamsDto;
import com.photon.enums.ExceptionEnum;
import com.photon.enums.SuccessEnum;
import com.photon.exception.ApplicationException;
import com.photon.identity.authentication.dto.request.LoginOtpDto;
import com.photon.identity.authentication.handler.otphandler.api.OtpHandler;
import com.photon.identity.authentication.service.PasswordService;
import com.photon.identity.commons.enums.AuthAdaptor;
import com.photon.identity.idp.dto.AuthTypeDto;
import com.photon.identity.idp.dto.IdentityProviderDto;
import com.photon.identity.idp.dto.mapper.AuthTypeMapper;
import com.photon.identity.idp.repository.AuthTypeRepository;
import com.photon.identity.idp.utils.IdentityProviderHandler;
import com.photon.utils.HttpRequestManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component("onboardingOtpHandler")
public class OnboardingOtpHandler implements OtpHandler {

    private final AuthTypeRepository authTypeRepository;
    private final PasswordService passwordService;

    public OnboardingOtpHandler(AuthTypeRepository authTypeRepository, PasswordService passwordService) {
        this.authTypeRepository = authTypeRepository;
        this.passwordService = passwordService;
    }

    @Override
    public ApiResponseDto<AuthResponseDto> requestOtp(String authType, IdentityProviderDto identityProviderDto, Object authRequest) throws ApplicationException {
        AuthResponseDto authResponse = new AuthResponseDto();
        ReportingParamsDto reportingParams = HttpRequestManager.getReportingParams();
        Optional<AuthTypeDto> authTypeOptional =  this.authTypeRepository.findById(authType).map(AuthTypeMapper::toDto);

        if (authTypeOptional.isEmpty())
            throw new ApplicationException(ExceptionEnum.ERR_1017.getErrorResponseBody("Auth type not found"), HttpStatus.UNAUTHORIZED);
        if (!authTypeOptional.get().getIsActive())
            throw new ApplicationException(ExceptionEnum.ERR_1038.getErrorResponseBody(), HttpStatus.EXPECTATION_FAILED);

        if( authTypeOptional.get().getAuthAdapter() != AuthAdaptor.SMS_OTP && authTypeOptional.get().getAuthAdapter() != AuthAdaptor.EML_OTP )
            throw new ApplicationException(ExceptionEnum.ERR_1025.getErrorResponseBody(), HttpStatus.UNAUTHORIZED);

        LoginOtpDto loginOtp = (LoginOtpDto) authRequest;

        authResponse.setAuthType(authType);
        authResponse.setSecurityKey(this.passwordService.requestOtp(loginOtp.getPrinciple(), authTypeOptional.get().getAuthAdapter(), authTypeOptional.get(), identityProviderDto.getIdentityProviderType().name(), reportingParams.getDeviceId()));
        return SuccessEnum.SUCCESS.getSuccessResponseBody(authResponse,"OTP sent successfully");
    }
}